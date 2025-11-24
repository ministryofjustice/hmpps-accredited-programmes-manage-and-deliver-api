package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class ProgrammeGroupServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    stubAuthTokenEndpoint()
    nDeliusApiStubs.stubUserTeamsResponse(
      "AUTH_ADM",
      NDeliusUserTeams(
        teams = listOf(
          NDeliusUserTeam(
            code = "TEAM001",
            description = "Test Team 1",
            pdu = CodeDescription("PDU001", "Test PDU 1"),
            region = CodeDescription("REGION001", "WIREMOCKED REGION"),
          ),
        ),
      ),
    )
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("Create Group")
  inner class CreateGroup {
    @Test
    fun `createGroup can successfully create a new group`() {
      val slots = setOf(
        CreateGroupSessionSlot(DayOfWeek.MONDAY, 1, 1, AmOrPm.AM),
        CreateGroupSessionSlot(DayOfWeek.TUESDAY, 1, 1, AmOrPm.PM),
      )

      val group = ProgrammeGroupFactory().toCreateGroup(slots)

      programmeGroupService.createGroup(group, "AUTH_ADM")
      val createdGroup = programmeGroupRepository.findByCode(group.groupCode)
      assertThat { createdGroup }.isNotNull
      assertThat(createdGroup?.code).isEqualTo(group.groupCode)
      assertThat(createdGroup?.programmeGroupSessionSlots).size().isEqualTo(2)

      assertThat(createdGroup?.programmeGroupSessionSlots).allMatch {
        (it.dayOfWeek == DayOfWeek.MONDAY && it.startTime.equals(LocalTime.of(1, 1))) ||
          (it.dayOfWeek == DayOfWeek.TUESDAY && it.startTime.equals(LocalTime.of(13, 1)))
      }
    }

    @Test
    fun `createGroup throws an error if a group already exists`() {
      val groupCode = "AAA111"
      val existingGroup = ProgrammeGroupFactory().withCode(groupCode).withRegionName("WIREMOCKED REGION").produce()
      testDataGenerator.createGroup(existingGroup)

      assertThrows<ConflictException> {
        programmeGroupService.createGroup(
          CreateGroupRequest(
            existingGroup.code,
            ProgrammeGroupCohort.from(existingGroup.cohort, existingGroup.isLdc),
            existingGroup.sex,
            LocalDate.parse("2025-01-01"),
            setOf(),
            "PDU_NAME",
            "PDU_CODE",
            "DELIVERY_LOCATION_NAME",
            "DELIVERY_LOCATION_CODE",
          ),
          username = "AUTH_ADM",
        )
      }
    }
  }

  @Nested
  @DisplayName("Get Group Details")
  inner class GetGroupDetails {

    @Test
    fun `getGroupWaitlistData returns empty page when no waitlist data exists`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)
      val pageable = PageRequest.of(0, 10)

      // When
      val result = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = null,
        nameOrCRN = null,
        pdu = null,
        reportingTeams = null,
      )

      // Then
      assertThat(result).isNotNull
      assertThat(result.pagedGroupData.content).isEmpty()
    }

    @Test
    fun `getGroupWaitlistData throws NotFoundException for non-existent group`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()
      val pageable = PageRequest.of(0, 10)

      // When & Then
      assertThrows<NotFoundException> {
        programmeGroupService.getGroupWaitlistDataByCriteria(
          pageable = pageable,
          selectedTab = GroupPageTab.WAITLIST,
          groupId = nonExistentGroupId,
          sex = null,
          cohort = null,
          nameOrCRN = null,
          pdu = null,
          reportingTeams = null,
        )
      }
    }

    @Test
    fun `getGroupWaitlistData filters by sex correctly`() {
      // Given
      val group = setupGroupWaitListItemTestData()
      val pageable = PageRequest.of(0, 10)

      // When
      val maleResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = "Male",
        cohort = null,
        nameOrCRN = null,
        pdu = null,
        reportingTeams = null,
      )

      val femaleResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = "Female",
        cohort = null,
        nameOrCRN = null,
        pdu = null,
        reportingTeams = null,
      )

      // Then
      assertThat(maleResults.pagedGroupData.content).allMatch { it.sex == "Male" }
      assertThat(femaleResults.pagedGroupData.content).allMatch { it.sex == "Female" }
    }

    @Test
    fun `getGroupWaitlistData filters by cohort correctly`() {
      // Given
      val group = setupGroupWaitListItemTestData()
      val pageable = PageRequest.of(0, 10)

      // When
      val sexualOffenceResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = ProgrammeGroupCohort.SEXUAL,
        nameOrCRN = null,
        pdu = null,
        reportingTeams = null,
      )

      val generalOffenceResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = ProgrammeGroupCohort.GENERAL,
        nameOrCRN = null,
        pdu = null,
        reportingTeams = null,
      )

      // Then
      assertThat(sexualOffenceResults.pagedGroupData.content).allMatch { it.cohort == OffenceCohort.SEXUAL_OFFENCE }
      assertThat(generalOffenceResults.pagedGroupData.content).allMatch { it.cohort == OffenceCohort.GENERAL_OFFENCE }
    }

    @Test
    fun `getGroupWaitlistData filters by nameOrCRN correctly`() {
      // Given
      val group = setupGroupWaitListItemTestData()
      val pageable = PageRequest.of(0, 10)

      // When
      val nameResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = null,
        nameOrCRN = "john",
        pdu = null,
        reportingTeams = null,
      )

      val crnResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = null,
        nameOrCRN = "CRN001",
        pdu = null,
        reportingTeams = null,
      )

      // Then
      assertThat(nameResults.pagedGroupData.content).allMatch {
        it.personName.contains("John", ignoreCase = true)
      }
      assertThat(crnResults.pagedGroupData.content).allMatch {
        it.crn.contains("CRN001", ignoreCase = true)
      }
    }

    @Test
    fun `getGroupWaitlistData filters by pdu correctly`() {
      // Given
      val group = setupGroupWaitListItemTestData()
      val pageable = PageRequest.of(0, 10)

      // When
      val pduResults = programmeGroupService.getGroupWaitlistDataByCriteria(
        pageable = pageable,
        selectedTab = GroupPageTab.WAITLIST,
        groupId = group.id!!,
        sex = null,
        cohort = null,
        nameOrCRN = null,
        pdu = "Test PDU 1",
        reportingTeams = null,
      )

      // Then
      assertThat(pduResults.pagedGroupData.content).allMatch { it.pdu == "Test PDU 1" }
    }

    private fun setupGroupWaitListItemTestData(): ProgrammeGroupEntity {
      val group = ProgrammeGroupFactory().withCode("TEST002").produce()
      testDataGenerator.createGroup(group)

      val referrals = testReferralHelper.createReferrals(
        5,
        referralConfigs =
        listOf(
          TestReferralHelper.ReferralConfig(
            crn = "CRN001",
            personName = "John Smith",
            sex = "Male",
            cohort = OffenceCohort.GENERAL_OFFENCE,
            reportingPdu = "Test PDU 1",
            reportingTeam = "Team A",
          ),
          TestReferralHelper.ReferralConfig(
            crn = "CRN002",
            personName = "Jane Doe",
            sex = "Female",
            cohort = OffenceCohort.GENERAL_OFFENCE,
            reportingPdu = "Test PDU 2",
            reportingTeam = "Team B",
          ),
          TestReferralHelper.ReferralConfig(
            crn = "CRN003",
            personName = "John Brown",
            sex = "Male",
            cohort = OffenceCohort.SEXUAL_OFFENCE,
            reportingPdu = "Test PDU 1",
            reportingTeam = "Team A",
          ),
          TestReferralHelper.ReferralConfig(
            crn = "CRN004",
            personName = "Mary Johnson",
            sex = "Female",
            cohort = OffenceCohort.GENERAL_OFFENCE,
            reportingPdu = "Test PDU 2",
            reportingTeam = "Team B",
          ),
          TestReferralHelper.ReferralConfig(
            crn = "CRN005",
            personName = "Jonn Doe",
            sex = "Male",
            cohort = OffenceCohort.GENERAL_OFFENCE,
            reportingPdu = "Test PDU 1",
            reportingTeam = "Team C",
          ),
        ),
      )

//      val referrals = listOf(
//        createReferralWithWaitlistStatus("CRN001", "John Smith", "Male", "Sexual offence", "Test PDU 1", "Team A"),
//        createReferralWithWaitlistStatus("CRN002", "Jane Doe", "Female", "General offence", "Test PDU 2", "Team B"),
//        createReferralWithWaitlistStatus("CRN003", "John Brown", "Male", "Sexual offence", "Test PDU 1", "Team A"),
//        createReferralWithWaitlistStatus("CRN004", "Mary Johnson", "Female", "General offence", "Test PDU 2", "Team B"),
//        createReferralWithWaitlistStatus("CRN005", "Jonn Doe", "Male", "General offence", "Test PDU 1", "Team C"),
//      )

      referrals.forEach { referral ->
        programmeGroupMembershipService.allocateReferralToGroup(referral.id!!, group.id!!, "SYSTEM", "")
      }

      return group
    }

    @Test
    fun `getGroupFilters returns empty filters when no referral reporting locations exist`() {
      // When
      val filters = programmeGroupService.getGroupFilters()

      // Then
      assertThat(filters.locationFilterValues).isEmpty()
      assertThat(filters.sex).containsExactly("Male", "Female")
    }

    @Test
    fun `getGroupFilters returns distinct pdu and reporting team values`() {
      // Given - Create referrals with reporting locations
      val referral1 = ReferralEntityFactory().withCrn("CRN001").produce()
      val referral2 = ReferralEntityFactory().withCrn("CRN002").produce()
      val referral3 = ReferralEntityFactory().withCrn("CRN003").produce()

      val statusHistory1 = ReferralStatusHistoryEntityFactory().produce(
        referral1,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      val statusHistory2 = ReferralStatusHistoryEntityFactory().produce(
        referral2,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      val statusHistory3 = ReferralStatusHistoryEntityFactory().produce(
        referral3,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )

      val reportingLocation1 = ReferralReportingLocationFactory()
        .withReferral(referral1)
        .withPduName("PDU Alpha")
        .withReportingTeam("Team 1")
        .produce()

      val reportingLocation2 = ReferralReportingLocationFactory()
        .withReferral(referral2)
        .withPduName("PDU Beta")
        .withReportingTeam("Team 2")
        .produce()

      val reportingLocation3 = ReferralReportingLocationFactory()
        .withReferral(referral3)
        .withPduName("PDU Alpha") // Duplicate PDU
        .withReportingTeam("Team 1") // Duplicate Team
        .produce()

      // Persist test data
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral1,
        statusHistory1,
        reportingLocation1,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral2,
        statusHistory2,
        reportingLocation2,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral3,
        statusHistory3,
        reportingLocation3,
      )

      // When
      val filters = programmeGroupService.getGroupFilters()

      // Then
      val expectedTeams = mapOf(
        "PDU Alpha" to listOf("Team 1"),
        "PDU Beta" to listOf("Team 2"),
      )

      assertThat(filters.locationFilterValues)
        .allMatch { location ->
          expectedTeams[location.pduName]?.let { teams ->
            location.reportingTeams.containsAll(teams)
          } ?: true
        }
      assertThat(filters.sex).containsExactly("Male", "Female")
    }

    @Test
    fun `getGroupFilters handles multiple distinct values correctly`() {
      // Given
      val testData = listOf(
        Triple("CRN101", "North PDU", "North Team A"),
        Triple("CRN102", "South PDU", "South Team B"),
        Triple("CRN103", "East PDU", "East Team C"),
        Triple("CRN104", "West PDU", "West Team D"),
        Triple("CRN105", "North PDU", "North Team B"),
        Triple("CRN106", "South PDU", "South Team A"),
      )

      testData.forEach { (crn, pduName, reportingTeam) ->
        val referral = ReferralEntityFactory().withCrn(crn).produce()
        val statusHistory = ReferralStatusHistoryEntityFactory().produce(
          referral,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
        val reportingLocation = ReferralReportingLocationFactory()
          .withReferral(referral)
          .withPduName(pduName)
          .withReportingTeam(reportingTeam)
          .produce()

        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }

      // When
      val filters = programmeGroupService.getGroupFilters()

      // Then
      val expectedTeams = mapOf(
        "North PDU" to listOf("North Team A", "North Team B"),
        "South PDU" to listOf("South Team A", "South Team B"),
        "East PDU" to listOf("East Team C"),
        "West PDU" to listOf("West Team D"),
      )

      assertThat(filters.locationFilterValues)
        .allMatch { location ->
          expectedTeams[location.pduName]?.let { teams ->
            location.reportingTeams.containsAll(teams)
          } ?: true
        }
      assertThat(filters.sex).containsExactly("Male", "Female")
    }
  }
}
