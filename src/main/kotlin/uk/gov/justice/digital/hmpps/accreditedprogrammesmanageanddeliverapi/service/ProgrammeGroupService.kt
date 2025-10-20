package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.GroupWaitlistItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getGroupWaitlistItemSpecification
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
) {
  fun createGroup(groupCode: String): ProgrammeGroupEntity? {
    programmeGroupRepository.findByCode(groupCode)?.let { throw ConflictException("Programme group with code $groupCode already exists") }

    return programmeGroupRepository.save(
      ProgrammeGroupEntity(
        code = groupCode,
      ),
    )
  }

  fun getGroupById(groupId: UUID): ProgrammeGroupEntity = programmeGroupRepository.findByIdOrNull(groupId)
    ?: throw NotFoundException("Programme group with id $groupId not found")

  fun getGroupWaitlistData(
    groupId: UUID,
    sex: String?,
    cohort: OffenceCohort?,
    nameOrCRN: String?,
    pdu: String?,
    pageable: Pageable,
  ): Page<GroupWaitlistItem> {
    // Verify the group exists first
    getGroupById(groupId)

    val specification = getGroupWaitlistItemSpecification(groupId, sex, cohort, nameOrCRN, pdu)

    return groupWaitlistItemViewRepository.findAll(specification, pageable)
      .map { it.toApi() }
  }

  fun getGroupFilters(): ProgrammeGroupDetails.Filters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val distinctPDUs = referralReportingLocations.map { it.pduName }.distinct()
    val distinctReportingTeams = referralReportingLocations.map { it.reportingTeam }.distinct()

    return ProgrammeGroupDetails.Filters(
      pduNames = distinctPDUs,
      reportingTeams = distinctReportingTeams,
    )
  }
}
