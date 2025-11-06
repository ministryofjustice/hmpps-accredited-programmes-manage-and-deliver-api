package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.PagedCaseListReferrals
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime

class CaseListControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Nested
  @DisplayName("GetCaseListReferrals")
  inner class GetCaseListReferrals {
    @BeforeEach
    fun beforeEach() {
      testDataCleaner.cleanAllTables()
      createReferralsWithStatusHistoryAndReportingLocations()
      testDataGenerator.refreshReferralCaseListItemView()
      stubAuthTokenEndpoint()

      // Grant permission to all of the following (fake) CRNs
      nDeliusApiStubs.stubAccessCheck(
        true,
        "X7182552",
        "CRN-999999",
        "CRN-888888",
        "CRN-777777",
        "CRN-66666",
        "CRN-555555",
        "CRN-111111",
      )

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

    private fun createReferralsWithStatusHistoryAndReportingLocations() {
      // Create referrals with associated status history and reporting locations
      val awaitingAssessmentStatusDescription =
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val programmeCompleteStatusDescription =
        referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()

      val referral1 = ReferralEntityFactory()
        .withPersonName("Joe Bloggs")
        .withCrn("X7182552")
        .withInterventionName("Horizon")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val referralReportingLocation1 = ReferralReportingLocationFactory(referral1)
        .withPduName("PDU1")
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val statusHistory1 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral1, awaitingAssessmentStatusDescription)

      val referral2 = ReferralEntityFactory()
        .withPersonName("Alex River")
        .withCrn("CRN-999999")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val referralReportingLocation2 = ReferralReportingLocationFactory(referral2)
        .withPduName("PDU1")
        .withReportingTeam("reportingTeam2")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val statusHistory2 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral2, awaitingAssessmentStatusDescription)

      val referral3 = ReferralEntityFactory()
        .withPersonName("Jane Adams")
        .withCrn("CRN-888888")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val statusHistory3 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral3, awaitingAssessmentStatusDescription)
      val referralReportingLocation3 = ReferralReportingLocationFactory(referral3)
        .withPduName("PDU2")
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      val referral4 = ReferralEntityFactory()
        .withPersonName("Pete Grims")
        .withCrn("CRN-777777")
        .withInterventionName("New Me")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val statusHistory4 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral4, awaitingAssessmentStatusDescription)
      val referralReportingLocation4 = ReferralReportingLocationFactory(referral4)
        .withPduName("PDU1")
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      val referral5 = ReferralEntityFactory()
        .withPersonName("James Hayden")
        .withCrn("CRN-66666")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val statusHistory5 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.parse("2025-07-10T00:00:00"))
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.parse("2025-07-10T00:00:00"))
        .produce(referral5, awaitingAssessmentStatusDescription)
      val referralReportingLocation5 = ReferralReportingLocationFactory(referral5)
        .withPduName("PDU2")
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      val referral6 = ReferralEntityFactory()
        .withPersonName("Andrew Crosforth")
        .withCrn("CRN-555555")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val statusHistory6 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral6, awaitingAssessmentStatusDescription)
      val referralReportingLocation6 = ReferralReportingLocationFactory(referral6)
        .withPduName("UNKNOWN_PDU_NAME")
        .withReportingTeam("UNKNOWN_REPORTING_TEAM")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      val referral7 = ReferralEntityFactory()
        .withPersonName("James Mars")
        .withCrn("CRN-111111")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()
      val statusHistory7 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral7, programmeCompleteStatusDescription)
      val referralReportingLocation7 = ReferralReportingLocationFactory(referral7)
        .withPduName("PDU2")
        .withReportingTeam("reportingTeam2")
        .withRegionName("WIREMOCKED REGION")
        .produce()

      referral1.referralReportingLocationEntity = referralReportingLocation1
      referral2.referralReportingLocationEntity = referralReportingLocation2
      referral3.referralReportingLocationEntity = referralReportingLocation3
      referral4.referralReportingLocationEntity = referralReportingLocation4
      referral5.referralReportingLocationEntity = referralReportingLocation5
      referral6.referralReportingLocationEntity = referralReportingLocation6
      referral7.referralReportingLocationEntity = referralReportingLocation7

      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral1,
        statusHistory1,
        referralReportingLocation1,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral2,
        statusHistory2,
        referralReportingLocation2,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral3,
        statusHistory3,
        referralReportingLocation3,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral4,
        statusHistory4,
        referralReportingLocation4,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral5,
        statusHistory5,
        referralReportingLocation5,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral6,
        statusHistory6,
        referralReportingLocation6,
      )
      testDataGenerator.createReferralWithReportingLocationAndStatusHistory(
        referral7,
        statusHistory7,
        referralReportingLocation7,
      )
    }

    @Test
    fun `getCaseListItems for OPEN referrals return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(6)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems for CLOSED referrals return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("CRN-111111")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
      assertThat(response.otherTabTotal).isEqualTo(6)
    }

    @Test
    fun `getCaseListItems for OPEN referrals with no LDC history should default to false and return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val pagedReferralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(6)
      assertThat(pagedReferralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")

      assertThat(pagedReferralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.hasLdc).isFalse
        }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems for invalid ENUM type returns 400 BAD REQUEST`() {
      val response = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/pages/caselist/INVALID_ENUM",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.userMessage).isEqualTo("Invalid value for parameter openOrClosed")
    }

    @Test
    fun `should return HTTP 403 Forbidden when retrieving a referral without the appropriate role`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/pages/caselist/closed")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `getCaseListItems for OPEN and search by crn referrals return 200 and paged list of referral case list items `() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=CRN-888888",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(referralCaseListItems.first().crn).isEqualTo("CRN-888888")

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
        }
      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems for OPEN and search by personName referrals return 200 and paged list of referral case list items `() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=Alex River",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(referralCaseListItems.first().personName).isEqualTo("Alex River")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems for OPEN and search by personName and cohort returns matching referrals`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=Alex River&cohort=SEXUAL_OFFENCE",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)

      val referral = referralCaseListItems[0]
      assertThat(referral.personName).isEqualTo("Alex River")
      assertThat(referral.crn).isEqualTo("CRN-999999")
      assertThat(referral.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(referral.referralStatus).isEqualTo("Awaiting assessment")
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems returns matching referrals when only cohort is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?cohort=GENERAL_OFFENCE",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(5)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems returns matching referrals when pdu is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=PDU1",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(3)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.pdu).isEqualTo("PDU1")
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("pdu")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems returns matching referrals when reporting team is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=PDU1&reportingTeam=reportingTeam1",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(2)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.reportingTeam).isEqualTo("reportingTeam1")
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("pdu")
        assertThat(item).hasFieldOrProperty("reportingTeam")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems returns matching referrals when multiple reporting teams are used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=PDU1&reportingTeam=reportingTeam1&reportingTeam=reportingTeam2",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(3)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.reportingTeam).isIn("reportingTeam1", "reportingTeam2")
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("pdu")
        assertThat(item).hasFieldOrProperty("reportingTeam")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems should only return referrals with a matching status when supplied`() {
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?status=Awaiting%20assessment",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      // Then
      assertThat(response).isNotNull
      assertThat(referralCaseListItems).hasSize(6) // TODO: Return this back to 5, when another ReferralStatusDescription has been put in --TJWC 2025-09-16

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.referralStatus).isEqualTo("Awaiting assessment")
        }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems for OPEN referrals with reporting location should default to 'UNKNOWN' values and return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals

      assertThat(response).isNotNull
      assertThat(referralCaseListItems.totalElements).isEqualTo(6)
      assertThat(referralCaseListItems.content.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")

      val referralsWithUnknownReportingLocation = referralCaseListItems.content.filter { it.crn == "CRN-555555" }

      assertThat(referralsWithUnknownReportingLocation)
        .allSatisfy { item ->
          assertThat(item.reportingTeam).isEqualTo("UNKNOWN_REPORTING_TEAM")
          assertThat(item.pdu).isEqualTo("UNKNOWN_PDU_NAME")
        }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Nested
    @DisplayName("Get Case List Filter Data")
    inner class GetCaseListFilterData {
      @Test
      fun `getCaseListFilterData should return status and location filters for OPEN cases`() {
        // When
        val response = performRequestAndExpectOk(
          HttpMethod.GET,
          "/bff/caselist/filters",
          object : ParameterizedTypeReference<CaseListFilterValues>() {},
        )

        // Then
        assertThat(response).isNotNull
        assertThat(response).hasFieldOrProperty("statusFilterValues")
        assertThat(response).hasFieldOrProperty("locationFilterValues")
        val (statusFilters, locationFilters) = response

        assertThat(statusFilters.open).hasSize(10)
        assertThat(statusFilters.closed).hasSize(2)
        assertThat(locationFilters).hasSize(3)
        assertThat(
          locationFilters.find { it.pduName == "PDU1" }?.reportingTeams?.containsAll(
            mutableListOf(
              "reportingTeam1",
              "reportingTeam2",
            ),
          ),
        )
        // Check duplicate has been removed
        assertThat(locationFilters.find { it.pduName == "PDU1" }?.reportingTeams).hasSize(2)
        assertThat(
          locationFilters.find { it.pduName == "PDU2" }?.reportingTeams?.containsAll(
            mutableListOf(
              "reportingTeam1",
              "reportingTeam2",
            ),
          ),
        )
      }

      @Test
      fun `getCaseListFilterData should return status and locations filters for CLOSED cases`() {
        // When
        val response = performRequestAndExpectOk(
          HttpMethod.GET,
          "/bff/caselist/filters",
          object : ParameterizedTypeReference<CaseListFilterValues>() {},
        )

        // Then
        assertThat(response).isNotNull
        assertThat(response).hasFieldOrProperty("statusFilterValues")
        assertThat(response).hasFieldOrProperty("locationFilterValues")
        val (statusFilters, locationFilters) = response

        assertThat(statusFilters.open).hasSize(10)
        assertThat(statusFilters.closed).hasSize(2)
        assertThat(locationFilters).hasSize(3)
        assertThat(
          locationFilters.find { it.pduName == "PDU1" }?.reportingTeams?.containsAll(
            mutableListOf(
              "reportingTeam1",
              "reportingTeam2",
            ),
          ),
        )
        // Check duplicate has been removed
        assertThat(locationFilters.find { it.pduName == "PDU1" }?.reportingTeams).hasSize(2)
        assertThat(
          locationFilters.find { it.pduName == "PDU2" }?.reportingTeams?.containsAll(
            mutableListOf(
              "reportingTeam1",
              "reportingTeam2",
            ),
          ),
        )
      }
    }
  }
}
