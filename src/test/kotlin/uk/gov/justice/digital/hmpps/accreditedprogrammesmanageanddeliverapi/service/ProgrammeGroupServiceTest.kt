package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.PduReportingLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort.GENERAL
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab.WAITLIST
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.GroupWaitlistItemViewEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.LimitedAccessResolverService.Access
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.AuthenticationUtils
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.util.Optional
import java.util.UUID

class ProgrammeGroupServiceTest {
  private val programmeGroupRepository = mockk<ProgrammeGroupRepository>()
  private val groupWaitlistItemViewRepository = mockk<GroupWaitlistItemViewRepository>()
  private val referralReportingLocationRepository = mockk<ReferralReportingLocationRepository>()
  private val userService = mockk<UserService>()
  private val accreditedProgrammeTemplateRepository = mockk<AccreditedProgrammeTemplateRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val sessionRepository = mockk<SessionRepository>()
  private val facilitatorService = mockk<FacilitatorService>()
  private val sessionNameFormatter = mockk<SessionNameFormatter>()
  private val sessionService = mockk<SessionService>()
  private val moduleSessionTemplateRepository = mockk<ModuleSessionTemplateRepository>()
  private val moduleRepository = mockk<ModuleRepository>()
  private val authenticationUtils = mockk<AuthenticationUtils>()
  private val regionService = mockk<RegionService>()
  private val programmeGroupMembershipService = mockk<ProgrammeGroupMembershipService>()
  private val limitedAccessResolverService = mockk<LimitedAccessResolverService>()
  private lateinit var service: ProgrammeGroupService

  @BeforeEach
  fun setup() {
    service = ProgrammeGroupService(
      programmeGroupRepository,
      groupWaitlistItemViewRepository,
      referralReportingLocationRepository,
      userService,
      accreditedProgrammeTemplateRepository,
      scheduleService,
      sessionRepository,
      facilitatorService,
      sessionNameFormatter,
      sessionService,
      moduleSessionTemplateRepository,
      limitedAccessResolverService,
      true,
      moduleRepository,
      authenticationUtils,
      regionService,
      programmeGroupMembershipService,
    )
  }

  @Test
  fun shouldThrowExceptionWhenUsernameIsWithoutRegionOnCreateGroup() {
    // Given
    val username = "user1"
    val createGroupRequest = CreateGroupRequestFactory().produce()
    every { userService.getUserRegions(username) } returns emptyList()

    // When
    val exception = assertThrows<NotFoundException> {
      service.createGroup(createGroupRequest, username)
    }

    // Then
    assertTrue(
      exception.message!!
        .contains("Region for username $username not found"),
    )
    verify { userService.getUserRegions(username) }
  }

  @Test
  fun shouldThrowExceptionWhenUsernameIsWithoutRegionOnGetGroupInRegion() {
    // Given
    val username = "user1"
    val groupCode = "group1"
    every { userService.getUserRegions(username) } returns emptyList()

    // When
    val exception = assertThrows<NotFoundException> {
      service.getGroupInRegion(groupCode, username)
    }

    // Then
    assertTrue(
      exception.message!!
        .contains("Region for username $username not found"),
    )
    verify { userService.getUserRegions(username) }
  }

  @Test
  fun `should get group waitlist data by criteria with limited access offender check enabled`() {
    // Given
    val pageable: Pageable = Pageable.ofSize(10)
    val selectedTab = WAITLIST
    val groupId = UUID.randomUUID()
    val sex = "male"
    val cohort = GENERAL
    val nameOrCrn = "John Smith"
    val probationDeliveryUnit = "Test PDU"
    val probationDeliveryUnits = listOf(probationDeliveryUnit)
    val reportingTeam = "Team A"
    val reportingTeams = listOf(reportingTeam)
    val programmeGroupEntity = ProgrammeGroupFactory().withId(groupId).produce()
    val groupWaitlistItemViewEntity = GroupWaitlistItemViewEntityFactory().produce()
    val page = PageImpl(
      listOf(
        groupWaitlistItemViewEntity,
      ),
    )
    val username = "john.smith"
    val caseReferenceNumber = groupWaitlistItemViewEntity.crn
    val accessMap = mapOf(caseReferenceNumber to Access(lao = true, isExcluded = true))
    val probationDeliveryUnitReportingLocation =
      PduReportingLocation(pduName = probationDeliveryUnit, reportingTeam = reportingTeam)

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every {
      groupWaitlistItemViewRepository.findAll(
        any<Specification<GroupWaitlistItemViewEntity>>(),
        any<Pageable>(),
      )
    } returns page
    every { authenticationUtils.getUsername() } returns username
    every { limitedAccessResolverService.resolve(any(), any()) } returns accessMap
    every { groupWaitlistItemViewRepository.count(any<Specification<GroupWaitlistItemViewEntity>>()) } returns 1L
    every { referralReportingLocationRepository.getPdusAndReportingTeamsByRegions(any()) } returns listOf(
      probationDeliveryUnitReportingLocation,
    )

    // When
    val result = service.getGroupWaitlistDataByCriteria(
      pageable = pageable,
      selectedTab = selectedTab,
      groupId = groupId,
      sex = sex,
      cohort = cohort,
      nameOrCrn = nameOrCrn,
      pdus = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    )

    // Then
    assertThat(result).isNotNull()
    assertThat(result.pagedGroupData.content.size).isEqualTo(1)
    assertThat(result.pagedGroupData.content[0].crn).isEqualTo(caseReferenceNumber)
    assertThat(result.pagedGroupData.content[0].isLimitedAccessOffender).isTrue()
    assertThat(result.pagedGroupData.content[0].isExcluded).isTrue()

    verify(exactly = 1) { programmeGroupRepository.findById(groupId) }
    verify(exactly = 1) { authenticationUtils.getUsername() }
    verify(exactly = 1) {
      groupWaitlistItemViewRepository.findAll(
        any<Specification<GroupWaitlistItemViewEntity>>(),
        any<Pageable>(),
      )
    }
    verify(exactly = 1) { limitedAccessResolverService.resolve(username, listOf(caseReferenceNumber)) }
    verify(exactly = 1) { groupWaitlistItemViewRepository.count(any<Specification<GroupWaitlistItemViewEntity>>()) }
    verify(exactly = 1) { referralReportingLocationRepository.getPdusAndReportingTeamsByRegions(any()) }
  }

  @Test
  fun `should get group waitlist data by criteria with limited access offender check disabled`() {
    // Given
    service = ProgrammeGroupService(
      programmeGroupRepository,
      groupWaitlistItemViewRepository,
      referralReportingLocationRepository,
      userService,
      accreditedProgrammeTemplateRepository,
      scheduleService,
      sessionRepository,
      facilitatorService,
      sessionNameFormatter,
      sessionService,
      moduleSessionTemplateRepository,
      limitedAccessResolverService,
      false,
      moduleRepository,
      authenticationUtils,
      regionService,
      programmeGroupMembershipService,
    )
    val pageable: Pageable = Pageable.ofSize(10)
    val selectedTab = WAITLIST
    val groupId = UUID.randomUUID()
    val sex = "male"
    val cohort = GENERAL
    val nameOrCrn = "John Smith"
    val probationDeliveryUnit = "Test PDU"
    val probationDeliveryUnits = listOf(probationDeliveryUnit)
    val reportingTeam = "Team A"
    val reportingTeams = listOf(reportingTeam)
    val programmeGroupEntity = ProgrammeGroupFactory().withId(groupId).produce()
    val groupWaitlistItemViewEntity = GroupWaitlistItemViewEntityFactory().produce()
    val page = PageImpl(
      listOf(
        groupWaitlistItemViewEntity,
      ),
    )
    val caseReferenceNumber = groupWaitlistItemViewEntity.crn
    val probationDeliveryUnitReportingLocation =
      PduReportingLocation(pduName = probationDeliveryUnit, reportingTeam = reportingTeam)

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every {
      groupWaitlistItemViewRepository.findAll(
        any<Specification<GroupWaitlistItemViewEntity>>(),
        any<Pageable>(),
      )
    } returns page
    every { groupWaitlistItemViewRepository.count(any<Specification<GroupWaitlistItemViewEntity>>()) } returns 1L
    every { referralReportingLocationRepository.getPdusAndReportingTeamsByRegions(any()) } returns listOf(
      probationDeliveryUnitReportingLocation,
    )

    // When
    val result = service.getGroupWaitlistDataByCriteria(
      pageable = pageable,
      selectedTab = selectedTab,
      groupId = groupId,
      sex = sex,
      cohort = cohort,
      nameOrCrn = nameOrCrn,
      pdus = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    )

    // Then
    assertThat(result).isNotNull()
    assertThat(result.pagedGroupData.content.size).isEqualTo(1)
    assertThat(result.pagedGroupData.content[0].crn).isEqualTo(caseReferenceNumber)
    assertThat(result.pagedGroupData.content[0].isLimitedAccessOffender).isFalse()
    assertThat(result.pagedGroupData.content[0].isExcluded).isFalse()

    verify(exactly = 1) { programmeGroupRepository.findById(groupId) }
    verify(exactly = 0) { authenticationUtils.getUsername() }
    verify(exactly = 1) {
      groupWaitlistItemViewRepository.findAll(
        any<Specification<GroupWaitlistItemViewEntity>>(),
        any<Pageable>(),
      )
    }
    verify(exactly = 0) { limitedAccessResolverService.resolve(any(), any()) }
    verify(exactly = 1) { groupWaitlistItemViewRepository.count(any<Specification<GroupWaitlistItemViewEntity>>()) }
    verify(exactly = 1) { referralReportingLocationRepository.getPdusAndReportingTeamsByRegions(any()) }
  }
}
