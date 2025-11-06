package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.GroupWaitlistItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
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
  private val log = LoggerFactory.getLogger(this::class.java)

  fun createGroup(createGroup: CreateGroup): ProgrammeGroupEntity? {
    programmeGroupRepository.findByCode(createGroup.groupCode)
      ?.let { throw ConflictException("Programme group with code ${createGroup.groupCode} already exists") }

    log.info("Group created with code: ${createGroup.groupCode}")

    return programmeGroupRepository.save(createGroup.toEntity())
  }

  fun getGroupById(groupId: UUID): ProgrammeGroupEntity = programmeGroupRepository.findByIdOrNull(groupId)
    ?: throw NotFoundException("Programme group with id $groupId not found")

  fun getGroupWaitlistData(
    selectedTab: GroupPageTab,
    groupId: UUID,
    sex: String?,
    cohort: ProgrammeGroupCohort?,
    nameOrCRN: String?,
    pdu: String?,
    pageable: Pageable,
    reportingTeams: List<String>?,
  ): Page<GroupWaitlistItem> {
    // Verify the group exists first
    getGroupById(groupId)

    val specification = getGroupWaitlistItemSpecification(selectedTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

    return groupWaitlistItemViewRepository.findAll(specification, pageable)
      .map { it.toApi() }
  }

  fun getGroupWaitlistCount(
    selectedTab: GroupPageTab,
    groupId: UUID,
    sex: String?,
    cohort: ProgrammeGroupCohort?,
    nameOrCRN: String?,
    pdu: String?,
    reportingTeams: List<String>?,
  ): Int {
    // Verify the group exists first
    getGroupById(groupId)

    val specification = getGroupWaitlistItemSpecification(selectedTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

    return groupWaitlistItemViewRepository.count(specification).toInt()
  }

  fun getGroupFilters(): ProgrammeGroupDetails.Filters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()

    return ProgrammeGroupDetails.Filters(
      pduNames = referralReportingLocations.map { it.pduName }.distinct(),
      reportingTeams = referralReportingLocations.map { it.reportingTeam }.distinct(),
    )
  }
}
