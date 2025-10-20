package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDate
import java.util.UUID

class GroupControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("GetGroupDetails")
  inner class GetGroupDetails {

    @Test
    fun `getGroupDetails returns 200 with valid group and waitlist data`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST001").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // Then
      assertThat(response).isNotNull
      assertThat(response.group.code).isEqualTo("TEST001")
      assertThat(response.group.regionName).isEqualTo("TODO: Region mapping to be implemented")
      assertThat(response.allocationAndWaitlistData.counts.waitlist).isGreaterThanOrEqualTo(0)
      assertThat(response.allocationAndWaitlistData.filters).isNotNull
      assertThat(response.allocationAndWaitlistData.filters.sex).containsExactly("Male", "Female")
      assertThat(response.allocationAndWaitlistData.filters.cohort).containsExactlyInAnyOrder(*OffenceCohort.entries.toTypedArray())
      assertThat(response.allocationAndWaitlistData.paginatedWaitlistData).isNotNull
    }

    @Test
    fun `getGroupDetails returns 404 for non-existent group`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()
      stubAuthTokenEndpoint()

      // When & Then
      performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/group/$nonExistentGroupId?allocationAndWaitlistTab=WAITLIST&page=0&size=10",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `getGroupDetails filters by sex correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST002").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }

      // When - Filter by Male
      val maleResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&sex=Male&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // When - Filter by Female
      val femaleResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&sex=Female&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      maleResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.sex).isEqualTo("Male")
      }
      femaleResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.sex).isEqualTo("Female")
      }
    }

    @Test
    fun `getGroupDetails filters by cohort correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST003").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }

      // When - Filter by SEXUAL_OFFENCE
      val sexualOffenceResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&cohort=SEXUAL_OFFENCE&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // When - Filter by GENERAL_OFFENCE
      val generalOffenceResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&cohort=GENERAL_OFFENCE&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      sexualOffenceResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      }
      generalOffenceResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      }
    }

    @Test
    fun `getGroupDetails filters by nameOrCRN correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST004").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }

      // When - Search by name
      val nameSearchResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&nameOrCRN=john&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // When - Search by CRN
      val crnSearchResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&nameOrCRN=CRN001&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )

      // Then
      nameSearchResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.personName.lowercase()).contains("john")
      }
      crnSearchResponse.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.crn.lowercase()).contains("crn001")
      }
    }

    @Test
    fun `getGroupDetails filters by pdu correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST005").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&pdu=Test PDU 1&page=0&size=10",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // Then
      response.allocationAndWaitlistData.paginatedWaitlistData.forEach { item ->
        assertThat(item.pdu).isEqualTo("Test PDU 1")
      }
    }

    @Test
    fun `getGroupDetails should handle pagination correctly`() {
      // Given
      stubAuthTokenEndpoint()
      val group = ProgrammeGroupFactory().withCode("TEST006").produce()
      testDataGenerator.createGroup(group)

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }
      referrals.forEach { programmeGroupMembershipService.allocateReferralToGroup(it.first.id!!, group.id!!) }

      // When - Get first page
      val firstPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&page=0&size=2",
        object : ParameterizedTypeReference<ProgrammeGroupDetails>() {},
      )
      // When - Get second page
      val secondPageResponse = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&page=1&size=2",
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

      val referrals = createTestWaitlistData()
      referrals.forEach { (referral, statusHistory, reportingLocation) ->
        testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
          referral,
          statusHistory,
          reportingLocation,
        )
      }

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&sex=Male&cohort=SEXUAL_OFFENCE&pdu=Test PDU 1&page=0&size=10",
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
    fun `getGroupDetails returns 500 for ALLOCATED tab as it is not implemented`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST008").produce()
      testDataGenerator.createGroup(group)
      stubAuthTokenEndpoint()

      // When & Then
      performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/group/${group.id}?allocationAndWaitlistTab=ALLOCATED&page=0&size=10",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
      )
    }

    @Test
    fun `getGroupDetails returns 401 when not authorized`() {
      // Given
      val group = ProgrammeGroupFactory().withCode("TEST009").produce()
      testDataGenerator.createGroup(group)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/group/${group.id}?allocationAndWaitlistTab=WAITLIST&page=0&size=10")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    private fun createTestWaitlistData() = listOf(
      createReferralWithWaitlistStatus("CRN001", "John Smith", "Male", "Sexual offence", "Test PDU 1", "Team A"),
      createReferralWithWaitlistStatus("CRN002", "Jane Doe", "Female", "General offence", "Test PDU 2", "Team B"),
      createReferralWithWaitlistStatus("CRN003", "John Brown", "Male", "Sexual offence", "Test PDU 1", "Team A"),
      createReferralWithWaitlistStatus("CRN004", "Mary Johnson", "Female", "General offence", "Test PDU 2", "Team B"),
      createReferralWithWaitlistStatus("CRN005", "Bob Wilson", "Male", "General offence", "Test PDU 1", "Team C"),
    )

    private fun createReferralWithWaitlistStatus(
      crn: String,
      personName: String,
      sex: String,
      cohort: String,
      pduName: String,
      reportingTeam: String,
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
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )

      val reportingLocation = ReferralReportingLocationFactory()
        .withReferral(referral)
        .withPduName(pduName)
        .withReportingTeam(reportingTeam)
        .produce()

      return Triple(referral, statusHistory, reportingLocation)
    }
  }
}
