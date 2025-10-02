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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.RestResponsePage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime

class CaseListControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Nested
  @DisplayName("GetCaseListReferrals")
  inner class GetCaseListReferrals {
    @BeforeEach
    fun beforeEach() {
      testDataCleaner.cleanAllTables()
      createReferralsWithStatusHistory()
      testDataGenerator.refreshReferralCaseListItemView()
      stubAuthTokenEndpoint()
      nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
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
    }

    private fun createReferralsWithStatusHistory() {
      // Create referrals with associated status history
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

      val statusHistory1 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral1, awaitingAssessmentStatusDescription)

      testDataGenerator.createReferralWithStatusHistory(referral1, statusHistory1)

      val referral2 = ReferralEntityFactory()
        .withPersonName("Alex River")
        .withCrn("CRN-999999")
        .withInterventionName("Building Choices")
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()

      val statusHistory2 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral2, awaitingAssessmentStatusDescription)

      testDataGenerator.createReferralWithStatusHistory(referral2, statusHistory2)

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
      testDataGenerator.createReferralWithStatusHistory(referral3, statusHistory3)

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
      testDataGenerator.createReferralWithStatusHistory(referral4, statusHistory4)

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
      testDataGenerator.createReferralWithStatusHistory(referral5, statusHistory5)

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
      testDataGenerator.createReferralWithStatusHistory(referral6, statusHistory6)

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
      testDataGenerator.createReferralWithStatusHistory(referral7, statusHistory7)
    }

    @Test
    fun `getCaseListItems for OPEN referrals return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(6)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
    }

    @Test
    fun `getCaseListItems for CLOSED referrals return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(1)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("CRN-111111")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
    }

    @Test
    fun `getCaseListItems for OPEN referrals with no LDC history should default to false and return 200 and paged list of referral case list items`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(6)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.hasLdc).isFalse
        }
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
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(1)
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
    }

    @Test
    fun `getCaseListItems for OPEN and search by personName referrals return 200 and paged list of referral case list items `() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=Alex River",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(1)
      assertThat(referralCaseListItems.first().personName).isEqualTo("Alex River")

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("cohort")
        assertThat(item).hasFieldOrProperty("hasLdc")
      }
    }

    @Test
    fun `getCaseListItems for OPEN and search by personName and cohort returns matching referrals`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=Alex River&cohort=SEXUAL_OFFENCE",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(1)

      val referral = referralCaseListItems[0]
      assertThat(referral.personName).isEqualTo("Alex River")
      assertThat(referral.crn).isEqualTo("CRN-999999")
      assertThat(referral.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(referral.referralStatus).isEqualTo("Awaiting assessment")
    }

    @Test
    fun `getCaseListItems returns matching referrals when only cohort is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?cohort=GENERAL_OFFENCE",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      assertThat(response).isNotNull
      assertThat(response.totalElements).isEqualTo(5)

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
    }

    @Test
    fun `getCaseListItems should only return referrals with a matching status when supplied`() {
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?status=Awaiting%20assessment",
        object : ParameterizedTypeReference<RestResponsePage<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.content

      // Then
      assertThat(response).isNotNull
      assertThat(referralCaseListItems).hasSize(6) // TODO: Return this back to 5, when another ReferralStatusDescription has been put in --TJWC 2025-09-16

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.referralStatus).isEqualTo("Awaiting assessment")
        }
    }

    @Nested
    @DisplayName("Get Case List Filter Data")
    inner class GetCaseListFilterData {
      @Test
      fun `getCaseListFilterData should return status filters for OPEN cases`() {
        // When
        val response = performRequestAndExpectOk(
          HttpMethod.GET,
          "/bff/caselist/filters/OPEN",
          object : ParameterizedTypeReference<CaseListFilterValues>() {},
        )

        // Then
        assertThat(response).isNotNull
        assertThat(response).hasFieldOrProperty("statusFilterValues")
        assertThat(response).hasFieldOrProperty("otherReferralsCount")
        assertThat(response.otherReferralsCount).isEqualTo(1)
        val (statusFilters) = response

        assertThat(statusFilters.open).hasSize(10)
        assertThat(statusFilters.closed).hasSize(2)
      }

      @Test
      fun `getCaseListFilterData should return status filters for CLOSED cases`() {
        // When
        val response = performRequestAndExpectOk(
          HttpMethod.GET,
          "/bff/caselist/filters/CLOSED",
          object : ParameterizedTypeReference<CaseListFilterValues>() {},
        )

        // Then
        assertThat(response).isNotNull
        assertThat(response).hasFieldOrProperty("statusFilterValues")
        assertThat(response).hasFieldOrProperty("otherReferralsCount")
        assertThat(response.otherReferralsCount).isEqualTo(6)
        val (statusFilters) = response

        assertThat(statusFilters.open).hasSize(10)
        assertThat(statusFilters.closed).hasSize(2)
      }
    }
  }
}
