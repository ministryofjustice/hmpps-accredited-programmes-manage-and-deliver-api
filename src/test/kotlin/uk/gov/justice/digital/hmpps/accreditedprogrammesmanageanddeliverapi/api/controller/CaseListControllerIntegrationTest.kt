package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.PagedCaseListReferrals
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.UserRegionOverrideEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.UserRegionOverrideRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.UUID

class CaseListControllerIntegrationTest : IntegrationTestBase() {

  private lateinit var pduWithComma: String
  private lateinit var pduSecondary: String

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralCaseListItemRepository: ReferralCaseListItemRepository

  @Autowired
  private lateinit var userRegionOverrideRepository: UserRegionOverrideRepository

  @Nested
  @DisplayName("GetCaseListReferrals")
  @TestMethodOrder(MethodOrderer.MethodName::class)
  inner class GetCaseListReferrals {
    @BeforeEach
    fun beforeEach() {
      pduWithComma = "Test PDU, ${UUID.randomUUID()}"
      pduSecondary = "Test PDU ${UUID.randomUUID()}"

      testDataCleaner.cleanAllTables()
      createReferralsWithStatusHistoryAndReportingLocations()
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
        .produce()
      val referralReportingLocation1 = ReferralReportingLocationFactory(referral1)
        .withPduName(pduWithComma)
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val statusHistory1 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral1, awaitingAssessmentStatusDescription)
      val cohortHistory1 = ReferralCohortHistoryFactory().withReferral(referral1).produce()

      val referral2 = ReferralEntityFactory()
        .withPersonName("Alex River")
        .withCrn("CRN-999999")
        .withInterventionName("Building Choices")
        .produce()
      val referralReportingLocation2 = ReferralReportingLocationFactory(referral2)
        .withPduName(pduWithComma)
        .withReportingTeam("reportingTeam2")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val statusHistory2 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral2, awaitingAssessmentStatusDescription)
      val cohortHistory2 =
        ReferralCohortHistoryFactory().withReferral(referral2).withCohort(OffenceCohort.SEXUAL_OFFENCE).produce()

      val referral3 = ReferralEntityFactory()
        .withPersonName("Jane Adams")
        .withCrn("CRN-888888")
        .withInterventionName("Building Choices")
        .produce()
      val statusHistory3 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral3, awaitingAssessmentStatusDescription)
      val referralReportingLocation3 = ReferralReportingLocationFactory(referral3)
        .withPduName(pduSecondary)
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val cohortHistory3 = ReferralCohortHistoryFactory().withReferral(referral3).produce()

      val referral4 = ReferralEntityFactory()
        .withPersonName("Pete Grims")
        .withCrn("CRN-777777")
        .withInterventionName("New Me")
        .produce()
      val statusHistory4 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral4, awaitingAssessmentStatusDescription)
      val referralReportingLocation4 = ReferralReportingLocationFactory(referral4)
        .withPduName(pduWithComma)
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val cohortHistory4 = ReferralCohortHistoryFactory().withReferral(referral4).produce()

      val referral5 = ReferralEntityFactory()
        .withPersonName("James Hayden")
        .withCrn("CRN-66666")
        .withInterventionName("Building Choices")
        .produce()
      val statusHistory5 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.parse("2025-07-10T00:00:00"))
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.parse("2025-07-10T00:00:00"))
        .produce(referral5, awaitingAssessmentStatusDescription)
      val referralReportingLocation5 = ReferralReportingLocationFactory(referral5)
        .withPduName(pduSecondary)
        .withReportingTeam("reportingTeam1")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val cohortHistory5 = ReferralCohortHistoryFactory().withReferral(referral5).produce()

      val referral6 = ReferralEntityFactory()
        .withPersonName("Andrew Crosforth")
        .withCrn("CRN-555555")
        .withInterventionName("Building Choices")
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
      val cohortHistory6 = ReferralCohortHistoryFactory().withReferral(referral6).produce()

      val referral7 = ReferralEntityFactory()
        .withPersonName("James Mars")
        .withCrn("CRN-111111")
        .withInterventionName("Building Choices")
        .produce()
      val statusHistory7 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral7, programmeCompleteStatusDescription)
      val referralReportingLocation7 = ReferralReportingLocationFactory(referral7)
        .withPduName(pduWithComma)
        .withReportingTeam("reportingTeam2")
        .withRegionName("WIREMOCKED REGION")
        .produce()
      val cohortHistory7 = ReferralCohortHistoryFactory().withReferral(referral7).produce()

      val referral8 = ReferralEntityFactory()
        .withPersonName("Other Region Person")
        .withCrn("CRN-888888")
        .withInterventionName("Horizon")
        .produce()
      val referralReportingLocation8 = ReferralReportingLocationFactory(referral8)
        .withPduName("OTHER_REGION_PDU")
        .withReportingTeam("otherReportingTeam")
        .withRegionName("OTHER REGION")
        .produce()
      val statusHistory8 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral8, awaitingAssessmentStatusDescription)
      val cohortHistory8 = ReferralCohortHistoryFactory().withReferral(referral8).produce()

      referral1.referralReportingLocation = referralReportingLocation1
      referral2.referralReportingLocation = referralReportingLocation2
      referral3.referralReportingLocation = referralReportingLocation3
      referral4.referralReportingLocation = referralReportingLocation4
      referral5.referralReportingLocation = referralReportingLocation5
      referral6.referralReportingLocation = referralReportingLocation6
      referral7.referralReportingLocation = referralReportingLocation7
      referral8.referralReportingLocation = referralReportingLocation8

      referral1.referralCohortHistories = mutableSetOf(cohortHistory1)
      referral2.referralCohortHistories = mutableSetOf(cohortHistory2)
      referral3.referralCohortHistories = mutableSetOf(cohortHistory3)
      referral4.referralCohortHistories = mutableSetOf(cohortHistory4)
      referral5.referralCohortHistories = mutableSetOf(cohortHistory5)
      referral6.referralCohortHistories = mutableSetOf(cohortHistory6)
      referral7.referralCohortHistories = mutableSetOf(cohortHistory7)
      referral8.referralCohortHistories = mutableSetOf(cohortHistory8)

      testDataGenerator.createReferralWithFields(
        referral1,
        listOf(statusHistory1, statusHistory1, cohortHistory1, referralReportingLocation1),
      )
      testDataGenerator.createReferralWithFields(
        referral2,
        listOf(statusHistory2, statusHistory2, cohortHistory2, referralReportingLocation2),
      )
      testDataGenerator.createReferralWithFields(
        referral3,
        listOf(statusHistory3, statusHistory3, cohortHistory3, referralReportingLocation3),
      )
      testDataGenerator.createReferralWithFields(
        referral4,
        listOf(statusHistory4, statusHistory4, cohortHistory4, referralReportingLocation4),
      )
      testDataGenerator.createReferralWithFields(
        referral5,
        listOf(statusHistory5, statusHistory5, cohortHistory5, referralReportingLocation5),
      )
      testDataGenerator.createReferralWithFields(
        referral6,
        listOf(statusHistory6, statusHistory6, cohortHistory6, referralReportingLocation6),
      )
      testDataGenerator.createReferralWithFields(
        referral7,
        listOf(statusHistory7, statusHistory7, cohortHistory7, referralReportingLocation7),
      )
      testDataGenerator.createReferralWithFields(
        referral8,
        listOf(statusHistory8, statusHistory8, cohortHistory8, referralReportingLocation8),
      )

      val referralCaseListItemViews = referralCaseListItemRepository.findAll()
      assertThat(referralCaseListItemViews).hasSize(8)
      assertThat(referralCaseListItemViews.map { it.crn })
        .containsExactlyInAnyOrder(
          "X7182552",
          "CRN-999999",
          "CRN-888888",
          "CRN-777777",
          "CRN-66666",
          "CRN-555555",
          "CRN-111111",
          "CRN-888888",
        )
    }

    @Test
    fun `getCaseListItems for OPEN referrals return 200 and paged list of referral case list items`() {
      // Given & When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      // Then
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
        assertThat(item).hasFieldOrProperty("sentenceEndDate")
        assertThat(item).hasFieldOrProperty("sentenceEndDateSource")
        assertThat(item).hasFieldOrProperty("statusLabelColour")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
      assertThat(response.filters).isNotNull
      assertThat(response.filters.statusFilterValues.open).contains("Breach", "On programme")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).containsExactlyInAnyOrder(pduWithComma, pduSecondary, "UNKNOWN_PDU_NAME")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).doesNotContain("OTHER_REGION_PDU")
      assertThat(response.filters.cohort).containsAll(ProgrammeGroupCohort.entries.map { it.label })
    }

    @Test
    fun `getCaseListItems for OPEN referrals include manual override regions in results and filters`() {
      // Given
      userRegionOverrideRepository.save(
        UserRegionOverrideEntity(
          username = "AUTH_ADM",
          regionName = "OTHER REGION",
          createdAt = LocalDateTime.now(),
          createdBy = "AUTH_ADM",
        ),
      )

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      // Then
      assertThat(response.pagedReferrals.totalElements).isEqualTo(7)
      assertThat(response.filters.locationFilterValues.map { it.pduName }).contains("OTHER_REGION_PDU")
    }

    @Test
    fun `getCaseListItems for OPEN referrals can use manual override regions when user has no nDelius teams`() {
      // Given
      nDeliusApiStubs.stubUserTeamsResponse(
        "AUTH_ADM",
        NDeliusUserTeams(teams = emptyList()),
      )
      userRegionOverrideRepository.save(
        UserRegionOverrideEntity(
          username = "AUTH_ADM",
          regionName = "WIREMOCKED REGION",
          createdAt = LocalDateTime.now(),
          createdBy = "AUTH_ADM",
        ),
      )

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      // Then
      assertThat(response.pagedReferrals.totalElements).isEqualTo(6)
      assertThat(response.filters.locationFilterValues.map { it.pduName }).containsExactlyInAnyOrder(pduWithComma, pduSecondary, "UNKNOWN_PDU_NAME")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).doesNotContain("OTHER_REGION_PDU")
    }

    @Test
    fun `getCaseListItems for OPEN referrals return 200 and paged list of referral case list items where some referrals are LAO`() {
      // Given
      testReferralHelper.createReferrals(10)
      // & When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      // Then
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
        assertThat(item).hasFieldOrProperty("sentenceEndDate")
        assertThat(item).hasFieldOrProperty("sentenceEndDateSource")
        assertThat(item).hasFieldOrProperty("statusLabelColour")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
      assertThat(response.filters).isNotNull
      assertThat(response.filters.statusFilterValues.open).contains("Breach", "On programme")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).containsExactlyInAnyOrder(pduWithComma, pduSecondary, "UNKNOWN_PDU_NAME")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).doesNotContain("OTHER_REGION_PDU")
      assertThat(response.filters.cohort).containsAll(ProgrammeGroupCohort.entries.map { it.label })
    }

    @Test
    @Disabled("Disabled due to running time of test creating 501 referrals")
    fun `getCaseListItems for OPEN referrals returns smaller page when 500 referrals are LAO`() {
      // Given
      testReferralHelper.createReferrals(510)
      // & When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      // Then
      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(6)
      assertThat(referralCaseListItems.map { it.crn })
        .containsExactlyInAnyOrder("X7182552", "CRN-999999", "CRN-888888", "CRN-777777", "CRN-66666", "CRN-555555")
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
        assertThat(item).hasFieldOrProperty("sentenceEndDate")
        assertThat(item).hasFieldOrProperty("sentenceEndDateSource")
        assertThat(item).hasFieldOrProperty("statusLabelColour")
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
        assertThat(item).hasFieldOrProperty("sentenceEndDate")
        assertThat(item).hasFieldOrProperty("sentenceEndDateSource")
        assertThat(item).hasFieldOrProperty("statusLabelColour")
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
        assertThat(item).hasFieldOrProperty("sentenceEndDate")
        assertThat(item).hasFieldOrProperty("sentenceEndDateSource")
        assertThat(item).hasFieldOrProperty("statusLabelColour")
      }
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems for OPEN and search by personName and cohort returns matching referrals`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?crnOrPersonName=Alex River&cohort=Sexual offence",
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
      assertThat(referral.statusLabelColour).isEqualTo("purple")
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems returns matching referrals when only cohort is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?cohort=General offence",
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
    fun `getCaseListItems returns no results when cohort with LDC is used and no records exist`() {
      // Given and When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?cohort=General offence LDC",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(0)
      assertThat(response.pagedReferrals.content).isEmpty()
      assertThat(response.otherTabTotal).isEqualTo(0)
    }

    @Test
    fun `getCaseListItems returns matching referrals when pdu is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(3)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.pdu).isEqualTo(pduWithComma)
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("pdu")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems returns matching referrals when multiple pdus are used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduWithComma)}&pdu=${encodeQueryParamValue(pduSecondary)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )
      val referralCaseListItems = response.pagedReferrals.content

      assertThat(response).isNotNull
      assertThat(response.pagedReferrals.totalElements).isEqualTo(5)

      assertThat(referralCaseListItems)
        .allSatisfy { item ->
          assertThat(item.pdu).isIn(pduWithComma, pduSecondary)
        }

      referralCaseListItems.forEach { item ->
        assertThat(item).hasFieldOrProperty("crn")
        assertThat(item).hasFieldOrProperty("personName")
        assertThat(item).hasFieldOrProperty("referralStatus")
        assertThat(item).hasFieldOrProperty("pdu")
      }
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems returns matching referrals when reporting team is used as part of request`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduWithComma)}&reportingTeam=reportingTeam1",
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
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduWithComma)}&reportingTeam=reportingTeam1&reportingTeam=reportingTeam2",
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
      assertThat(response.otherTabTotal).isEqualTo(1)
    }

    @Test
    fun `getCaseListItems for OPEN referrals supports single comma-containing pdu query value`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(3)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item -> assertThat(item.pdu).isEqualTo(pduWithComma) }
    }

    @Test
    fun `getCaseListItems for OPEN referrals supports repeated pdu query values including comma-containing names`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?pdu=${encodeQueryParamValue(pduSecondary)}&pdu=${encodeQueryParamValue(pduWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(5)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item ->
          assertThat(item.pdu).isIn(pduWithComma, pduSecondary)
        }
    }

    @Test
    fun `getCaseListItems for CLOSED referrals supports single comma-containing pdu query value`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed?pdu=${encodeQueryParamValue(pduWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item -> assertThat(item.pdu).isEqualTo(pduWithComma) }
    }

    @Test
    fun `getCaseListItems for CLOSED referrals supports repeated pdu query values including comma-containing names`() {
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed?pdu=${encodeQueryParamValue(pduSecondary)}&pdu=${encodeQueryParamValue(pduWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item -> assertThat(item.pdu).isEqualTo(pduWithComma) }
    }

    @Test
    fun `getCaseListItems for OPEN referrals supports single comma-containing reportingTeam query value`() {
      val reportingTeamWithComma = "Team A, Manchester"

      // Create a referral with a reportingTeam containing a comma
      val referral = ReferralEntityFactory()
        .withPersonName("Test Person")
        .withCrn("CRN-COMMA-TEAM")
        .withInterventionName("Horizon")
        .produce()
      val reportingLocation = ReferralReportingLocationFactory(referral)
        .withPduName(pduWithComma)
        .withReportingTeam(reportingTeamWithComma)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral.referralReportingLocation = reportingLocation

      val awaitingAssessmentStatus = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral, awaitingAssessmentStatus)
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referral).produce()
      referral.referralCohortHistories = mutableSetOf(cohortHistory)

      testDataGenerator.createReferralWithFields(referral, listOf(statusHistory, reportingLocation))

      nDeliusApiStubs.stubAccessCheck(true, "CRN-COMMA-TEAM")

      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?reportingTeam=${encodeQueryParamValue(reportingTeamWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item -> assertThat(item.reportingTeam).isEqualTo(reportingTeamWithComma) }
    }

    @Test
    fun `getCaseListItems for OPEN referrals supports repeated reportingTeam query values including comma-containing names`() {
      val reportingTeamWithComma = "Team B, London"
      val reportingTeamNormal = "Team C"

      // Create first referral with comma-containing team
      val referral1 = ReferralEntityFactory()
        .withPersonName("Person One")
        .withCrn("CRN-MULTI-TEAM-1")
        .withInterventionName("Horizon")
        .produce()
      val reportingLocation1 = ReferralReportingLocationFactory(referral1)
        .withPduName(pduWithComma)
        .withReportingTeam(reportingTeamWithComma)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral1.referralReportingLocation = reportingLocation1

      // Create second referral with normal team name
      val referral2 = ReferralEntityFactory()
        .withPersonName("Person Two")
        .withCrn("CRN-MULTI-TEAM-2")
        .withInterventionName("Building Choices")
        .produce()
      val reportingLocation2 = ReferralReportingLocationFactory(referral2)
        .withPduName(pduSecondary)
        .withReportingTeam(reportingTeamNormal)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral2.referralReportingLocation = reportingLocation2

      val awaitingAssessmentStatus = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val statusHistory1 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral1, awaitingAssessmentStatus)
      val statusHistory2 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral2, awaitingAssessmentStatus)

      val cohortHistory1 = ReferralCohortHistoryFactory().withReferral(referral1).produce()
      val cohortHistory2 = ReferralCohortHistoryFactory().withReferral(referral2).produce()
      referral1.referralCohortHistories = mutableSetOf(cohortHistory1)
      referral2.referralCohortHistories = mutableSetOf(cohortHistory2)

      testDataGenerator.createReferralWithFields(referral1, listOf(statusHistory1, reportingLocation1))
      testDataGenerator.createReferralWithFields(referral2, listOf(statusHistory2, reportingLocation2))

      nDeliusApiStubs.stubAccessCheck(true, "CRN-MULTI-TEAM-1", "CRN-MULTI-TEAM-2")

      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/open?reportingTeam=${encodeQueryParamValue(reportingTeamWithComma)}&reportingTeam=${encodeQueryParamValue(reportingTeamNormal)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(2)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item ->
          assertThat(item.reportingTeam).isIn(reportingTeamWithComma, reportingTeamNormal)
        }
    }

    @Test
    fun `getCaseListItems for CLOSED referrals supports single comma-containing reportingTeam query value`() {
      val reportingTeamWithComma = "Team D, Birmingham"

      // Create a closed referral with a reportingTeam containing a comma
      val referral = ReferralEntityFactory()
        .withPersonName("Closed Person")
        .withCrn("CRN-CLOSED-COMMA")
        .withInterventionName("New Me")
        .produce()
      val reportingLocation = ReferralReportingLocationFactory(referral)
        .withPduName(pduWithComma)
        .withReportingTeam(reportingTeamWithComma)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral.referralReportingLocation = reportingLocation

      val programmeCompleteStatus = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral, programmeCompleteStatus)
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referral).produce()
      referral.referralCohortHistories = mutableSetOf(cohortHistory)

      testDataGenerator.createReferralWithFields(referral, listOf(statusHistory, reportingLocation))

      nDeliusApiStubs.stubAccessCheck(true, "CRN-CLOSED-COMMA")

      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed?reportingTeam=${encodeQueryParamValue(reportingTeamWithComma)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(1)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item -> assertThat(item.reportingTeam).isEqualTo(reportingTeamWithComma) }
    }

    @Test
    fun `getCaseListItems for CLOSED referrals supports repeated reportingTeam query values including comma-containing names`() {
      val reportingTeamWithComma = "Team E, Leeds"
      val reportingTeamWithAmpersand = "Team F & Associates"

      // Create first closed referral with comma-containing team
      val referral1 = ReferralEntityFactory()
        .withPersonName("Closed One")
        .withCrn("CRN-CLOSED-MULTI-1")
        .withInterventionName("Horizon")
        .produce()
      val reportingLocation1 = ReferralReportingLocationFactory(referral1)
        .withPduName(pduWithComma)
        .withReportingTeam(reportingTeamWithComma)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral1.referralReportingLocation = reportingLocation1

      // Create second closed referral with ampersand in team name
      val referral2 = ReferralEntityFactory()
        .withPersonName("Closed Two")
        .withCrn("CRN-CLOSED-MULTI-2")
        .withInterventionName("Building Choices")
        .produce()
      val reportingLocation2 = ReferralReportingLocationFactory(referral2)
        .withPduName(pduSecondary)
        .withReportingTeam(reportingTeamWithAmpersand)
        .withRegionName("WIREMOCKED REGION")
        .produce()
      referral2.referralReportingLocation = reportingLocation2

      val programmeCompleteStatus = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
      val statusHistory1 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral1, programmeCompleteStatus)
      val statusHistory2 = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.now())
        .withCreatedBy("USER_ID_12345")
        .withStartDate(LocalDateTime.now())
        .produce(referral2, programmeCompleteStatus)

      val cohortHistory1 = ReferralCohortHistoryFactory().withReferral(referral1).produce()
      val cohortHistory2 = ReferralCohortHistoryFactory().withReferral(referral2).produce()
      referral1.referralCohortHistories = mutableSetOf(cohortHistory1)
      referral2.referralCohortHistories = mutableSetOf(cohortHistory2)

      testDataGenerator.createReferralWithFields(referral1, listOf(statusHistory1, reportingLocation1))
      testDataGenerator.createReferralWithFields(referral2, listOf(statusHistory2, reportingLocation2))

      nDeliusApiStubs.stubAccessCheck(true, "CRN-CLOSED-MULTI-1", "CRN-CLOSED-MULTI-2")

      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/pages/caselist/closed?reportingTeam=${encodeQueryParamValue(reportingTeamWithComma)}&reportingTeam=${encodeQueryParamValue(reportingTeamWithAmpersand)}",
        object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {},
      )

      assertThat(response.pagedReferrals.totalElements).isEqualTo(2)
      assertThat(response.pagedReferrals.content)
        .allSatisfy { item ->
          assertThat(item.reportingTeam).isIn(reportingTeamWithComma, reportingTeamWithAmpersand)
        }
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

    @Test
    fun `getCaseListItems for OPEN referrals returns only locations for the authenticated user region`() {
      // Given
      val otherUsername = "OTHER_USER"
      nDeliusApiStubs.stubUserTeamsResponse(
        otherUsername,
        NDeliusUserTeams(
          teams = listOf(
            NDeliusUserTeam(
              code = "TEAM_OTHER",
              description = "Other Team",
              pdu = CodeDescription("PDU_OTHER", "Other PDU"),
              region = CodeDescription("REGION_OTHER", "OTHER REGION"),
            ),
          ),
        ),
      )

      // When
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/pages/caselist/open")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(username = otherUsername))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<PagedCaseListReferrals<ReferralCaseListItem>>() {})
        .returnResult().responseBody!!

      // Then
      assertThat(response.filters.locationFilterValues.map { it.pduName }).containsExactly("OTHER_REGION_PDU")
      assertThat(response.filters.locationFilterValues.map { it.pduName }).doesNotContain(pduWithComma, pduSecondary, "UNKNOWN_PDU_NAME")
    }

    private fun encodeQueryParamValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
  }
}
