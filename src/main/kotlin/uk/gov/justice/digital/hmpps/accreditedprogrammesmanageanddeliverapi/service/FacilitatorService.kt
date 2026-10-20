package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import java.util.UUID

@Service
@Transactional
class FacilitatorService(
  private val facilitatorRepository: FacilitatorRepository,
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
  suspend fun updatePersonName(facilitatorIds: List<UUID>): RefreshPersonalDetailsResult {
    log.debug(
      "START update personal details for facilitators with missing structured personal names: {}",
      facilitatorIds,
    )
    val facilitatorIdList = facilitatorIds.ifEmpty {
      withContext(Dispatchers.IO) {
        // facilitatorRepository.findAllIdsWherePersonSurnameIsNull()
      }
    }
    // val refreshPersonalDetailsResult = TODO call nDelius api and refresh facilitator names
    log.debug("END update personal details for facilitators with missing structured personal names: {}", facilitatorIds)

    return RefreshPersonalDetailsResult() // refreshPersonalDetailsResult
  }
}
