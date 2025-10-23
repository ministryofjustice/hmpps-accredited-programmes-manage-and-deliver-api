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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDate
import java.util.UUID

class GroupControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  private lateinit var referrals: List<Triple<ReferralEntity, ReferralStatusHistoryEntity, ReferralReportingLocationEntity>>

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
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

    referrals = createTestWaitlistData()
    referrals.forEach { (referral, statusHistory, reportingLocation) ->
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral,
        statusHistory,
        reportingLocation,
      )
    }
  }

  @Nested
  @DisplayName("Get Group details")
  inner class GetGroupDetails {
    @Test
    fun `getGroupDetails returns 200 with valid group and waitlist data`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST001")
      assertThat(response.group.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(response.allocationAndWaitlistData.counts.waitlist).isEqualTo(0)
      assertThat(response.allocationAndWaitlistData.filters).isNotNull
      assertThat(response.allocationAndWaitlistData.filters.sex).containsExactly("Male", "Female")
      assertThat(response.allocationAndWaitlistData.filters.cohort).containsExactlyInAnyOrder(*OffenceCohort.entries.toTypedArray())
      assertThat(response.allocationAndWaitlistData.paginatedWaitlistData).isNotNull
    }

    @Test
    fun `getGroupDetails returns 200 and uses default filters if none are provided`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)
      referrals.forEach { programmeGroupMembershipService.allocateReferralToGroup(it.first.id!!, group.id!!) }

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST001")
      assertThat(response.group.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(response.allocationAndWaitlistData.counts.waitlist).isEqualTo(5)
      assertThat(response.allocationAndWaitlistData.filters).isNotNull
      assertThat(response.allocationAndWaitlistData.filters.sex).containsExactly("Male", "Female")
      assertThat(response.allocationAndWaitlistData.filters.cohort).containsExactlyInAnyOrder(*OffenceCohort.entries.toTypedArray())
      assertThat(response.allocationAndWaitlistData.pagination.size).isEqualTo(10)
      assertThat(response.allocationAndWaitlistData.pagination.page).isEqualTo(0)
      assertThat(response.allocationAndWaitlistData.paginatedWaitlistData).isNotNull
      assertThat(response.allocationAndWaitlistData.paginatedWaitlistData.size).isEqualTo(5)
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

      referrals.forEach { programmeGroupMembershipService.allocateReferralToGroup(it.first.id!!, group.id!!) }

      // When - Get first page
      val firstPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=0&size=2",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // When - Get second page
      val secondPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/WAITLIST?page=1&size=2",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      assertThat(firstPageResponse.allocationAndWaitlistData.pagination.page).isEqualTo(0)
      assertThat(secondPageResponse.allocationAndWaitlistData.pagination.page).isEqualTo(1)

      assertThat(firstPageResponse.allocationAndWaitlistData.pagination.size).isEqualTo(2)
      assertThat(secondPageResponse.allocationAndWaitlistData.pagination.size).isEqualTo(2)

      val firstPageData = firstPageResponse.allocationAndWaitlistData.paginatedWaitlistData
      val secondPageData = secondPageResponse.allocationAndWaitlistData.paginatedWaitlistData

      assertThat(firstPageData).hasSize(2)
      assertThat(secondPageData).hasSize(2)
      assertThat(firstPageData).doesNotContainAnyElementsOf(secondPageData)
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
        "/bff/group/${group.id}/WAITLIST?sex=Male&cohort=SEXUAL_OFFENCE&pdu=Test PDU 1&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      response.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.sex).isEqualTo("Male")
        assertThat(item.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
        assertThat(item.pdu).isEqualTo("Test PDU 1")
      }
    }

    @Test
    fun `getGroupDetails returns 200 for ALLOCATED tab with all data when no filters are provided`() {
      // Given
      testDataCleaner.cleanAllTables()
      val group = ProgrammeGroupFactory().withCode("TEST008").produce()
      testDataGenerator.createGroup(group)
      stubAuthTokenEndpoint()

      val allocatedListData = createTestAllocatedListData()
      allocatedListData.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }
      allocatedListData.forEach { programmeGroupMembershipService.allocateReferralToGroup(it.first.id!!, group.id!!) }

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}/ALLOCATED",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST008")
      assertThat(response.group.regionName).isEqualTo("WIREMOCKED REGION")
      assertThat(response.allocationAndWaitlistData.counts.waitlist).isEqualTo(0)
      assertThat(response.allocationAndWaitlistData.counts.allocated).isEqualTo(6)
      assertThat(response.allocationAndWaitlistData.filters).isNotNull
      assertThat(response.allocationAndWaitlistData.filters.sex).containsExactly("Male", "Female")
      assertThat(response.allocationAndWaitlistData.filters.cohort).containsExactlyInAnyOrder(*OffenceCohort.entries.toTypedArray())
      assertThat(response.allocationAndWaitlistData.pagination.size).isEqualTo(10)
      assertThat(response.allocationAndWaitlistData.pagination.page).isEqualTo(0)
      assertThat(response.allocationAndWaitlistData.paginatedWaitlistData).isEmpty()
      assertThat(response.allocationAndWaitlistData.paginatedAllocationData).isNotEmpty
      assertThat(response.allocationAndWaitlistData.paginatedAllocationData).hasSize(6)
      assertThat(response.allocationAndWaitlistData.paginatedAllocationData).noneMatch { it.status == ReferralStatusType.AWAITING_ALLOCATION.description }
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
  @DisplayName("Allocate to Programme Group")
  inner class AllocateToProgrammeGroup {
    @Test
    fun `allocateReferralToGroup can successfully allocate a referral to a group`() {
      // Given
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      // When
      performRequestAndExpectStatusNoBody(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val result = referralRepository.findByIdOrNull(referral.id!!)!!

      // Then
      assertThat(result).isNotNull
      assertThat(result.id).isEqualTo(referral.id)
      assertThat(result.programmeGroupMemberships).hasSize(1)
      assertThat(result.programmeGroupMemberships.first().programmeGroup.id).isEqualTo(group.id)
    }

    @Test
    fun `allocateReferralToGroup throws an error if referral does not exist`() {
      val referralId = UUID.randomUUID()
      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val exception = performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/$referralId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )

      assertThat(exception.userMessage).isEqualTo("Not Found: Referral with id $referralId not found")
    }

    @Test
    fun `allocateReferralToGroup throws an error if group does not exist`() {
      val groupId = UUID.randomUUID()
      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)
      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      val exception = performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/$groupId/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
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

      val exception = performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
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

      val exception = performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
      assertThat(exception.userMessage).isEqualTo("Bad request: Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }
  }

  private fun createTestWaitlistData() = listOf(
    createReferralWithWaitlistStatus(
      "CRN001",
      "John Smith",
      "Male",
      "Sexual offence",
      "Test PDU 1",
      "Team A",
      ReferralStatusDescriptionRepository::getAwaitingAllocationStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN002",
      "Jane Doe",
      "Female",
      "General offence",
      "Test PDU 2",
      "Team B",
      ReferralStatusDescriptionRepository::getAwaitingAllocationStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN003",
      "John Brown",
      "Male",
      "Sexual offence",
      "Test PDU 1",
      "Team A",
      ReferralStatusDescriptionRepository::getAwaitingAllocationStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN004",
      "Mary Johnson",
      "Female",
      "General offence",
      "Test PDU 2",
      "Team B",
      ReferralStatusDescriptionRepository::getAwaitingAllocationStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN005",
      "Bob Wilson",
      "Male",
      "General offence",
      "Test PDU 1",
      "Team C",
      ReferralStatusDescriptionRepository::getAwaitingAllocationStatusDescription,
    ),
  )

  private fun createTestAllocatedListData() = listOf(
    createReferralWithWaitlistStatus(
      "CRN006",
      "John Smith",
      "Male",
      "Sexual offence",
      "Test PDU 1",
      "Team A",
      ReferralStatusDescriptionRepository::getOnProgrammeStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN007",
      "Jane Doe",
      "Female",
      "General offence",
      "Test PDU 2",
      "Team B",
      ReferralStatusDescriptionRepository::getAwaitingAssessmentStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN008",
      "John Brown",
      "Male",
      "Sexual offence",
      "Test PDU 1",
      "Team A",
      ReferralStatusDescriptionRepository::getScheduledStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN009",
      "Mary Doe",
      "Female",
      "General offence",
      "Test PDU 3",
      "Team B",
      ReferralStatusDescriptionRepository::getOnProgrammeStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN010",
      "Bob Wilson",
      "Male",
      "General offence",
      "Test PDU 1",
      "Team C",
      ReferralStatusDescriptionRepository::getReturnToCourtStatusDescription,
    ),
    createReferralWithWaitlistStatus(
      "CRN011",
      "John Doe",
      "Male",
      "General offence",
      "Test PDU 2",
      "Team C",
      ReferralStatusDescriptionRepository::getOnProgrammeStatusDescription,
    ),
  )

  private fun createReferralWithWaitlistStatus(
    crn: String,
    personName: String,
    sex: String,
    cohort: String,
    pduName: String,
    reportingTeam: String,
    getReferralStatusDescriptionFunction: (ReferralStatusDescriptionRepository) -> ReferralStatusDescriptionEntity,
  ): Triple<ReferralEntity, ReferralStatusHistoryEntity, ReferralReportingLocationEntity> {
    val referral = ReferralEntityFactory()
      .withCrn(crn)
      .withPersonName(personName)
      .withSex(sex)
      .withCohort(OffenceCohort.fromDisplayName(cohort))
      .withSentenceEndDate(LocalDate.now().plusYears(2))
      .withDateOfBirth(LocalDate.now().minusYears(30))
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .produce()

    val statusHistory = ReferralStatusHistoryEntityFactory().produce(
      referral,
      getReferralStatusDescriptionFunction.invoke(referralStatusDescriptionRepository),
    )

    val reportingLocation = ReferralReportingLocationFactory()
      .withReferral(referral)
      .withPduName(pduName)
      .withReportingTeam(reportingTeam)
      .produce()

    return Triple(referral, statusHistory, reportingLocation)
  }
}
