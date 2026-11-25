package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
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
   *
   */
  fun findAllWherePersonSurnameIsNull(pageable: Pageable): Page<FacilitatorEntity> = facilitatorRepository.findAllWherePersonSurnameIsNull(pageable)

  /**
   * Updates the personal names of a facilitator in the system.
   *
   * @param facilitatorId The facilitator ID for which to update structured names.
   */
  fun updateNameByFacilitatorId(facilitatorId: UUID): FacilitatorEntity? {
    log.debug("START update name for facilitator with id: {}", facilitatorId)

    val facilitator = facilitatorRepository.findByIdOrNull(facilitatorId)
    if (facilitator != null) {
      val staffDto = getStaffByCode(facilitator.ndeliusPersonCode)
      facilitator.personForename = staffDto.forename
      facilitator.personSurname = staffDto.surname
      facilitator.personMiddleNames = staffDto.middleNames
      facilitatorRepository.save(facilitator)
      log.info("[Facilitator of id $facilitatorId]...success!")
    } else {
      log.error("[Facilitator of id $facilitatorId] doesn't exists!")
    }

    log.debug("END update facilitator names for id: {}", facilitatorId)

    return facilitator
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
