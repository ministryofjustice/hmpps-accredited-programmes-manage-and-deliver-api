package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.PagedProgrammeDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

class ProgrammeGroupControllerIntegrationTest(@Autowired private val referralService: ReferralService) : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  private lateinit var referrals: List<ReferralEntity>

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()

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
    referrals.map {
      referralService.updateStatus(
        it,
        status.id,
        createdBy = "AUTH_USER",
      )
    }
    referrals = referralRepository.findAll()
  }

  @Nested
  @DisplayName("Get Group details")
  inner class GetGroupDetails {
    @Test
    fun `getGroupDetails returns 200 with valid group and waitlist data`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").withRegionName("TEST REGION").produce()
      testDataGenerator.createGroup(group)

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
      assertThat(response.group.regionName).isEqualTo("TEST REGION")
      assertThat(response.pagedGroupData.totalElements).isEqualTo(5)
      assertThat(response.otherTabTotal).isEqualTo(1)
      assertThat(response.pagedGroupData).isNotNull
      assertThat(response.pagedGroupData.content.map { it.statusColour }).isNotEmpty
    }

    @Test
    fun `getGroupDetails contains a list of filters`() {
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
          "General Offence",
          "General Offence - LDC",
          "Sexual Offence",
          "Sexual Offence - LDC",
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
    fun `getGroupDetails returns 200 and uses default filters if none are provided`() {
      // Given
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
    fun `getGroupDetails returns 404 for non-existent group`() {
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
    fun `getGroupDetails should handle pagination correctly`() {
      // Given
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
    fun `getGroupDetails combines multiple filters correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST007").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?sex=Male&cohort=Sexual Offence&pdu=Test PDU 1&page=0&size=10",
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
    fun `getGroupDetails filters by multiple reportingTeams on WAITLIST tab`() {
      // Given
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
    fun `getGroupDetails should ignore reportingTeam if PDU is not also present`() {
      // Given
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
    fun `getGroupDetails filters by reportingTeam and pdu together`() {
      // Given
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
    fun `getGroupDetails returns 200 for ALLOCATED tab with all data when no filters are provided`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST008").withRegionName("WIREMOCKED REGION").produce()
      testDataGenerator.createGroup(group)
      stubAuthTokenEndpoint()

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
    fun `getGroupDetails returns 401 when not authorized`() {
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
  @DisplayName("Allocate to Programme group")
  inner class AllocateToProgrammeGroup {
    @Test
    fun `allocateReferralToGroup can successfully allocate a referral to a group`() {
      // Given
      val theCrnNumber = randomUppercaseString()
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

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

      val referral = referralService.createReferral(
        FindAndReferReferralDetailsFactory()
          .withPersonReference(theCrnNumber)
          .withEventNumber(1)
          .produce(),
      )

      val allocateToGroupRequest = AllocateToGroupRequest(
        additionalDetails = "The additional details for the test",
      )

      // When
      val response = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
        body = allocateToGroupRequest,
        returnType = object : ParameterizedTypeReference<AllocateToGroupResponse>() {},
      )

      val foundRepository = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(response.message).isEqualTo("the-forename the-surname was added to this group. Their referral status is now Scheduled.")

      assertThat(foundRepository).isNotNull
      assertThat(foundRepository.id).isEqualTo(referral.id)
      assertThat(foundRepository.programmeGroupMemberships).hasSize(1)
      assertThat(foundRepository.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)
    }

    @Test
    fun `allocateReferralToGroup throws an error if referral does not exist`() {
      val referralId = UUID.randomUUID()
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/$referralId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )

      assertThat(exception.userMessage).isEqualTo("Not Found: No Referral found for id: $referralId")
    }

    @Test
    fun `allocateReferralToGroup throws an error if group does not exist`() {
      val groupId = UUID.randomUUID()
      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

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
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

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
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)
      val groupMembership = ProgrammeGroupMembershipFactory().withReferral(referral).withProgrammeGroup(group).produce()
      testDataGenerator.createGroupMembership(groupMembership)

      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
        body = AllocateToGroupRequest("Empty additional details"),
      )
      assertThat(exception.userMessage).isEqualTo("Bad request: Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }
  }

  @Nested
  @DisplayName("Create a Programme group")
  inner class CreateProgrammeGroup {
    @Test
    fun `create group with code and return 200 when it doesn't already exist`() {
      val body = CreateGroupRequest("TEST_GROUP", ProgrammeGroupCohort.GENERAL, ProgrammeGroupSexEnum.MALE)
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
    }

    @Test
    fun `create group and assign correct cohort and sex and return 200 when it doesn't already exist`() {
      val body = CreateGroupRequest("TEST_GROUP", ProgrammeGroupCohort.SEXUAL_LDC, ProgrammeGroupSexEnum.FEMALE)
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group",
        body = body,
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )
      val createdGroup = programmeGroupRepository.findByCode(body.groupCode)!!
      assertThat(createdGroup.code).isEqualTo(body.groupCode)
      assertThat(createdGroup.id).isNotNull
      assertThat(createdGroup.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(createdGroup.isLdc).isTrue
      assertThat(createdGroup.sex).isEqualTo(ProgrammeGroupSexEnum.FEMALE)
      assertThat(createdGroup.regionName).isEqualTo("WIREMOCKED REGION")
    }

    @Test
    fun `create group with code and return CONFLICT when it already exists within the region`() {
      val group = ProgrammeGroupFactory().withCode("TEST_GROUP").withRegionName("WIREMOCKED REGION").produce()
      testDataGenerator.createGroup(group)
      val body = CreateGroupRequest("TEST_GROUP", ProgrammeGroupCohort.GENERAL, ProgrammeGroupSexEnum.MALE)
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
    fun `return 401 when unauthorised`() {
      val body = CreateGroupRequest("TEST_GROUP", ProgrammeGroupCohort.GENERAL, ProgrammeGroupSexEnum.MALE)
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
      val body = CreateGroupRequest("TEST_GROUP", ProgrammeGroupCohort.GENERAL, ProgrammeGroupSexEnum.MALE)
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
}
