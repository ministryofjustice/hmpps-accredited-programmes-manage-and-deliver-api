package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.StaffDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_STAFF_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import java.util.UUID
import kotlin.collections.ifEmpty

@Service
@Transactional
class FacilitatorService(
  private val facilitatorRepository: FacilitatorRepository,
  private val telemetryService: TelemetryService,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun findOrCreateFacilitator(teamMember: CreateGroupTeamMember): FacilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(teamMember.facilitatorCode)
    ?: facilitatorRepository.save(teamMember.toFacilitatorEntity())

  fun findOrCreateFacilitator(editSessionFacilitatorRequest: EditSessionFacilitatorRequest): FacilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(editSessionFacilitatorRequest.facilitatorCode)
    ?: facilitatorRepository.save(editSessionFacilitatorRequest.toFacilitatorEntity())

  /**
   * Updates the personal names of facilitators in the system. If the provided list of facilitator IDs is empty,
   * it retrieves all facilitator IDs with missing structured personal names from the repository.
   *
   * @param facilitatorIds The list of facilitator IDs for which to update personal names.
   *                       If the list is empty, IDs are fetched from the repository.
   * @return A result object containing lists of IDs that were successfully updated, not found, or failed during the update process.
   */
  suspend fun updateNameByFacilitatorIds(facilitatorIds: List<UUID>): RefreshPersonalDetailsResult {
    log.debug(
      "START update personal details for facilitators with missing structured personal names: {}",
      facilitatorIds,
    )
    val successIds: MutableList<UUID> = mutableListOf()
    val notFoundIds: MutableList<UUID> = mutableListOf()
    val failureIds: MutableList<UUID> = mutableListOf()

    val idsToProcess = facilitatorIds.ifEmpty {
      withContext(Dispatchers.IO) {
        facilitatorRepository.findAllIdsWherePersonSurnameIsNull()
      }
    }

    idsToProcess.forEachIndexed { index, id ->
      log.info("[{}/{}] Refreshing Name for Facilitator with id {}...", index + 1, facilitatorIds.size, id)
      try {
        val optionalFacilitator = facilitatorRepository.findById(id)
        if (optionalFacilitator.isPresent) {
          val facilitator = optionalFacilitator.get()
          val staffDto = getStaffByCode(facilitator.ndeliusPersonCode)
          facilitator.personForename = staffDto.forename
          facilitator.personSurname = staffDto.surname
          facilitator.personMiddleNames = staffDto.middleNames
          facilitatorRepository.save(facilitator)
          log.info("[Facilitator of id $id]...success!")
          successIds.add(id)
        } else {
          notFoundIds.add(id)
        }
      } catch (e: Exception) {
        log.error("[Facilitator of id $id]...failure: ${e.message})")
        failureIds.add(id)
      }
    }
    log.debug("END update personal details for facilitators with missing structured personal names: {}", facilitatorIds)

    return RefreshPersonalDetailsResult(
      successIds,
      notFoundIds,
      failureIds,
    )
  }

  private fun getStaffByCode(code: String): StaffDto = when (val response = nDeliusIntegrationApiClient.getStaffByCode(code)) {
    is ClientResult.Failure -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_STAFF_N_DELIUS.eventName}.failure",
        integrationActionType = GET_STAFF_N_DELIUS.name,
        outcome = "failure",
      )
      throw BusinessException("Failed to retrieve Facilitator for code: $code due to upstream error")
    }

    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_STAFF_N_DELIUS.eventName}.success",
        integrationActionType = GET_STAFF_N_DELIUS.name,
        outcome = "success",
      )

      response.body
    }
  }
}
