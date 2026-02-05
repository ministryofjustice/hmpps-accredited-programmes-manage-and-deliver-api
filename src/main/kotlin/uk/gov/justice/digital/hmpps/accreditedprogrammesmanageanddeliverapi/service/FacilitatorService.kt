package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository

@Service
@Transactional
class FacilitatorService(
  private val facilitatorRepository: FacilitatorRepository,
) {
  fun findOrCreateFacilitator(teamMember: CreateGroupTeamMember): FacilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(teamMember.facilitatorCode)
    ?: facilitatorRepository.save(teamMember.toFacilitatorEntity())

  fun findOrCreateFacilitator(editSessionFacilitatorRequest: EditSessionFacilitatorRequest): FacilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(editSessionFacilitatorRequest.facilitatorCode)
    ?: facilitatorRepository.save(editSessionFacilitatorRequest.toFacilitatorEntity())
}
