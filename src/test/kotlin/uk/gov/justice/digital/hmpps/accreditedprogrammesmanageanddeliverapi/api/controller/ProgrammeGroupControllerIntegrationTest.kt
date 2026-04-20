package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupScheduleOverview
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupSessionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleIndividualSessionDetailsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionTypeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionScheduleType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupDaysAndTimes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.GroupSexDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.GroupTreatmentManagerAndFacilitatorDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.GroupsByRegionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.PagedProgrammeDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.UAAB
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusAppointmentEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPduWithTeamFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusRegionWithMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamWithMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.NDeliusApiProbationDeliveryUnitWithOfficeLocationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class ProgrammeGroupControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  private lateinit var referrals: List<ReferralEntity>

  @Autowired
  private lateinit var nDeliusAppointmentRepository: NDeliusAppointmentRepository

  @Autowired
  private lateinit var scheduleService: ScheduleService

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs.clearAllStubs()
    govUkApiStubs.stubBankHolidaysResponse()
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

  private fun initialiseReferrals() {
    referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team C"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 2", reportingTeam = "Team B"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 2", reportingTeam = "Team C"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 3", reportingTeam = "Team B"),
      ),
    )
    // Update all referrals to 'Awaiting Allocation status'
    val status = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    referrals.forEach {
      referralService.updateStatus(
        it,
        status.id,
        createdBy = "AUTH_USER",
      )
    }
  }

  @Nested
  @DisplayName("Get Group details")
  inner class GetGroupAllocations {
    @Test
    fun `getGroupAllocations returns 200 with valid group and waitlist data`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = testGroupHelper.createGroup(groupCode = "TEST001")
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      // Allocate one referral to a group with 'Awaiting allocation' status to ensure it's not returned as part of our waitlist data
      val referral = referrals.first()
      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=0&size=10",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )
      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST001")
      assertThat(response.group.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(response.pagedGroupData.totalElements).isEqualTo(5)
      assertThat(response.otherTabTotal).isEqualTo(1)
      assertThat(response.pagedGroupData).isNotNull
      assertThat(response.pagedGroupData.content.map { it.statusColour }).isNotEmpty
    }

    @Test
    fun `getGroupAllocations returns empty page when no waitlist data exists`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.pagedGroupData.content).isEmpty()
    }

    @Test
    fun `getGroupAllocations contains a list of filters`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)

      // When
      val body = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=0&size=10",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
        HttpStatus.OK.value(),
      )

      assertThat(body.filters).isNotNull
      assertThat(body.filters.sex).isEqualTo(listOf("Male", "Female"))
      assertThat(body.filters.cohort).isEqualTo(
        listOf(
          "General offence",
          "General offence - LDC",
          "Sexual offence",
          "Sexual offence - LDC",
        ),
      )
      val expectedTeams = mapOf(
        "PDU 1" to listOf("Team A", "Team C"),
        "PDU 2" to listOf("Team B", "Team C"),
        "PDU 3" to listOf("Team B", "Team B"),
      )

      assertThat(body.filters.locationFilterValues)
        .allMatch { location ->
          expectedTeams[location.pduName]?.let { teams ->
            location.reportingTeams.containsAll(teams)
          } ?: true
        }
    }

    @Test
    fun `getGroupAllocations returns 200 and uses default filters if none are provided`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").withRegionName("TEST REGION").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )
      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST001")
      assertThat(response.group.regionName).isEqualTo("TEST REGION")
      assertThat(response.pagedGroupData.content.size).isEqualTo(6)
      assertThat(response.filters).isNotNull
      assertThat(response.filters.sex).containsExactly("Male", "Female")
      assertThat(response.pagedGroupData.totalElements).isEqualTo(6)
      assertThat(response.pagedGroupData.number).isEqualTo(0)
      assertThat(response.pagedGroupData).isNotNull
      assertThat(response.otherTabTotal).isZero
    }

    @Test
    fun `items in the WAITLIST are sorted by the sentenceEndDate`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
        HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.pagedGroupData.content).extracting("sentenceEndDate", LocalDate::class.java)
        .isSortedAccordingTo(naturalOrder<LocalDate>())
    }

    @Test
    fun `getGroupAllocations returns 404 for non-existent group`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()
      stubAuthTokenEndpoint()

      // When & Then
      performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/group/$nonExistentGroupId/WAITLIST?page=0&size=10",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `getGroupAllocations should handle pagination correctly`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST006").produce()
      testDataGenerator.createGroup(group)

      // When - Get first page
      val firstPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=0&size=2",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )
      // When - Get second page
      val secondPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=1&size=2",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(firstPageResponse.pagedGroupData.number).isEqualTo(0)
      assertThat(secondPageResponse.pagedGroupData.number).isEqualTo(1)

      assertThat(firstPageResponse.pagedGroupData.size).isEqualTo(2)
      assertThat(secondPageResponse.pagedGroupData.size).isEqualTo(2)

      val firstPageData = firstPageResponse.pagedGroupData
      val secondPageData = secondPageResponse.pagedGroupData

      assertThat(firstPageData.content.size).isEqualTo(2)
      assertThat(secondPageData.content.size).isEqualTo(2)
      assertThat(firstPageData.content).doesNotContainAnyElementsOf(secondPageData.content)
    }

    @Test
    fun `getGroupAllocations combines multiple filters correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST007").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?sex=Male&cohort=Sexual offence&pdu=Test PDU 1&page=0&size=10",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      response.pagedGroupData.content.forEach { item ->
        assertThat(item.sex).isEqualTo("Male")
        assertThat(item.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
        assertThat(item.pdu).isEqualTo("Test PDU 1")
        assertThat(item.referralId).isNotNull
        assertThat(item.sourcedFrom).isNotNull
      }
    }

    @Test
    fun `getGroupAllocations filters by multiple reportingTeams on WAITLIST tab`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST006").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?pdu=PDU 1&reportingTeam=Team A&reportingTeam=Team C",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(response.pagedGroupData.content).isNotEmpty
      assertThat(response.pagedGroupData.content).hasSize(3)
      assertThat(
        response.pagedGroupData.content.all {
          it.reportingTeam in listOf("Team A", "Team C")
        },
      ).isTrue()
    }

    @Test
    fun `getGroupAllocations should ignore reportingTeam if PDU is not also present`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST006").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?reportingTeam=Team A&reportingTeam=Team C",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(response.pagedGroupData.content).isNotEmpty
      assertThat(response.pagedGroupData.content).hasSize(6)
      assertThat(
        response.pagedGroupData.content.all {
          it.reportingTeam in listOf("Team A", "Team B", "Team C")
        },
      ).isTrue()
    }

    @Test
    fun `getGroupAllocations filters by reportingTeam and pdu together`() {
      // Given
      initialiseReferrals()
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST006B").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?pdu=PDU 1&reportingTeam=Team A",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(response.pagedGroupData.content).isNotEmpty
      assertThat(response.pagedGroupData.content).hasSize(2)

      response.pagedGroupData.content.forEach { item ->
        assertThat(item.pdu).isEqualTo("PDU 1")
        assertThat(item.reportingTeam).isEqualTo("Team A")
      }
    }

    @Test
    fun `getGroupAllocations returns 200 for ALLOCATED tab with all data when no filters are provided`() {
      // Given
      initialiseReferrals()
      val group = testGroupHelper.createGroup(groupCode = "TEST008")
      stubAuthTokenEndpoint()
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      // Allocate all our referrals to a group
      referrals.forEach {
        programmeGroupMembershipService.allocateReferralToGroup(
          it.id!!,
          group.id!!,
          "SYSTEM",
          "",
        )
      }
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/ALLOCATED",
        object : ParameterizedTypeReference<PagedProgrammeDetails<GroupItem>>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST008")
      assertThat(response.group.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(response.pagedGroupData.totalElements).isEqualTo(6)
      assertThat(response.otherTabTotal).isEqualTo(0)
      assertThat(response.filters).isNotNull
      assertThat(response.filters.sex).containsExactly("Male", "Female")
      assertThat(response.pagedGroupData.size).isEqualTo(10)
      assertThat(response.pagedGroupData.number).isEqualTo(0)
      assertThat(response.pagedGroupData.content).isNotEmpty
      assertThat(response.pagedGroupData.content).hasSize(6)
      assertThat(response.pagedGroupData.content).allMatch { it.activeProgrammeGroupId !== null }
    }

    @Test
    fun `getGroupAllocations returns 401 when not authorized`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST009").produce()
      testDataGenerator.createGroup(group)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${group.id}/WAITLIST?page=0&size=10")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get programme groups by region")
  inner class GetProgrammeGroupsByRegionTests {
    @Test
    fun `should return NOT_STARTED OR IN PROGRESS groups only with correct otherTabTotal`() {
      // Given
      stubAuthTokenEndpoint()

      val region = "WIREMOCKED REGION"
      // Create 3 not started groups, 2 started groups in the same region, 1 completed group in the same region,
      // plus one in another region
      val group1 = ProgrammeGroupFactory().withCode("GROUP-A-NS-1")
        .withRegionName(region).withEarliestStartDate(LocalDate.now().plusDays(5)).produce()
      val group2 = ProgrammeGroupFactory().withCode("GROUP-A-NS-2")
        .withRegionName(region).withEarliestStartDate(LocalDate.now().plusDays(5)).produce()
      val group3 = ProgrammeGroupFactory().withCode("GROUP-A-NS-3")
        .withRegionName(region).withEarliestStartDate(LocalDate.now().plusDays(5)).produce()

      val group4 = ProgrammeGroupFactory().withCode("GROUP-A-S-1").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(5)).produce()
      val group5 = ProgrammeGroupFactory().withCode("GROUP-A-S-2").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(1)).produce()

      val group6 = ProgrammeGroupFactory().withCode("GROUP-A-C-1").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(5)).produce()

      val groupInOtherRegion = ProgrammeGroupFactory().withCode("GROUP-B-NS-1").withRegionName("South West").produce()

      listOf(
        group1,
        group2,
        group3,
        group4,
        group5,
        group6,
        groupInOtherRegion,
      ).forEach { testDataGenerator.createGroup(it) }

      val referral1 = testDataGenerator.createReferral(personName = "GROUP-A-S-1-Completed", crn = "X123456")
      val referral2 = testDataGenerator.createReferral(personName = "GROUP-A-S-1-On-Programme", crn = "X123457")
      val referral3 = testDataGenerator.createReferral(personName = "GROUP-A-C-1-Completed", crn = "X123458")

      testDataGenerator.allocateReferralsToGroup(listOf(referral1, referral2), group4)
      testDataGenerator.allocateReferralsToGroup(listOf(referral3), group6)

      val referralStatusDescriptionCompleted =
        referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val referralStatusDescriptionOnProgramme = referralStatusDescriptionRepository.getOnProgrammeStatusDescription()
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral1,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral1,
          referralStatusDescriptionCompleted,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral2,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral3,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral3,
          referralStatusDescriptionCompleted,
        ),
      )

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/groups/NOT_STARTED_OR_IN_PROGRESS?page=0&size=10",
        object : ParameterizedTypeReference<GroupsByRegionResponse<Group>>() {},
      )

      // Then
      assertThat(response.pagedGroupData.totalElements).isEqualTo(4)
      val codes = response.pagedGroupData.content.map { it.code }
      assertThat(codes).containsExactlyInAnyOrder(
        "GROUP-A-NS-1",
        "GROUP-A-NS-2",
        "GROUP-A-NS-3",
        "GROUP-A-S-1",
      )
      // otherTabTotal should be count of started groups in the region (2)
      assertThat(response.otherTabTotal).isEqualTo(2)
      assertThat(response.regionName).isEqualTo("WIREMOCKED REGION")
    }

    @Test
    fun `should return COMPLETE groups only with correct otherTabTotal`() {
      // Given
      stubAuthTokenEndpoint()

      val region = "WIREMOCKED REGION"
      val group1 = ProgrammeGroupFactory().withCode("GROUP-A-NS-1").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().plusDays(5)).produce()
      val group2 = ProgrammeGroupFactory().withCode("GROUP-A-NS-2").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().plusDays(5)).produce()

      val group3 = ProgrammeGroupFactory().withCode("GROUP-A-S-1").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(5)).produce()
      val group4 = ProgrammeGroupFactory().withCode("GROUP-A-S-2").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(1)).produce()

      val group5 = ProgrammeGroupFactory().withCode("GROUP-A-C-1").withRegionName(region)
        .withEarliestStartDate(LocalDate.now().minusDays(5)).produce()

      listOf(group1, group2, group3, group4, group5).forEach { testDataGenerator.createGroup(it) }

      val referral1 = testDataGenerator.createReferral(personName = "GROUP-A-S-1-Completed", crn = "X123456")
      val referral2 = testDataGenerator.createReferral(personName = "GROUP-A-S-1-On-Programme", crn = "X123457")
      val referral3 = testDataGenerator.createReferral(personName = "GROUP-A-C-1-Completed", crn = "X123458")

      testDataGenerator.allocateReferralsToGroup(listOf(referral1, referral2), group3)
      testDataGenerator.allocateReferralsToGroup(listOf(referral3), group5)

      val referralStatusDescriptionCompleted =
        referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val referralStatusDescriptionOnProgramme = referralStatusDescriptionRepository.getOnProgrammeStatusDescription()
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral1,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral1,
          referralStatusDescriptionCompleted,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral2,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral3,
          referralStatusDescriptionOnProgramme,
        ),
      )
      testDataGenerator.creatReferralStatusHistory(
        ReferralStatusHistoryEntityFactory().produce(
          referral3,
          referralStatusDescriptionCompleted,
        ),
      )

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/groups/COMPLETE?page=0&size=10",
        object : ParameterizedTypeReference<GroupsByRegionResponse<Group>>() {},
      )

      // Then: should contain only started groups
      assertThat(response.pagedGroupData.totalElements).isEqualTo(2)
      val codes = response.pagedGroupData.content.map { it.code }
      assertThat(codes).containsExactlyInAnyOrder("GROUP-A-C-1", "GROUP-A-S-2")
      // otherTabTotal should be count of not-started or in progress groups (3)
      assertThat(response.otherTabTotal).isEqualTo(3)
      assertThat(response.regionName).isEqualTo("WIREMOCKED REGION")
    }

    @Test
    fun `should paginate results`() {
      // Given
      stubAuthTokenEndpoint()
      val region = "WIREMOCKED REGION"
      val group1 = ProgrammeGroupFactory().withCode("GROUP-A-NS-1").withRegionName(region).produce()
      val group2 = ProgrammeGroupFactory().withCode("GROUP-A-NS-2").withRegionName(region).produce()
      val group3 = ProgrammeGroupFactory().withCode("GROUP-A-NS-3").withRegionName(region).produce()
      listOf(group1, group2, group3).forEach { testDataGenerator.createGroup(it) }

      // When
      val page0 = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/groups/NOT_STARTED_OR_IN_PROGRESS?page=0&size=1",
        object : ParameterizedTypeReference<GroupsByRegionResponse<Group>>() {},
      )
      val page1 = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/groups/NOT_STARTED_OR_IN_PROGRESS?page=1&size=1",
        object : ParameterizedTypeReference<GroupsByRegionResponse<Group>>() {},
      )

      // Then
      assertThat(page0.pagedGroupData.size).isEqualTo(1)
      assertThat(page0.pagedGroupData.totalElements).isEqualTo(3)
      assertThat(page1.pagedGroupData.size).isEqualTo(1)
      assertThat(page1.pagedGroupData.totalElements).isEqualTo(3)
    }

    @Test
    fun `should return 401 when not authorized`() {
      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/groups/NOT_STARTED_OR_IN_PROGRESS?page=0&size=1)")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Allocate to Programme group")
  inner class AllocateToProgrammeGroup {
    @Test
    fun `allocateReferralToGroup can successfully allocate a referral to a group`() {
      // Given
      val theCrnNumber = randomUppercaseString()
      val group = testGroupHelper.createGroup()

      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(theCrnNumber, 1)
      nDeliusApiStubs.stubPersonalDetailsResponse(
        NDeliusPersonalDetailsFactory().withName(
          FullName(
            forename = "the-forename",
            middleNames = null,
            surname = "the-surname",
          ),
        ).produce(),
      )
      oasysApiStubs.stubSuccessfulPniResponse(theCrnNumber)
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      val referral = testReferralHelper.createReferral(crn = theCrnNumber, personName = "the-forename the-surname")
      val allocateToGroupRequest = AllocateToGroupRequest(additionalDetails = "The additional details for the test")

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = allocateToGroupRequest,
        returnType = object : ParameterizedTypeReference<AllocateToGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(response.message).isEqualTo("the-forename the-surname was added to this group. Their referral status is now Scheduled.")

      assertThat(foundReferral).isNotNull
      assertThat(foundReferral.id).isEqualTo(referral.id)
      assertThat(foundReferral.programmeGroupMemberships).hasSize(1)
      val currentGroupMembership = foundReferral.programmeGroupMemberships.first()
      assertThat(currentGroupMembership.programmeGroup.id).isEqualTo(group.id)
      assertThat(currentGroupMembership.programmeGroup.sessions.sumOf { it.attendees.count() }).isEqualTo(21)

      wiremock.verify(1, postRequestedFor(urlEqualTo("/appointments")))
      val nDeliusAppointments = nDeliusAppointmentRepository.findAll()
      assertThat(nDeliusAppointments.size).isEqualTo(21)
      assertThat(foundReferral.eventId).isIn(nDeliusAppointments.mapNotNull { it.referral.eventId })
      assertThat(foundReferral).isNotNull
      assertThat(foundReferral.id).isEqualTo(referral.id)
      assertThat(foundReferral.programmeGroupMemberships).hasSize(1)
      assertThat(foundReferral.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)
      // Check that we have added the PoP to the session attendees list
      val attendeeList =
        foundReferral.programmeGroupMemberships.first().programmeGroup.sessions.flatMap { sessionEntity -> sessionEntity.attendees.map { it.personName } }
      assertThat(attendeeList).allMatch { attendeeList.contains(it) }
    }

    @Test
    fun `allocateReferralToGroup throws an error if referral does not exist`() {
      val referralId = UUID.randomUUID()
      val group = testGroupHelper.createGroup()

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/$referralId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )

      assertThat(exception.userMessage).isEqualTo("Not Found: Referral with id $referralId not found")
    }

    @Test
    fun `allocateReferralToGroup throws an error if group does not exist`() {
      val groupId = UUID.randomUUID()
      val referral = testReferralHelper.createReferral()

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/$groupId/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )
      assertThat(exception.userMessage).isEqualTo("Not Found: Group with id $groupId not found")
    }

    @Test
    fun `allocateReferralToGroup throws an error if referral is in a closed state`() {
      val group = testGroupHelper.createGroup()
      val referral =
        testReferralHelper.createReferralAndUpdateStatus(referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription())

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )
      assertThat(exception.userMessage).isEqualTo("Bad request: Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    @Test
    fun `allocateReferralToGroup throws an error if referral already allocated to a group`() {
      val group = testGroupHelper.createGroup()
      val referral =
        testReferralHelper.createReferralAndUpdateStatus(referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription())
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.CONFLICT.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )
      assertThat(exception.userMessage).isEqualTo("Conflict: Referral with id ${referral.id} is already allocated to a group: ${group.code}")
    }

    @Test
    fun `allocateReferralToGroup will only add PoP to core group sessions and not any individual scheduled sessions`() {
      // Given
      val theCrnNumber = randomUppercaseString()
      val facilitators = listOf(CreateGroupTeamMemberFactory().produce())
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(theCrnNumber, 1)
      nDeliusApiStubs.stubPersonalDetailsResponse()
      oasysApiStubs.stubSuccessfulPniResponse(theCrnNumber)
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      val group = testGroupHelper.createGroup()
      val alreadyAllocatedReferral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, alreadyAllocatedReferral)
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = group.sessions.find { it.sessionType == SessionType.ONE_TO_ONE }!!.moduleSessionTemplate.id!!,
        referralIds = listOf(alreadyAllocatedReferral.id!!),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )
      scheduleService.scheduleIndividualSessionAndReturnResponse(group.id!!, scheduleSessionRequest)
      // When

      val referral = testReferralHelper.createReferral(crn = theCrnNumber, personName = "the-forename the-surname")

      val allocateToGroupRequest = AllocateToGroupRequest(additionalDetails = "The additional details for the test")
      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = allocateToGroupRequest,
        returnType = object : ParameterizedTypeReference<AllocateToGroupResponse>() {},
      )
      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(response.message).isEqualTo("the-forename the-surname was added to this group. Their referral status is now Scheduled.")

      val currentGroupMembership = foundReferral.programmeGroupMemberships.first()
      assertThat(currentGroupMembership.programmeGroup.id).isEqualTo(group.id)
      val nonPlaceHolderIndividualSessions =
        currentGroupMembership.programmeGroup.sessions.filter { !it.isPlaceholder && it.sessionType == SessionType.ONE_TO_ONE }
      assertThat(nonPlaceHolderIndividualSessions.map { sessionEntity -> sessionEntity.attendees.map { it.personName } }).isNotIn(
        foundReferral.personName,
      )

      wiremock.verify(3, postRequestedFor(urlEqualTo("/appointments")))
      val nDeliusAppointments = nDeliusAppointmentRepository.findAll()
      assertThat(nDeliusAppointments.size).isEqualTo(43)
      assertThat(foundReferral.eventId).isIn(nDeliusAppointments.mapNotNull { it.referral.eventId })
      assertThat(foundReferral).isNotNull
      assertThat(foundReferral.id).isEqualTo(referral.id)
      assertThat(foundReferral.programmeGroupMemberships).hasSize(1)
      assertThat(foundReferral.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)
      // Check that we have added the PoP to the session attendees list
      val attendeeList =
        foundReferral.programmeGroupMemberships.first().programmeGroup.sessions.flatMap { sessionEntity -> sessionEntity.attendees.map { it.personName } }
      assertThat(attendeeList).allMatch { attendeeList.contains(it) }
    }
  }

  @Nested
  @DisplayName("Remove from Programme group")
  inner class RemoveFromProgrammeGroup {

    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()

    @Test
    fun `removeReferralFromGroup can successfully remove a referral from a group`() {
      // Given
      val group = testGroupHelper.createGroup()

      val referral = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()

      // Allocate the referral to the group first
      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "THE_ALLOCATED_TO_GROUP_BY_ID",
        "any additional details",
      )

      val removeFromGroupRequest = RemoveFromGroupRequest(
        referralStatusDescriptionId = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id,
        additionalDetails = "The additional details for the removal",
      )

      // Add a future session for the group and the referral
      val session = sessionRepository.save(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(buildingChoicesTemplate.modules.first().sessionTemplates.first())
          .withStartsAt(LocalDateTime.now().plusDays(1))
          .withEndsAt(LocalDateTime.now().plusDays(1).plusHours(1))
          .produce(),
      )
      session.attendees.add(
        AttendeeFactory()
          .withReferral(referral)
          .withSession(session)
          .produce(),
      )
      sessionRepository.save(session)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/remove/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = removeFromGroupRequest,
        returnType = object : ParameterizedTypeReference<RemoveFromGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(response.message).contains("Future scheduled sessions for this PoP have been deleted in nDelius and the Digital Service.")
      assertThat(foundReferral).isNotNull
      assertThat(foundReferral.id).isEqualTo(referral.id)

      val currentlyAllocatedGroup = programmeGroupMembershipService.getCurrentlyAllocatedGroup(foundReferral)
      assertThat(currentlyAllocatedGroup).isNull()

      val currentStatusHistory = referralService.getCurrentStatusHistory(foundReferral)
      assertThat(currentStatusHistory).isNotNull
      assertThat(currentStatusHistory!!.referralStatusDescription.description).isEqualTo("Awaiting allocation")
      assertThat(currentStatusHistory.additionalDetails).isEqualTo("The additional details for the removal")

      // Check that all future sessions associated with group and person are removed
      val remainingAttendeeNames = foundReferral.programmeGroupMemberships.first().programmeGroup.sessions
        .flatMap { session -> session.attendees.map { it.personName } }

      assertThat(remainingAttendeeNames).doesNotContain(foundReferral.personName)
      val foundSession = sessionRepository.findByIdOrNull(session.id!!)
      assertThat(foundSession).isNull()
      // Validate that associated ndelius appointments have been removed from the DB
      val foundNdeliusAppointment = nDeliusAppointmentRepository.findBySessionId(session.id!!)
      assertThat(foundNdeliusAppointment).isNull()
    }

    @Test
    fun `should remove referral from group and leave past session attendance records intact`() {
      // Given
      val groupCode = "AAA111"
      val group = testGroupHelper.createGroup(groupCode)

      val referral = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getOnProgrammeStatusDescription(),
      )

      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "THE_ALLOCATED_TO_GROUP_BY_ID",
        "any additional details",
      )

      val statusHistory =
        ReferralStatusHistoryEntity(
          referral = referral,
          referralStatusDescription = referralStatusDescriptionRepository.getOnProgrammeStatusDescription(),
          additionalDetails = "additional details",
          createdBy = "createdBy",
        )

      referral.statusHistories.add(statusHistory)
      referralRepository.save(referral)

      val removeFromGroupRequest = RemoveFromGroupRequest(
        referralStatusDescriptionId = referralStatusDescriptionRepository.getReturnToCourtStatusDescription().id,
        additionalDetails = "The additional details for the removal",
      )

      // Add a past session for the group and the referral
      val session = sessionRepository.save(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(buildingChoicesTemplate.modules.first().sessionTemplates.first())
          .withStartsAt(LocalDateTime.now().minusDays(1))
          .withEndsAt(LocalDateTime.now().minusDays(1).plusHours(1))
          .produce(),
      )
      session.attendees.add(
        AttendeeFactory()
          .withReferral(referral)
          .withSession(session)
          .produce(),
      )
      session.ndeliusAppointments.add(
        NDeliusAppointmentEntityFactory()
          .withNdeliusAppointmentId(UUID.randomUUID())
          .withReferral(referral = referral)
          .withSession(session)
          .produce(),
      )
      sessionRepository.save(session)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/remove/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = removeFromGroupRequest,
        returnType = object : ParameterizedTypeReference<RemoveFromGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(response.message).contains("Future scheduled sessions for this PoP have been deleted in nDelius and the Digital Service.")
      assertThat(foundReferral).isNotNull
      assertThat(foundReferral.id).isEqualTo(referral.id)

      val currentlyAllocatedGroup = programmeGroupMembershipService.getCurrentlyAllocatedGroup(foundReferral)
      assertThat(currentlyAllocatedGroup).isNull()

      val currentStatusHistory = referralService.getCurrentStatusHistory(foundReferral)
      assertThat(currentStatusHistory).isNotNull
      assertThat(currentStatusHistory!!.referralStatusDescription.description).isEqualTo("Return to court")
      assertThat(currentStatusHistory.additionalDetails).isEqualTo("The additional details for the removal")

      // validate that past session attendances are left intact for this referral
      val remainingAttendeeNames = foundReferral.programmeGroupMemberships.first().programmeGroup.sessions
        .flatMap { session -> session.attendees.map { it.personName } }

      assertThat(remainingAttendeeNames).contains(foundReferral.personName)
    }

    @Test
    fun `should remove referral from group and delete any sessions which are 'One-to-One && NOT placeholder'`() {
      // Given
      val group = testGroupHelper.createGroup()

      val referral = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()

      // Allocate the referral to the group first
      testGroupHelper.allocateToGroup(group, referral)

      val removeFromGroupRequest = RemoveFromGroupRequest(
        referralStatusDescriptionId = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id,
        additionalDetails = "The additional details for the removal",
      )

      val oneToOneSession =
        buildingChoicesTemplate.modules.first().sessionTemplates.find { it.sessionType == SessionType.ONE_TO_ONE }!!

      // Add a future scheduled individual session for the referral
      val session = sessionRepository.save(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(oneToOneSession)
          .withStartsAt(LocalDateTime.now().plusDays(1))
          .withEndsAt(LocalDateTime.now().plusDays(1).plusHours(1))
          .withIsPlaceholder(false)
          .produce(),
      )
      session.attendees.add(
        AttendeeFactory()
          .withReferral(referral)
          .withSession(session)
          .produce(),
      )
      sessionRepository.save(session)

      // When
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/remove/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = removeFromGroupRequest,
        returnType = object : ParameterizedTypeReference<RemoveFromGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      // Check that all future sessions associated with group and person are removed
      val remainingAttendeeNames = foundReferral.programmeGroupMemberships.first().programmeGroup.sessions
        .flatMap { session -> session.attendees.map { it.personName } }

      assertThat(remainingAttendeeNames).doesNotContain(foundReferral.personName)
      // Validate our future individual non placeholder session has been removed
      val foundSession = sessionRepository.findByIdOrNull(session.id!!)
      assertThat(foundSession).isNull()
      // Validate that associated ndelius appointments have been removed from the DB
      val foundNdeliusAppointment = nDeliusAppointmentRepository.findBySessionId(session.id!!)
      assertThat(foundNdeliusAppointment).isNull()
    }

    @Test
    fun `should remove referral from group and delete any sessions which are 'Catch-ups AND they were the only attendee'`() {
      // Given
      val group = testGroupHelper.createGroup()

      val referral = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()

      // Allocate the referral to the group first
      testGroupHelper.allocateToGroup(group, referral)

      val removeFromGroupRequest = RemoveFromGroupRequest(
        referralStatusDescriptionId = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id,
        additionalDetails = "The additional details for the removal",
      )

      val groupSession =
        buildingChoicesTemplate.modules
          .firstNotNullOf { module ->
            module.sessionTemplates.find { it.sessionType == SessionType.GROUP }
          }

      // Add a future scheduled group catchup session for the referral
      val session = sessionRepository.save(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(groupSession)
          .withStartsAt(LocalDateTime.now().plusDays(1))
          .withEndsAt(LocalDateTime.now().plusDays(1).plusHours(1))
          .withIsPlaceholder(false)
          .withIsCatchup(true)
          .produce(),
      )

      session.attendees.add(
        AttendeeFactory()
          .withReferral(referral)
          .withSession(session)
          .produce(),
      )
      sessionRepository.save(session)

      // When
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/remove/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = removeFromGroupRequest,
        returnType = object : ParameterizedTypeReference<RemoveFromGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      // Check that all future sessions associated with group and person are removed
      val remainingAttendeeNames = foundReferral.programmeGroupMemberships.first().programmeGroup.sessions
        .flatMap { session -> session.attendees.map { it.personName } }

      assertThat(remainingAttendeeNames).doesNotContain(foundReferral.personName)
      // Validate our future group catchup session has been removed
      val foundSession = sessionRepository.findByIdOrNull(session.id!!)
      assertThat(foundSession).isNull()
      // Validate that associated ndelius appointments have been removed from the DB
      val foundNdeliusAppointment = nDeliusAppointmentRepository.findBySessionId(session.id!!)
      assertThat(foundNdeliusAppointment).isNull()
    }

    @Test
    fun `should remove referral from group and NOT delete any sessions which are 'Catch-ups AND there were other attendees'`() {
      // Given
      val group = testGroupHelper.createGroup()

      val referralToRemove = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )
      val referralNotToRemove = testReferralHelper.createReferralAndUpdateStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()

      // Allocate the referral to the group first
      testGroupHelper.allocateToGroup(group, referralToRemove)
      testGroupHelper.allocateToGroup(group, referralNotToRemove)

      val removeFromGroupRequest = RemoveFromGroupRequest(
        referralStatusDescriptionId = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id,
        additionalDetails = "The additional details for the removal",
      )

      val groupSession =
        buildingChoicesTemplate.modules
          .firstNotNullOf { module ->
            module.sessionTemplates.find { it.sessionType == SessionType.GROUP }
          }

      // Add a future scheduled group catchup session for the referral
      val session = sessionRepository.save(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(groupSession)
          .withStartsAt(LocalDateTime.now().plusDays(1))
          .withEndsAt(LocalDateTime.now().plusDays(1).plusHours(1))
          .withIsPlaceholder(false)
          .withIsCatchup(true)
          .produce(),
      )

      session.attendees.add(
        AttendeeFactory()
          .withReferral(referralToRemove)
          .withSession(session)
          .produce(),
      )
      session.attendees.add(
        AttendeeFactory()
          .withReferral(referralNotToRemove)
          .withSession(session)
          .produce(),

      )
      sessionRepository.save(session)

      // When
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/remove/${referralToRemove.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = removeFromGroupRequest,
        returnType = object : ParameterizedTypeReference<RemoveFromGroupResponse>() {},
      )

      val foundReferral = referralRepository.findByIdOrNull(referralToRemove.id!!)!!

      // Then
      // Check that all future sessions associated with group and person are removed
      val remainingAttendeeNames = foundReferral.programmeGroupMemberships.first().programmeGroup.sessions
        .flatMap { session -> session.attendees.map { it.personName } }

      assertThat(remainingAttendeeNames).doesNotContain(foundReferral.personName)
      // Validate our future group catchup session has not been removed
      val foundSession = sessionRepository.findByIdOrNull(session.id!!)
      assertThat(foundSession).isNotNull
      assertThat(foundSession!!.attendees.map { it.personName }).doesNotContain(referralToRemove.personName)
      // Validate that associated ndelius appointments have been removed from the DB
      val foundNdeliusAppointment = nDeliusAppointmentRepository.findBySessionId(session.id!!)
      assertThat(foundNdeliusAppointment).isNull()
    }
  }

  @Nested
  @DisplayName("Create a Programme group")
  inner class CreateProgrammeGroup {
    val createGroupTeamMemberFactory = CreateGroupTeamMemberFactory()
    val createGroupRequestFactory = CreateGroupRequestFactory()

    @Test
    fun `create group with all parameters and return 200 when it doesn't already exist`() {
      val teamMember1 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER)
      val teamMember2 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember3 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember4 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.COVER_FACILITATOR)

      val body = CreateGroupRequestFactory().produce(
        "TEST_GROUP",
        ProgrammeGroupCohort.GENERAL,
        ProgrammeGroupSexEnum.MALE,
        LocalDate.parse("2025-01-01"),
        setOf(CreateGroupSessionSlot(DayOfWeek.MONDAY, 1, 1, AmOrPm.AM)),
        "TEST_PDU",
        "CODE_PDU",
        "LOCATION_NAME",
        "LOCATION_CODE",
        listOf(teamMember1, teamMember2, teamMember3, teamMember4),
      )
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.code).isEqualTo(body.groupCode)
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      assertThat(createdGroup.isLdc).isFalse
      assertThat(createdGroup.sex).isEqualTo(ProgrammeGroupSexEnum.MALE)
      assertThat(createdGroup.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(createdGroup.earliestPossibleStartDate).isEqualTo(LocalDate.parse("2025-01-01"))
      assertThat(createdGroup.programmeGroupSessionSlots).hasSize(1)
      assertThat(createdGroup.programmeGroupSessionSlots.first().dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
      assertThat(createdGroup.programmeGroupSessionSlots.first().startTime).isEqualTo(LocalTime.of(1, 1))
      assertThat(createdGroup.treatmentManager?.ndeliusPersonCode).isEqualTo(teamMember1.facilitatorCode)
      assertThat(createdGroup.groupFacilitators).hasSize(3)
      assertThat(createdGroup.accreditedProgrammeTemplate).isNotNull
      assertThat(createdGroup.accreditedProgrammeTemplate!!.name).isEqualTo("Building Choices")
    }

    @Test
    fun `create group and assign code and return 200 when it doesn't already exist`() {
      val body = createGroupRequestFactory.produce(groupCode = "TEST_GROUP")
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.code).isEqualTo(body.groupCode)
      assertThat(createdGroup.id).isNotNull
    }

    @Test
    fun `create group with code and return CONFLICT when it already exists within the region`() {
      val group = CreateGroupRequestFactory().produce(groupCode = "TEST_GROUP")
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = group,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val body = createGroupRequestFactory.produce("TEST_GROUP")
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        body = body,
        expectedResponseStatus = HttpStatus.CONFLICT.value(),
      )
      assertThat(response.userMessage).isEqualTo("Conflict: Programme group with code TEST_GROUP already exists in region")
    }

    @Test
    fun `create group and assign correct cohort and return 200 when it doesn't already exist`() {
      val body = createGroupRequestFactory.produce(cohort = ProgrammeGroupCohort.SEXUAL_LDC)
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(createdGroup.isLdc).isTrue
    }

    @Test
    fun `create group and assign correct sex and return 200 when it doesn't already exist`() {
      val body = createGroupRequestFactory.produce(sex = ProgrammeGroupSexEnum.MALE)
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.sex).isEqualTo(ProgrammeGroupSexEnum.MALE)
    }

    @Test
    fun `create group and assign correct pdu and delivery locations and return 200 when it doesn't already exist`() {
      val body = createGroupRequestFactory.produce(
        pduCode = randomUppercaseString(),
        pduName = randomWord(1..2).toString(),
        deliveryLocationCode = randomUppercaseString(),
        deliveryLocationName = randomWord(1..2).toString(),
      )
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.probationDeliveryUnitCode).isEqualTo(body.pduCode)
      assertThat(createdGroup.probationDeliveryUnitName).isEqualTo(body.pduName)
      assertThat(createdGroup.deliveryLocationCode).isEqualTo(body.deliveryLocationCode)
      assertThat(createdGroup.deliveryLocationName).isEqualTo(body.deliveryLocationName)
    }

    @Test
    fun `create group and assign correct team members and facilitators and return 200 when it doesn't already exist`() {
      val teamMember1 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER)
      val teamMember2 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember3 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember4 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.COVER_FACILITATOR)
      val body = createGroupRequestFactory.produce(
        teamMembers = listOf(teamMember1, teamMember2, teamMember3, teamMember4),
      )
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.treatmentManager?.ndeliusPersonCode).isEqualTo(teamMember1.facilitatorCode)
      val expectedFacilitatorCodes = listOf(
        teamMember2.facilitatorCode,
        teamMember3.facilitatorCode,
        teamMember4.facilitatorCode,
      )

      val actualFacilitatorCodes = createdGroup.groupFacilitators.map { it.facilitator.ndeliusPersonCode }

      assertThat(actualFacilitatorCodes)
        .containsExactlyInAnyOrderElementsOf(expectedFacilitatorCodes)
    }

    @Test
    fun `create group and return 400 when it doesn't already exist and there is no treatment manager`() {
      val teamMember1 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember2 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)
      val teamMember3 =
        createGroupTeamMemberFactory.produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.COVER_FACILITATOR)
      val body = createGroupRequestFactory.produce(
        teamMembers = listOf(teamMember1, teamMember2, teamMember3),
      )
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        body = body,
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
      assertThat(response.developerMessage).isEqualTo("At least one treatment manager must be specified for a programme group")
    }

    @Test
    fun `create Group and assign slots and return 200 when it doesn't already exist`() {
      val slots = setOf(
        CreateGroupSessionSlot(DayOfWeek.MONDAY, 1, 1, AmOrPm.AM),
        CreateGroupSessionSlot(DayOfWeek.TUESDAY, 1, 1, AmOrPm.PM),
      )
      val body = createGroupRequestFactory.produce(createGroupSessionSlot = slots)
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)
      assertThat { createdGroup }.isNotNull
      assertThat(createdGroup?.code).isEqualTo(body.groupCode)
      assertThat(createdGroup?.programmeGroupSessionSlots).size().isEqualTo(2)

      assertThat(createdGroup?.programmeGroupSessionSlots).allMatch {
        (it.dayOfWeek == DayOfWeek.MONDAY && it.startTime == LocalTime.of(1, 1)) ||
          (it.dayOfWeek == DayOfWeek.TUESDAY && it.startTime == LocalTime.of(13, 1))
      }
    }

    @Test
    fun `create group and create sessions`() {
      val body = CreateGroupRequestFactory().produce()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup).isNotNull
      assertThat(createdGroup.sessions).isNotEmpty()
    }

    @Test
    fun `create group returns response body with id and successMessage`() {
      // Given
      val groupCode = "TEST"
      val body = createGroupRequestFactory.produce(groupCode = groupCode)
      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        returnType = object : ParameterizedTypeReference<uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Then
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(response.id).isEqualTo(createdGroup.id)
      assertThat(response.successMessage).isEqualTo("Group ${body.groupCode} created.")
      assertThat(response.successMessage).contains(groupCode)
    }

    @Test
    fun `return 401 when unauthorised`() {
      val body = createGroupRequestFactory.produce()
      webTestClient
        .method(HttpMethod.POST)
        .uri("/group")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `create multiple groups should not fail`() {
      // This test is in place as we are currently seeing an issue that produces the error
      // More than one row with the given identifier was found: 3442732f-9a0d-4981-8f9e-54e622e72211, for class: uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
      repeat(10) { iteration ->
        val body = createGroupRequestFactory.produce()

        performRequestAndExpectStatus(
          httpMethod = HttpMethod.POST,
          uri = "/group",
          body = body,
          expectedResponseStatus = HttpStatus.CREATED.value(),
        )

        val createdGroup = programmeGroupRepository.findByCode(body.groupCode)
          ?: error("Iteration $iteration: group not found")

        assertThat(createdGroup.code).isEqualTo(body.groupCode)
        assertThat(createdGroup.id).isNotNull
      }
    }
  }

  @Nested
  @DisplayName("Get group by region")
  @WithMockAuthUser("TEST_USER")
  inner class GetGroupInRegion {
    @Test
    fun `return 200 and group when exists in region`() {
      val groupCode = "AAA111"
      val groupRegion = "WIREMOCKED REGION"
      val group = ProgrammeGroupFactory().withCode(groupCode).withRegionName(groupRegion).produce()
      testDataGenerator.createGroup(group)

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/group/${group.code}/details",
        returnType = object : ParameterizedTypeReference<Group>() {},
      )

      assertThat(response.code).isEqualTo(groupCode)
      assertThat(response.regionName).isEqualTo(groupRegion)
    }

    @Test
    fun `return 200 and empty body when group does not exist in region`() {
      val groupCode = "AAA111"
      val groupRegion = "TEST REGION"
      nDeliusApiStubs.stubUserTeamsResponse(
        "TEST_USER",
        NDeliusUserTeamsFactory().withSingleTeam(
          regionDescription = "OTHER REGION",
        ).produce(),
      )
      val group = ProgrammeGroupFactory().withCode(groupCode).withRegionName(groupRegion).produce()
      testDataGenerator.createGroup(group)

      performRequestAndExpectStatusNoBody(
        httpMethod = HttpMethod.GET,
        uri = "/group/${group.code}/details",
        expectedResponseStatus = HttpStatus.OK.value(),
      )
    }

    @Test
    fun `return 401 when unauthorised`() {
      val body = ProgrammeGroupFactory().produce()
      webTestClient
        .method(HttpMethod.GET)
        .uri("/group/TEST/details")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get Pdus in User region")
  inner class GetPduInUserRegion {
    @Test
    fun `return 200 and list of pdus when exist in region`() {
      val pdu = NDeliusPduWithTeamFactory().produce()
      val members = NDeliusRegionWithMembersFactory().produce(pdus = listOf(pdu, pdu))

      nDeliusApiStubs.stubRegionWithMembersResponse("REGION001", members)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/pdus-for-user-region",
        returnType = object : ParameterizedTypeReference<List<CodeDescription>>() {},
      )

      assertThat(response).hasSize(2)
      assertThat(response.first().description).isEqualTo(pdu.description)
      assertThat(response.first().code).isEqualTo(pdu.code)
    }

    @Test
    fun `return 401 when unauthorised`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/pdus-for-user-region")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get office locations in PDU")
  inner class GetOfficeLocationsInPdu {
    @Test
    fun `return 200 and list of office locations when exist in PDU`() {
      val pdu = NDeliusApiProbationDeliveryUnitWithOfficeLocationsFactory().produce()
      nDeliusApiStubs.stubRegionPduOfficeLocationsResponse(pdu.code, pdu)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/office-locations-for-pdu/${pdu.code}",
        returnType = object : ParameterizedTypeReference<List<CodeDescription>>() {},
      )

      assertThat(response).hasSize(3)
      assertThat(response).containsAll(pdu.officeLocations)
    }

    @Test
    fun `return 401 when unauthorised`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/office-locations-for-pdu/RANDOM_PDU")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get team members in PDU")
  inner class GetTeamMembersInPdu {
    @Test
    fun `return 200 and list of pdus when exist in region`() {
      val members = listOf(NDeliusUserTeamMembersFactory().produce(), NDeliusUserTeamMembersFactory().produce())
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = members))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)

      val members2 = listOf(NDeliusUserTeamMembersFactory().produce(), NDeliusUserTeamMembersFactory().produce())
      val teams2 = listOf(NDeliusUserTeamWithMembersFactory().produce(members = members2))
      val pdu2 = NDeliusPduWithTeamFactory().produce(team = teams2)
      val regionWithMembers =
        NDeliusRegionWithMembersFactory().produce(pdus = listOf(pdu, pdu2), code = "WIREMOCKED REGION")

      nDeliusApiStubs.stubRegionWithMembersResponse("REGION001", regionWithMembers)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/region/members",
        returnType = object : ParameterizedTypeReference<List<UserTeamMember>>() {},
      )

      assertThat(response).hasSize(members.size + members2.size)
      assertThat(response.first().personName).isEqualTo(members.first().name.getNameAsString())
      assertThat(response.first().personCode).isEqualTo(members.first().code)
    }

    @Test
    fun `return 200 and empty list when no pdus in region`() {
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(pdus = listOf())

      nDeliusApiStubs.stubRegionWithMembersResponse("REGION001", regionWithMembers)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/region/members",
        returnType = object : ParameterizedTypeReference<List<UserTeamMember>>() {},
      )

      assertThat(response).hasSize(0)
    }

    @Test
    fun `return 401 when unauthorised`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/region/members")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Schedule Session")
  @WithMockAuthUser("AUTH_ADM")
  inner class ScheduleSession {
    val facilitators = listOf(CreateGroupTeamMemberFactory().produce())

    @Test
    fun `should return 201 when scheduling a one-to-one session with valid data`() {
      // Given
      initialiseReferrals()
      val referral = referrals.first()
      val group = testGroupHelper.createGroup()
      testGroupHelper.allocateToGroup(group, referral)
      val sessionTemplate = group.accreditedProgrammeTemplate!!.modules.first().sessionTemplates.first()

      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = sessionTemplate.id!!,
        referralIds = listOf(referral.id!!),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<String>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).isEqualTo("${sessionTemplate.name} for ${referral.personName} has been added.")
      val retrievedSession =
        sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(sessionTemplate.id!!, group.id!!)
          .sortedByDescending { it.createdAt }.first()
      assertThat(retrievedSession.sessionFacilitators.first().facilitator.personName)
        .isEqualTo("Default facilitator name")
      assertThat(retrievedSession.locationName).isEqualTo(group.deliveryLocationName)
      assertThat(retrievedSession.attendees).hasSize(1)
      assertThat(retrievedSession.attendees[0].personName).isEqualTo(referral.personName)
      wiremock.verify(2, postRequestedFor(urlEqualTo("/appointments")))
      assertThat(nDeliusAppointmentRepository.findBySessionId(retrievedSession.id!!)!!).isNotNull
    }

    @Test
    fun `should return 201 when scheduling a one-to-one catch-up session with valid data`() {
      // Given
      initialiseReferrals()
      val referral = referrals.first()
      val group = testGroupHelper.createGroup()
      testGroupHelper.allocateToGroup(group, referral)
      val sessionTemplate = group.accreditedProgrammeTemplate!!.modules.first().sessionTemplates.first()

      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = sessionTemplate.id!!,
        referralIds = listOf(referral.id!!),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
        sessionScheduleType = SessionScheduleType.CATCH_UP,
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<String>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).isEqualTo("${sessionTemplate.name} catch-up for ${referral.personName} has been added.")
      val retrievedSession =
        sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(sessionTemplate.id!!, group.id!!)
          .sortedByDescending { it.createdAt }.first()
      assertThat(retrievedSession.sessionFacilitators.first().facilitator.personName)
        .isEqualTo("Default facilitator name")
      assertThat(retrievedSession.locationName).isEqualTo(group.deliveryLocationName)
      assertThat(retrievedSession.attendees).hasSize(1)
      assertThat(retrievedSession.attendees[0].personName).isEqualTo(referral.personName)
      assertThat(retrievedSession.isCatchup).isTrue
      wiremock.verify(2, postRequestedFor(urlEqualTo("/appointments")))
      assertThat(nDeliusAppointmentRepository.findBySessionId(retrievedSession.id!!)!!).isNotNull
    }

    @Test
    fun `should return 400 when referralIds is empty`() {
      // Given
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = UUID.randomUUID(),
        referralIds = emptyList(),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )
      val group = ProgrammeGroupFactory().produce()

      // When / Then
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
    }

    @Test
    fun `should return 400 when facilitators is empty`() {
      // Given
      val group = ProgrammeGroupFactory().produce()
      val referralId = UUID.randomUUID()
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = UUID.randomUUID(),
        referralIds = listOf(referralId),
        facilitators = emptyList(),
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When / Then
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
    }

    @Test
    fun `should return 400 when hour is out of range`() {
      // Given
      val referralId = UUID.randomUUID()
      val group = ProgrammeGroupFactory().produce()
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = UUID.randomUUID(),
        referralIds = listOf(referralId),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 13, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When / Then
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
    }

    @Test
    fun `should return 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()
      val referralId = UUID.randomUUID()
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = UUID.randomUUID(),
        referralIds = listOf(referralId),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When & Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/$nonExistentGroupId/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).contains("Group with id: $nonExistentGroupId could not be found")
    }

    @Test
    fun `should return 404 when session template does not exist`() {
      // Given
      val nonExistentTemplateId = UUID.randomUUID()
      initialiseReferrals()
      val referral = referrals.first()
      val group = testGroupHelper.createGroup()
      testGroupHelper.allocateToGroup(group, referral)
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = nonExistentTemplateId,
        referralIds = listOf(referral.id!!),
        facilitators = facilitators,
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When & Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).contains("Session template with id: $nonExistentTemplateId could not be found")
    }

    @Test
    fun `should return 201 and create facilitator when facilitator does not exist in database`() {
      // Given
      initialiseReferrals()
      val referral = referrals.first()
      val group = testGroupHelper.createGroup()
      testGroupHelper.allocateToGroup(group, referral)
      val sessionTemplate = group.accreditedProgrammeTemplate!!.modules.first().sessionTemplates.first()

      val nonExistentFacilitator = CreateGroupTeamMemberFactory()
        .withFacilitator("Non existent Facilitator")
        .withFacilitatorCode("Non existent code")
        .produce()

      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = sessionTemplate.id!!,
        referralIds = listOf(referral.id!!),
        facilitators = listOf(nonExistentFacilitator),
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      // When & Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/session/schedule",
        body = scheduleSessionRequest,
        returnType = object : ParameterizedTypeReference<String>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      assertThat(response).isNotNull
      assertThat(response).isEqualTo("${sessionTemplate.name} for ${referral.personName} has been added.")
      val retrievedSession =
        sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(sessionTemplate.id!!, group.id!!)
          .sortedByDescending { it.createdAt }.first()
      assertThat(retrievedSession.sessionFacilitators).extracting("facilitator.personName")
        .contains("Non existent Facilitator")
      // This is 2 as we made a call when we allocated to a group as part of test setup
      wiremock.verify(2, postRequestedFor(urlEqualTo("/appointments")))
    }

    @Test
    fun `should return 401 when unauthorised`() {
      val group = ProgrammeGroupFactory().produce()
      val scheduleSessionRequest = ScheduleSessionRequest(
        sessionTemplateId = UUID.randomUUID(),
        referralIds = listOf(UUID.randomUUID()),
        facilitators = listOf(
          CreateGroupTeamMember(
            facilitator = "Test Facilitator",
            facilitatorCode = "FAC001",
            teamName = "Test Team",
            teamCode = "TEAM001",
            teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER,
          ),
        ),
        startDate = LocalDate.of(2025, 1, 1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )

      webTestClient
        .post()
        .uri("/group/${group.id}/session/schedule")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(scheduleSessionRequest)
        .exchange()
        .expectStatus().isUnauthorized
    }
  }

  @Nested
  @DisplayName("Get session templates for group module")
  @WithMockAuthUser("AUTH_ADM")
  inner class GetSessionTemplatesForGroupModule {

    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()

    @Test
    fun `Successfully retrieves session templates for a module using V56 migration data`() {
      // Given
      stubAuthTokenEndpoint()
      val modules = buildingChoicesTemplate.modules
      assertThat(modules).isNotEmpty

      // Use the "Getting Started" module (module_number = 2) which has 2 sessions
      val gettingStartedModule = modules.find { it.name == "Getting started" }
      assertThat(gettingStartedModule).isNotNull

      // Create a test group linked to Building Choices
      val group = ProgrammeGroupFactory()
        .withCode("TEST_GROUP_001")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      group.accreditedProgrammeTemplate = buildingChoicesTemplate
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/${gettingStartedModule!!.id}/schedule-session-type",
        expectedResponseStatus = HttpStatus.OK.value(),
        returnType = object : ParameterizedTypeReference<ScheduleSessionTypeResponse>() {},
        body = " ",
      )

      // Then
      assertThat(response.sessionTemplates).hasSize(4)
      assertThat(response.sessionTemplates.map { it.name })
        .containsExactly(
          "Getting started 1 catch-up",
          "Getting started 2 catch-up",
          "Getting started one-to-one",
          "Getting started one-to-one catch-up",
        )
      assertThat(response.sessionTemplates.map { it.sessionType })
        .containsExactly(
          SessionType.GROUP,
          SessionType.GROUP,
          SessionType.ONE_TO_ONE,
          SessionType.ONE_TO_ONE,
        )
      assertThat(response.pageHeading).isEqualTo("Schedule a Getting started session")
    }

    @Test
    fun `Successfully retrieves session templates for a pre group module and formats title correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val modules = buildingChoicesTemplate.modules
      assertThat(modules).isNotEmpty

      val preGroupModule = modules.find { it.name == "Pre-group one-to-ones" }
      assertThat(preGroupModule).isNotNull

      // Create a test group linked to Building Choices
      val group = ProgrammeGroupFactory()
        .withCode("TEST_GROUP_001")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      group.accreditedProgrammeTemplate = buildingChoicesTemplate
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/${preGroupModule!!.id}/schedule-session-type",
        expectedResponseStatus = HttpStatus.OK.value(),
        returnType = object : ParameterizedTypeReference<ScheduleSessionTypeResponse>() {},
        body = " ",
      )

      // Then
      assertThat(response.sessionTemplates).hasSize(2)
      assertThat(response.sessionTemplates.map { it.name })
        .containsExactly("Pre-group one-to-one", "Pre-group one-to-one catch-up")
      assertThat(response.sessionTemplates.map { it.sessionType })
        .containsExactly(SessionType.ONE_TO_ONE, SessionType.ONE_TO_ONE)
      assertThat(response.pageHeading).isEqualTo("Schedule a pre-group one-to-one")
    }

    @Test
    fun `Successfully retrieves session templates for a post programme review module and formats title correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val modules = buildingChoicesTemplate.modules
      assertThat(modules).isNotEmpty

      val postProgrammeReview = modules.find { it.name == "Post-programme reviews" }
      assertThat(postProgrammeReview).isNotNull

      // Create a test group linked to Building Choices
      val group = ProgrammeGroupFactory()
        .withCode("TEST_GROUP_001")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      group.accreditedProgrammeTemplate = buildingChoicesTemplate
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/${postProgrammeReview!!.id}/schedule-session-type",
        expectedResponseStatus = HttpStatus.OK.value(),
        returnType = object : ParameterizedTypeReference<ScheduleSessionTypeResponse>() {},
        body = " ",
      )

      // Then
      assertThat(response.sessionTemplates).hasSize(2)
      assertThat(response.sessionTemplates.map { it.name })
        .containsOnly(
          "Post-programme review",
          "Post-programme review catch-up",
        )
      assertThat(response.sessionTemplates.map { it.sessionType })
        .containsOnly(
          SessionType.ONE_TO_ONE,
          SessionType.ONE_TO_ONE,
        )
      assertThat(response.pageHeading).isEqualTo("Schedule a post-programme review")
    }

    @Test
    fun `Returns 404 when group does not exist`() {
      // Given
      stubAuthTokenEndpoint()
      val nonExistentGroupId = UUID.randomUUID()
      val modules = buildingChoicesTemplate.modules
      val moduleId = modules.first().id!!

      // When/Then
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/module/$moduleId/schedule-session-type",
        body = " ",
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `Returns 404 when module does not exist`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory()
        .withCode("TEST_GROUP_002")
        .withRegionName("WIREMOCKED REGION")
        .withAccreditedProgrammeTemplate(buildingChoicesTemplate)
        .produce()

      testDataGenerator.createGroup(group)

      val nonExistentModuleId = UUID.randomUUID()

      // When / Then
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/$nonExistentModuleId/schedule-session-type",
        body = " ",
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get Schedule Individual Session Details")
  @WithMockAuthUser("AUTH_ADM")
  inner class GetScheduleIndividualSessionDetails {
    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()

    @Test
    fun `returns 200 with facilitators and group members for valid group and module`() {
      initialiseReferrals()
      // Setup nDelius stubs for facilitators
      val members = listOf(
        NDeliusUserTeamMembersFactory().produce(code = "CODE_1", name = FullName("First", null, "Forename")),
        NDeliusUserTeamMembersFactory().produce(code = "CODE_2", name = FullName("Second", null, "Forename")),
      )
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = members))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(
        pdus = listOf(pdu),
        code = "REGION001",
      )
      nDeliusApiStubs.stubRegionWithMembersResponse("REGION001", regionWithMembers)
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      // Create a programme group
      val group = testGroupHelper.createGroup()

      // Allocate referrals to the group
      val referral1 = referrals[0]
      val referral2 = referrals[1]
      programmeGroupMembershipService.allocateReferralToGroup(
        referralId = referral1.id!!,
        groupId = group.id!!,
        allocatedToGroupBy = "AUTH_ADM",
        additionalDetails = "Test allocation",
      )
      programmeGroupMembershipService.allocateReferralToGroup(
        referralId = referral2.id!!,
        groupId = group.id!!,
        allocatedToGroupBy = "AUTH_ADM",
        additionalDetails = "Test allocation",
      )

      // Get a module from the group's template
      val module = buildingChoicesTemplate.modules.first()

      // Make the request
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/${module.id}/schedule-individual-session-details",
        returnType = object : ParameterizedTypeReference<ScheduleIndividualSessionDetailsResponse>() {},
      )

      // Assertions
      assertThat(response.facilitators).hasSize(2)
      assertThat(response.facilitators.map { it.personCode }).containsExactlyInAnyOrder("CODE_1", "CODE_2")

      assertThat(response.groupMembers).hasSize(2)
      assertThat(response.groupMembers.map { it.crn }).containsExactlyInAnyOrder(
        referral1.crn,
        referral2.crn,
      )
      assertThat(response.groupMembers.map { it.name }).containsExactlyInAnyOrder(
        referral1.personName,
        referral2.personName,
      )
      assertThat(response.groupMembers.map { it.referralId }).containsExactlyInAnyOrder(
        referral1.id,
        referral2.id,
      )
    }

    @Test
    fun `returns 404 when group does not exist`() {
      val nonExistentGroupId = UUID.randomUUID()
      val module = buildingChoicesTemplate.modules.first()

      val errorResponse = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/module/${module.id}/schedule-individual-session-details",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
      assertThat(errorResponse.userMessage).contains("Group with id $nonExistentGroupId not found")
    }

    @Test
    fun `returns 404 when module does not exist`() {
      val group = ProgrammeGroupFactory().produce()
      programmeGroupRepository.save(group)

      val nonExistentModuleId = UUID.randomUUID()

      val errorResponse = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/module/$nonExistentModuleId/schedule-individual-session-details",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
      assertThat(errorResponse.userMessage).contains("Module with id $nonExistentModuleId not found")
    }
  }

  @Nested
  @DisplayName("Get modules and sessions for group")
  inner class GetGroupSessions {
    @Test
    fun `return 200 and bff data if successful`() {
      stubAuthTokenEndpoint()
      val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
      val body = CreateGroupRequestFactory().produce(
        createGroupSessionSlot = setOf(slot1),
      )
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val group = programmeGroupRepository.findByCode(body.groupCode)!!

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/sessions",
        returnType = object : ParameterizedTypeReference<ProgrammeGroupModuleSessionsResponse>() {},
      )

      assertThat(response.group).isNotNull
      assertThat(response.modules).isNotNull
      assertThat(response.modules.size).isEqualTo(7)
      assertThat(response.modules.sumOf { it.sessions.count() }).isEqualTo(21)
      assertThat(response.modules.map { it.name }).containsExactly(
        "Pre-group one-to-ones",
        "Getting started",
        "Managing myself",
        "Managing life’s problems",
        "Managing people around me",
        "Bringing it all together",
        "Post-programme reviews",
      )
      response.modules.forEach { module ->
        when (module.name) {
          "Getting started" -> assertThat(module.sessions.map { it.name }).containsExactly(
            "Getting started 1: Introduction to Building Choices",
            "Getting started 2: Understanding myself",
          )

          "Managing myself" -> assertThat(module.sessions.map { it.name }).containsExactly(
            "Managing myself 1: Understanding my feelings",
            "Managing myself 2: Helpful and unhelpful feelings",
            "Managing myself 3: Managing my feelings, part 1",
            "Managing myself 4: Managing my feelings, part 2",
            "Managing myself 5: Understanding my thinking",
            "Managing myself 6: Developing my flexible thinking",
          )

          "Managing life’s problems" -> assertThat(module.sessions.map { it.name }).containsExactly(
            "Managing life’s problems 1: Understanding problems",
            "Managing life’s problems 2: Exploring life’s problems",
            "Managing life’s problems 3: Planning to manage life’s problems",
            "Managing life’s problems 4: Putting it into action",
          )

          "Managing people around me" -> assertThat(module.sessions.map { it.name }).containsExactly(
            "Managing people around me 1: Understanding the people and influences around me",
            "Managing people around me 2: My role in relationships",
            "Managing people around me 3: Relationship skills, part 1",
            "Managing people around me 4: Relationship skills, part 2",
            "Managing people around me 5: Practising our relationship skills",
            "Managing people around me 6: Module skills practice",
          )

          "Bringing it all together" -> assertThat(module.sessions.map { it.name }).containsExactly(
            "Bringing it all together 1: Future me plan",
            "Bringing it all together 2: Future me practice",
            "Bringing it all together 3: Programme completion",
          )
        }
      }
      response.modules.forEach { module ->
        module.sessions.forEach { session ->
          when (session.type) {
            "Group" -> {
              assertThat(session.participants).isEqualTo(listOf("All"))
            }

            "Individual" -> {
              assertThat(session.participants).isNotEqualTo(listOf("All")) // TO be updated when attendees/facilitators tables are added.
            }
          }
        }
      }
    }

    @Test
    fun `return 401 when unauthorised`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${UUID.randomUUID()}/sessions")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get Schedules")
  inner class GetSchedules {

    @AfterEach
    fun tearDown() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `return 401 when unauthorised for schedule`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${UUID.randomUUID()}/schedule-overview")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `returns 404 when group does not exist for schedule`() {
      val nonExistentGroupId = UUID.randomUUID()

      val errorResponse = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/schedule-overview",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
      assertThat(errorResponse.userMessage).contains("Group with id $nonExistentGroupId not found")
    }

    @Test
    fun `returns 200 with complete schedule when group has all session types`() {
      // Given
      accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      val group = testGroupHelper.createGroup()

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/schedule-overview",
        returnType = object : ParameterizedTypeReference<GroupScheduleOverview>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.groupCode).isEqualTo(group.code)
      assertThat(response.sessions).isNotEmpty
    }

    @Test
    fun `schedule from group returns 404 when sessions does not exist for group`() {
      // Given
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory().withCode("EMPTY_SCHED").produce(),
      )

      // When & Then
      performRequestAndExpectStatusNoBody(
        HttpMethod.GET,
        uri = "/bff/group/${group.id}/schedule-overview",
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get sessions for group")
  inner class GetGroupSessionPage {
    @Test
    fun `return 200 and bff data if successful`() {
      // Given
      referrals = testReferralHelper.createReferrals()

      // Create group
      val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
      val body = CreateGroupRequestFactory().produce(
        createGroupSessionSlot = setOf(slot1),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val group = programmeGroupRepository.findByCode(body.groupCode)!!
      group.sessions = group.sessions.filter { !it.isPlaceholder }.toMutableSet()

      // assign a referral to a group
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referrals.first().id}",
        body = AllocateToGroupRequest(
          additionalDetails = "Test allocation",
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val groupWithAllocation = programmeGroupRepository.findByCode(body.groupCode)!!
      groupWithAllocation.sessions = group.sessions.filter { !it.isPlaceholder }.toMutableSet()

      val session = groupWithAllocation.sessions.first()
      val groupMembership = groupWithAllocation.programmeGroupMemberships.first()

      val attendance = SessionAttendanceEntity(
        session = session,
        groupMembership = groupMembership,
        outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().withCode(UAAB).produce(),
        createdAt = LocalDateTime.now(),
      ).apply {
        val note = SessionNotesHistoryEntity(
          attendance = this,
          notes = "Test session notes",
          createdAt = LocalDateTime.now(),
        )
        notesHistory.add(note)
      }

      session.attendances.add(attendance)
      sessionRepository.saveAndFlush(session)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/session/${groupWithAllocation.sessions.first().id}",
        returnType = object : ParameterizedTypeReference<GroupSessionResponse>() {},
      )

      // Then
      assertThat(response.groupCode).isEqualTo(body.groupCode)
      assertThat(response.sessionType).isIn("Group")
      assertThat(response.isCatchup).isFalse
      assertThat(response.pageTitle).isEqualTo("${groupWithAllocation.sessions.first().moduleSessionTemplate.module.name} ${groupWithAllocation.sessions.first().moduleSessionTemplate.sessionNumber}: ${group.sessions.first().moduleSessionTemplate.name}")
      assertThat(response.date).isNotNull
      assertThat(response.time).isEqualTo("9:30am to midday")
      assertThat(response.scheduledToAttend).isEqualTo(groupWithAllocation.sessions.first().attendees.map { it.personName })
      assertThat(response.facilitators).isEqualTo(group.groupFacilitators.map { it.facilitator.personName })
      assertThat(response.attendanceAndSessionNotes).hasSize(1)
      assertThat(response.attendanceAndSessionNotes)
        .filteredOn { it.crn == groupWithAllocation.programmeGroupMemberships.first().crn }
        .hasSize(1)
        .first()
        .extracting("attendance", "sessionNotes")
        .containsExactly("Did not attend", "Test session notes")
    }

    @Test
    fun `return 401 when unauthorised`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${UUID.randomUUID()}/session/${UUID.randomUUID()}")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `return 404 when the programme group is not found`() {
      val groupId = UUID.randomUUID()

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$groupId/session/${UUID.randomUUID()}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )
      assertThat(exception.userMessage).isEqualTo("Not Found: Group with id $groupId not found")
    }

    @Test
    fun `return 404 when the session is not found`() {
      val sessionId = UUID.randomUUID()

      // Create group
      val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
      val body = CreateGroupRequestFactory().produce(
        createGroupSessionSlot = setOf(slot1),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val group = programmeGroupRepository.findByCode(body.groupCode)!!

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/session/$sessionId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )
      assertThat(exception.userMessage).isEqualTo("Not Found: Session with $sessionId not found")
    }

    @Test
    fun `return 200 and bff data with multiple attendances and session notes`() {
      // Given
      initialiseReferrals()
      val referral1 = referrals[0]
      val referral2 = referrals[1]
      val referral3 = referrals[2]

      // Create group
      val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
      val body = CreateGroupRequestFactory().produce(
        createGroupSessionSlot = setOf(slot1),
      )
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val group = programmeGroupRepository.findByCode(body.groupCode)!!

      // Allocate referrals to group
      listOf(referral1, referral2, referral3).forEach { referral ->
        performRequestAndExpectStatus(
          httpMethod = HttpMethod.POST,
          uri = "/group/${group.id}/allocate/${referral.id}",
          body = AllocateToGroupRequest(
            additionalDetails = "Test allocation",
          ),
          expectedResponseStatus = HttpStatus.OK.value(),
        )
      }

      val groupWithAllocation = programmeGroupRepository.findByCode(body.groupCode)!!
      val session = groupWithAllocation.sessions.first { !it.isPlaceholder }

      // Setup attendance and notes for the session
      stubAuthTokenEndpoint()
      val groupMembership1 = groupWithAllocation.programmeGroupMemberships.first { it.referral.id == referral1.id }
      val groupMembership2 = groupWithAllocation.programmeGroupMemberships.first { it.referral.id == referral2.id }

      val attendance1 = SessionAttendanceEntity(
        session = session,
        groupMembership = groupMembership1,
        outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().produce(),
        createdAt = LocalDateTime.now().plusSeconds(2),
      ).apply {
        val note1 = SessionNotesHistoryEntity(
          attendance = this,
          notes = "Notes for referral 1 - initial",
          createdAt = LocalDateTime.now().plusSeconds(1),
        )
        notesHistory.add(note1)
        val note2 = SessionNotesHistoryEntity(
          attendance = this,
          notes = "Notes for referral 1 - latest",
          createdAt = LocalDateTime.now().plusSeconds(2),
        )
        notesHistory.add(note2)
      }

      val attendance2 = SessionAttendanceEntity(
        session = session,
        groupMembership = groupMembership2,
        outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().withCode(UAAB)
          .withDescription("Unacceptable Absence")
          .withAttendance(false).withCompliant(false).produce(),
        createdAt = LocalDateTime.now().plusSeconds(3),
      ).apply {
        val note3 = SessionNotesHistoryEntity(
          attendance = this,
          notes = "Notes for referral 2 - initial",
          createdAt = LocalDateTime.now().plusSeconds(1),
        )
        notesHistory.add(note3)
        val note4 = SessionNotesHistoryEntity(
          attendance = this,
          notes = "Notes for referral 2 - latest",
          createdAt = LocalDateTime.now().plusSeconds(2),
        )
        notesHistory.add(note4)
      }

      val attendance3 = SessionAttendanceEntity(
        session = session,
        groupMembership = groupMembership1,
        outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().withCode(UAAB)
          .withDescription("Unacceptable Absence")
          .withAttendance(false).withCompliant(false).produce(),
        createdAt = LocalDateTime.now().plusSeconds(1),
      )

      session.attendances.addAll(listOf(attendance1, attendance2, attendance3))
      sessionRepository.saveAndFlush(session)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/session/${session.id}",
        returnType = object : ParameterizedTypeReference<GroupSessionResponse>() {},
      )

      // Then
      assertThat(response.groupCode).isEqualTo(body.groupCode)
      assertThat(response.attendanceAndSessionNotes).hasSize(3)

      val notes1 = response.attendanceAndSessionNotes.find { it.crn == referral1.crn }!!
      assertThat(notes1.attendance).isEqualTo("Attended - Complied")
      assertThat(notes1.sessionNotes).isEqualTo("Notes for referral 1 - latest")

      val notes2 = response.attendanceAndSessionNotes.find { it.crn == referral2.crn }!!
      assertThat(notes2.attendance).isEqualTo("Did not attend")
      assertThat(notes2.sessionNotes).isEqualTo("Notes for referral 2 - latest")

      val notes3 = response.attendanceAndSessionNotes.find { it.crn == referral3.crn }!!
      assertThat(notes3.attendance).isEqualTo("To be confirmed")
      assertThat(notes3.sessionNotes).isEqualTo("Not added")
    }
  }

  @Nested
  @DisplayName("Update Programme group")
  inner class UpdateProgrammeGroup {
    val createGroupTeamMemberFactory = CreateGroupTeamMemberFactory()

    @Test
    fun `should return 200 when updating group code`() {
      // Given
      val group = testGroupHelper.createGroup(groupCode = "ORIGINAL_CODE")
      val updateRequest = UpdateGroupRequest(groupCode = "UPDATED_CODE")

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The group code has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.code).isEqualTo("UPDATED_CODE")
    }

    @Test
    fun `should return 200 when updating sex`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newSex = ProgrammeGroupSexEnum.FEMALE
      val updateRequest = UpdateGroupRequest(sex = newSex)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The gender has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.sex).isEqualTo(newSex)
    }

    @Test
    fun `should return 200 when updating cohort from general to sexual`() {
      // Given
      val group = testGroupHelper.createGroup()
      val updateRequest = UpdateGroupRequest(cohort = ProgrammeGroupCohort.SEXUAL)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The cohort has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(updatedGroup.isLdc).isFalse
    }

    @Test
    fun `should return 200 when updating cohort to LDC variant`() {
      // Given
      val group = testGroupHelper.createGroup()
      val updateRequest = UpdateGroupRequest(cohort = ProgrammeGroupCohort.GENERAL_LDC)

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      assertThat(updatedGroup.isLdc).isTrue
    }

    @Test
    fun `should return 200 when updating earliest start date`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newStartDate = LocalDate.now().plusDays(10)
      val updateRequest = UpdateGroupRequest(earliestStartDate = newStartDate, automaticallyRescheduleOtherSessions = false)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The start date has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.earliestPossibleStartDate).isEqualTo(newStartDate)
    }

    @Test
    fun `should return 200 when updating PDU details`() {
      // Given
      val group = testGroupHelper.createGroup()
      val updateRequest = UpdateGroupRequest(
        pduName = "New PDU Name",
        pduCode = "NEW_PDU_CODE",
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The delivery location has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.probationDeliveryUnitName).isEqualTo("New PDU Name")
      assertThat(updatedGroup.probationDeliveryUnitCode).isEqualTo("NEW_PDU_CODE")
    }

    @Test
    fun `should return 200 when updating delivery location`() {
      // Given
      val group = testGroupHelper.createGroup()
      val updateRequest = UpdateGroupRequest(
        deliveryLocationName = "New Location Name",
        deliveryLocationCode = "NEW_LOC_CODE",
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The delivery location has been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.deliveryLocationName).isEqualTo("New Location Name")
      assertThat(updatedGroup.deliveryLocationCode).isEqualTo("NEW_LOC_CODE")
    }

    @Test
    fun `should return 200 when updating session slots`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newSlots = setOf(
        CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 30, AmOrPm.PM),
        CreateGroupSessionSlot(DayOfWeek.FRIDAY, 10, 0, AmOrPm.AM),
      )
      val updateRequest = UpdateGroupRequest(createGroupSessionSlot = newSlots, automaticallyRescheduleOtherSessions = false)

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The days and times have been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.programmeGroupSessionSlots).hasSize(2)
      assertThat(updatedGroup.programmeGroupSessionSlots).anyMatch {
        it.dayOfWeek == DayOfWeek.WEDNESDAY && it.startTime == LocalTime.of(14, 30)
      }
      assertThat(updatedGroup.programmeGroupSessionSlots).anyMatch {
        it.dayOfWeek == DayOfWeek.FRIDAY && it.startTime == LocalTime.of(10, 0)
      }
    }

    @Test
    fun `should return 200 with alternative message when updating earliest start date with automaticallyRescheduleOtherSessions`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newStartDate = LocalDate.now().plusDays(10)
      val updateRequest = UpdateGroupRequest(
        earliestStartDate = newStartDate,
        automaticallyRescheduleOtherSessions = true,
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The start date and schedule have been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.earliestPossibleStartDate).isEqualTo(newStartDate)
    }

    @Test
    fun `should return 200 with alternative message when updating session slots with automaticallyRescheduleOtherSessions`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newSlots = setOf(
        CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 30, AmOrPm.PM),
        CreateGroupSessionSlot(DayOfWeek.FRIDAY, 10, 0, AmOrPm.AM),
      )
      val updateRequest = UpdateGroupRequest(
        createGroupSessionSlot = newSlots,
        automaticallyRescheduleOtherSessions = true,
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The days and times and schedule have been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.programmeGroupSessionSlots).hasSize(2)
      assertThat(updatedGroup.programmeGroupSessionSlots).anyMatch {
        it.dayOfWeek == DayOfWeek.WEDNESDAY && it.startTime == LocalTime.of(14, 30)
      }
      assertThat(updatedGroup.programmeGroupSessionSlots).anyMatch {
        it.dayOfWeek == DayOfWeek.FRIDAY && it.startTime == LocalTime.of(10, 0)
      }
    }

    @Test
    fun `should return 200 when updating team members`() {
      // Given
      val group = testGroupHelper.createGroup()
      val referenceTime = LocalDateTime.now()
      val originalFacilitatorCode = group.groupFacilitators.first().facilitator.ndeliusPersonCode

      val groupSessions = sessionRepository.findByProgrammeGroupId(group.id!!).sortedBy { it.startsAt }
      val pastSession = groupSessions.first()
      pastSession.startsAt = referenceTime.minusDays(1)
      pastSession.endsAt = referenceTime.minusDays(1).plusHours(2)

      val futureSession = groupSessions[1]
      futureSession.startsAt = referenceTime.plusDays(1)
      futureSession.endsAt = referenceTime.plusDays(1).plusHours(2)
      sessionRepository.saveAll(listOf(pastSession, futureSession))

      val newTreatmentManager = createGroupTeamMemberFactory.produceWithRandomValues(
        teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER,
      )
      val newFacilitator = createGroupTeamMemberFactory.produceWithRandomValues(
        teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR,
      )
      val newCoverFacilitator = createGroupTeamMemberFactory.produceWithRandomValues(
        teamMemberType = CreateGroupTeamMemberType.COVER_FACILITATOR,
      )
      val updateRequest = UpdateGroupRequest(
        teamMembers = listOf(newTreatmentManager, newFacilitator, newCoverFacilitator),
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response.successMessage).isEqualTo("The people responsible for the group have been updated.")
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.treatmentManager?.ndeliusPersonCode).isEqualTo(newTreatmentManager.facilitatorCode)
      assertThat(updatedGroup.groupFacilitators).hasSize(2)
      assertThat(updatedGroup.groupFacilitators.map { it.facilitator.ndeliusPersonCode })
        .containsExactlyInAnyOrder(newFacilitator.facilitatorCode, newCoverFacilitator.facilitatorCode)

      val refreshedSessions = sessionRepository.findByProgrammeGroupId(group.id!!)
      val futureSessionFacilitators = refreshedSessions
        .filter { it.startsAt.isAfter(referenceTime) }
        .map { session -> session.sessionFacilitators.map { it.facilitator.ndeliusPersonCode }.toSet() }

      assertThat(futureSessionFacilitators).allSatisfy { facilitatorCodes ->
        assertThat(facilitatorCodes).containsExactlyInAnyOrder(newFacilitator.facilitatorCode, newCoverFacilitator.facilitatorCode)
      }

      val pastSessionFacilitators = refreshedSessions
        .filter { !it.startsAt.isAfter(referenceTime) }
        .map { session -> session.sessionFacilitators.map { it.facilitator.ndeliusPersonCode }.toSet() }

      assertThat(pastSessionFacilitators).allSatisfy { facilitatorCodes ->
        assertThat(facilitatorCodes).contains(originalFacilitatorCode)
        assertThat(facilitatorCodes).doesNotContain(newFacilitator.facilitatorCode, newCoverFacilitator.facilitatorCode)
      }
    }

    @Test
    fun `should return 200 when updating multiple fields at once`() {
      // Given
      val group = testGroupHelper.createGroup()
      val updateRequest = UpdateGroupRequest(
        groupCode = "UPDATED_GROUP_CODE",
        sex = ProgrammeGroupSexEnum.FEMALE,
        cohort = ProgrammeGroupCohort.SEXUAL_LDC,
        earliestStartDate = LocalDate.of(2026, 12, 25),
        pduName = "Updated PDU",
        pduCode = "UPD_PDU",
        deliveryLocationName = "Updated Location",
        deliveryLocationCode = "UPD_LOC",
      )

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.code).isEqualTo("UPDATED_GROUP_CODE")
      assertThat(updatedGroup.sex).isEqualTo(ProgrammeGroupSexEnum.FEMALE)
      assertThat(updatedGroup.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(updatedGroup.isLdc).isTrue
      assertThat(updatedGroup.earliestPossibleStartDate).isEqualTo(LocalDate.of(2026, 12, 25))
      assertThat(updatedGroup.probationDeliveryUnitName).isEqualTo("Updated PDU")
      assertThat(updatedGroup.probationDeliveryUnitCode).isEqualTo("UPD_PDU")
      assertThat(updatedGroup.deliveryLocationName).isEqualTo("Updated Location")
      assertThat(updatedGroup.deliveryLocationCode).isEqualTo("UPD_LOC")
    }

    @Test
    fun `should return 200 and not change fields that are not provided`() {
      // Given
      val group = testGroupHelper.createGroup()
      val originalCode = group.code
      val originalSex = group.sex
      val originalCohort = group.cohort

      val updateRequest = UpdateGroupRequest(
        pduName = "Only PDU Updated",
      )

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.code).isEqualTo(originalCode)
      assertThat(updatedGroup.sex).isEqualTo(originalSex)
      assertThat(updatedGroup.cohort).isEqualTo(originalCohort)
      assertThat(updatedGroup.probationDeliveryUnitName).isEqualTo("Only PDU Updated")
    }

    @Test
    fun `should return 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()
      val updateRequest = UpdateGroupRequest(groupCode = "NEW_CODE")

      // When / Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/$nonExistentGroupId",
        body = updateRequest,
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).contains("Programme group with id $nonExistentGroupId not found")
    }

    @Test
    fun `should reschedule sessions when automaticallyRescheduleOtherSessions is true and session slots are updated`() {
      // Given
      val group = testGroupHelper.createGroup()
      val originalSessionCount = group.sessions.size
      val newSlots = setOf(
        CreateGroupSessionSlot(DayOfWeek.THURSDAY, 3, 0, AmOrPm.PM),
      )
      val updateRequest = UpdateGroupRequest(
        createGroupSessionSlot = newSlots,
        automaticallyRescheduleOtherSessions = true,
      )

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.programmeGroupSessionSlots).hasSize(1)
      assertThat(updatedGroup.programmeGroupSessionSlots.first().dayOfWeek).isEqualTo(DayOfWeek.THURSDAY)
      // Sessions should still exist (rescheduled, not deleted)
      assertThat(updatedGroup.sessions.size).isGreaterThanOrEqualTo(originalSessionCount)
    }

    @Test
    fun `should not reschedule sessions when automaticallyRescheduleOtherSessions is false`() {
      // Given
      val group = testGroupHelper.createGroup()
      val newSlots = setOf(
        CreateGroupSessionSlot(DayOfWeek.MONDAY, 9, 0, AmOrPm.AM),
      )
      val updateRequest = UpdateGroupRequest(
        createGroupSessionSlot = newSlots,
        automaticallyRescheduleOtherSessions = false,
      )

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        body = updateRequest,
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
      assertThat(updatedGroup.programmeGroupSessionSlots).hasSize(1)
    }

    @Test
    fun `should return 404 if the group doesnt exist`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${UUID.randomUUID()}/details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get BFF Edit Group Cohort")
  inner class GetBffEditGroupCohort {

    @Test
    fun `returns 200 with edit cohort details for a valid group`() {
      // Given
      val group = testGroupHelper.createGroup(groupCode = "TEST001")

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-cohort",
        returnType = object : ParameterizedTypeReference<EditGroupCohort>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.captionText).isEqualTo("Edit group ${group.code}")
      assertThat(response.pageTitle).isEqualTo("Edit the group cohort")
      assertThat(response.submitButtonText).isEqualTo("Submit")
      assertThat(response.radios).isNotEmpty
    }

    @Test
    fun `returns 200 with all cohort radio options present`() {
      // Given
      val group = testGroupHelper.createGroup(groupCode = "TEST002")

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-cohort",
        returnType = object : ParameterizedTypeReference<EditGroupCohort>() {},
      )

      // Then
      val radioValues = response.radios.map { it.value }
      assertThat(radioValues).containsExactlyInAnyOrder(
        ProgrammeGroupCohort.GENERAL.name,
        ProgrammeGroupCohort.GENERAL_LDC.name,
        ProgrammeGroupCohort.SEXUAL.name,
        ProgrammeGroupCohort.SEXUAL_LDC.name,
      )
    }

    @Test
    fun `returns 200 with the current cohort pre-selected for a general offence group`() {
      // Given
      val group = ProgrammeGroupFactory()
        .withCode("TEST003")
        .produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-cohort",
        returnType = object : ParameterizedTypeReference<EditGroupCohort>() {},
      )

      // Then
      val selectedRadio = response.radios.find { it.selected }
      assertThat(selectedRadio).isNotNull
      assertThat(selectedRadio!!.value).isEqualTo(ProgrammeGroupCohort.GENERAL.name)
    }

    @Test
    fun `returns 200 with the current cohort pre-selected for an LDC group`() {
      // Given
      val group = ProgrammeGroupFactory()
        .withCode("TEST004")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .withIsLdc(true)
        .produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-cohort",
        returnType = object : ParameterizedTypeReference<EditGroupCohort>() {},
      )

      // Then
      val selectedRadio = response.radios.find { it.selected }
      assertThat(selectedRadio).isNotNull
      assertThat(selectedRadio!!.value).isEqualTo(ProgrammeGroupCohort.GENERAL_LDC.name)
    }

    @Test
    fun `returns 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/edit-cohort",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Group with id $nonExistentGroupId not found")
    }

    @Test
    fun `returns 403 when not authorised`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST005").produce()
      testDataGenerator.createGroup(group)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${group.id}/edit-cohort")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get BFF Edit Group Sex")
  inner class GetBffEditGroupSex {

    @Test
    fun `returns 200 with edit sex details for a valid group`() {
      // Given
      val group = testGroupHelper.createGroup(groupCode = "TEST001")

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/group-sex-details",
        returnType = object : ParameterizedTypeReference<GroupSexDetails>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.captionText).isEqualTo("Edit group ${group.code}")
      assertThat(response.pageTitle).isEqualTo("Edit the gender of the group")
      assertThat(response.submitButtonText).isEqualTo("Submit")
      assertThat(response.radios).isNotEmpty
    }

    @Test
    fun `returns 200 with the current sex pre-selected for a male group`() {
      // Given
      val group = ProgrammeGroupFactory()
        .withCode("TEST003")
        .withSex(ProgrammeGroupSexEnum.MALE)
        .produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/group-sex-details",
        returnType = object : ParameterizedTypeReference<GroupSexDetails>() {},
      )

      // Then
      val selectedRadio = response.radios.find { it.selected }
      assertThat(selectedRadio).isNotNull
      assertThat(selectedRadio!!.value).isEqualTo(ProgrammeGroupSexEnum.MALE.name)
    }

    @Test
    fun `returns 200 with the current sex pre-selected for a female group`() {
      // Given
      val group = ProgrammeGroupFactory()
        .withCode("TEST004")
        .withSex(ProgrammeGroupSexEnum.FEMALE)
        .produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/group-sex-details",
        returnType = object : ParameterizedTypeReference<GroupSexDetails>() {},
      )

      // Then
      val selectedRadio = response.radios.find { it.selected }
      assertThat(selectedRadio).isNotNull
      assertThat(selectedRadio!!.value).isEqualTo(ProgrammeGroupSexEnum.FEMALE.name)
    }

    @Test
    fun `returns 200 with the current sex pre-selected for a mixed group`() {
      // Given
      val group = ProgrammeGroupFactory()
        .withCode("TEST005")
        .withSex(ProgrammeGroupSexEnum.MIXED)
        .produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/group-sex-details",
        returnType = object : ParameterizedTypeReference<GroupSexDetails>() {},
      )

      // Then
      val selectedRadio = response.radios.find { it.selected }
      assertThat(selectedRadio).isNotNull
      assertThat(selectedRadio!!.value).isEqualTo(ProgrammeGroupSexEnum.MIXED.name)
    }

    @Test
    fun `returns 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/group-sex-details",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Group with id $nonExistentGroupId not found")
    }
  }

  @Nested
  @DisplayName("getBffEditGroupDaysAndTimes")
  @WithMockAuthUser(username = "AUTH_ADM", roles = ["ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR"])
  inner class GetBffEditGroupDaysAndTimes {
    @Test
    fun `successfully retrieves group days and times with correct 12-hour format conversion`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory()
        .withCode("TEST_DAYS_001")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      // Add session slots with various times to test conversion
      group.programmeGroupSessionSlots.add(
        ProgrammeGroupSessionSlotEntity(
          programmeGroup = group,
          dayOfWeek = DayOfWeek.MONDAY,
          startTime = LocalTime.of(9, 30), // 9:30 AM
        ),
      )
      group.programmeGroupSessionSlots.add(
        ProgrammeGroupSessionSlotEntity(
          programmeGroup = group,
          dayOfWeek = DayOfWeek.WEDNESDAY,
          startTime = LocalTime.of(14, 45), // 2:45 PM
        ),
      )
      group.programmeGroupSessionSlots.add(
        ProgrammeGroupSessionSlotEntity(
          programmeGroup = group,
          dayOfWeek = DayOfWeek.FRIDAY,
          startTime = LocalTime.of(0, 0), // Midnight -> 12:00 AM
        ),
      )
      group.programmeGroupSessionSlots.add(
        ProgrammeGroupSessionSlotEntity(
          programmeGroup = group,
          dayOfWeek = DayOfWeek.THURSDAY,
          startTime = LocalTime.of(12, 0), // Noon -> 12:00 PM
        ),
      )

      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-days-and-times",
        returnType = (object : ParameterizedTypeReference<EditGroupDaysAndTimes>() {}),
      )

      // Then
      assertThat(response.id).isEqualTo(group.id)
      assertThat(response.code).isEqualTo("TEST_DAYS_001")
      assertThat(response.programmeGroupSessionSlots).hasSize(4)

      // Verify Monday 9:30 AM
      val mondaySlot = response.programmeGroupSessionSlots.find { it.dayOfWeek == DayOfWeek.MONDAY }
      assertThat(mondaySlot).isNotNull
      assertThat(mondaySlot!!.hour).isEqualTo(9)
      assertThat(mondaySlot.minutes).isEqualTo(30)
      assertThat(mondaySlot.amOrPm).isEqualTo(AmOrPm.AM)

      // Verify Wednesday 2:45 PM
      val wednesdaySlot = response.programmeGroupSessionSlots.find { it.dayOfWeek == DayOfWeek.WEDNESDAY }
      assertThat(wednesdaySlot).isNotNull
      assertThat(wednesdaySlot!!.hour).isEqualTo(2)
      assertThat(wednesdaySlot.minutes).isEqualTo(45)
      assertThat(wednesdaySlot.amOrPm).isEqualTo(AmOrPm.PM)

      // Verify Thursday 12:00 PM (Noon)
      val thursdaySlot = response.programmeGroupSessionSlots.find { it.dayOfWeek == DayOfWeek.THURSDAY }
      assertThat(thursdaySlot).isNotNull
      assertThat(thursdaySlot!!.hour).isEqualTo(12)
      assertThat(thursdaySlot.minutes).isEqualTo(0)
      assertThat(thursdaySlot.amOrPm).isEqualTo(AmOrPm.PM)

      // Verify Friday 12:00 AM (Midnight)
      val fridaySlot = response.programmeGroupSessionSlots.find { it.dayOfWeek == DayOfWeek.FRIDAY }
      assertThat(fridaySlot).isNotNull
      assertThat(fridaySlot!!.hour).isEqualTo(12)
      assertThat(fridaySlot.minutes).isEqualTo(0)
      assertThat(fridaySlot.amOrPm).isEqualTo(AmOrPm.AM)
    }

    @Test
    fun `returns empty session slots when group has none`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory()
        .withCode("TEST_DAYS_004")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/edit-days-and-times",
        returnType = (object : ParameterizedTypeReference<EditGroupDaysAndTimes>() {}),
      )

      // Then
      assertThat(response.id).isEqualTo(group.id)
      assertThat(response.code).isEqualTo("TEST_DAYS_004")
      assertThat(response.programmeGroupSessionSlots).isEmpty()
    }

    @Test
    fun `returns 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/edit-days-and-times",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Group with id $nonExistentGroupId not found")
    }

    @Test
    fun `returns 403 when not authorised`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST_DAYS_005").produce()
      testDataGenerator.createGroup(group)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${group.id}/edit-days-and-times")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Get Group Treatment Manager And Facilitator Details")
  inner class GetGroupTreatmentManagerAndFacilitatorDetails {

    @Test
    fun `returns 200 with treatment manager and facilitator text fields for a valid group`() {
      // Given
      val teamMembers = listOf(
        CreateGroupTeamMember(
          facilitator = "Archibald Queenie",
          facilitatorCode = "TM001",
          teamName = "Management Team",
          teamCode = "TEAM001",
          teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER,
        ),
        CreateGroupTeamMember(
          facilitator = "Chloe Ransom",
          facilitatorCode = "RF001",
          teamName = "Facilitator Team",
          teamCode = "TEAM002",
          teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR,
        ),
        CreateGroupTeamMember(
          facilitator = "James Samhim",
          facilitatorCode = "CF001",
          teamName = "Cover Team",
          teamCode = "TEAM003",
          teamMemberType = CreateGroupTeamMemberType.COVER_FACILITATOR,
        ),
      )
      val group = testGroupHelper.createGroup(groupCode = "TEST_TM_001", teamMembers = teamMembers)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/${group.id}/group-treatment-manager-and-facilitator",
        returnType = object : ParameterizedTypeReference<GroupTreatmentManagerAndFacilitatorDetails>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.captionText).isEqualTo("Edit group ${group.code}")
      assertThat(response.pageTitle).isEqualTo("Edit who is responsible for the group")
      assertThat(response.submitButtonText).isEqualTo("Submit")
      assertThat(response.treatmentManager).isEqualTo("Archibald Queenie")
      assertThat(response.regularFacilitators).containsExactly("Chloe Ransom")
      assertThat(response.coverFacilitators).containsExactly("James Samhim")
    }

    @Test
    fun `returns 404 when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/group/$nonExistentGroupId/group-treatment-manager-and-facilitator",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Group with id $nonExistentGroupId not found")
    }

    @Test
    fun `returns 403 when not authorised`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST_TM_003").produce()
      testDataGenerator.createGroup(group)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${group.id}/group-treatment-manager-and-facilitator")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }
}
