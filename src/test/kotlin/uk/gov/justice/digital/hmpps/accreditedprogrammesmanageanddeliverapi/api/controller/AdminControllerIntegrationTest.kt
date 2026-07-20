package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralSentenceReferenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.StatusUpdateResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.CreateReferralStatusHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralSentenceReferenceRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.Duration.ofMillis
import java.util.UUID

class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()

    stubAuthTokenEndpoint()
  }

  @Test
  fun `Refreshing all Personal Details (for every referral) happens with the wildcard`() {
    // Given
    testDataGenerator.createReferralWithStatusHistory()

    // When
    val response = performRequestAndExpectStatusWithBody<PopulatePersonalDetailsResponse>(
      HttpMethod.POST,
      "/admin/populate-personal-details",
      object : ParameterizedTypeReference<PopulatePersonalDetailsResponse>() {},
      body = buildPopulatePersonalDetailsResponse(listOf("*")),
      expectedResponseStatus = 200,
    )

    // Then
    assertEquals(response.ids, listOf("*"))
  }

  @Test
  fun `populatePersonalDetails creates cohort history when referral has no cohort history`() {
    // Given
    stubAuthTokenEndpoint()
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(referralEntity)
    nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
    nDeliusApiStubs.stubPersonalDetailsResponse()
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)
    oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

    assertThat(countReferralCohortHistoryRows(referralEntity.id!!)).isZero()

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/populate-personal-details",
      object : ParameterizedTypeReference<PopulatePersonalDetailsResponse>() {},
      body = buildPopulatePersonalDetailsResponse(listOf(referralEntity.id.toString())),
      expectedResponseStatus = 200,
    )

    // Then
    assertThat(response.ids).containsExactly(referralEntity.id.toString())

    await withPollDelay ofMillis(100) withPollInterval ofMillis(100) untilCallTo {
      countReferralCohortHistoryRows(referralEntity.id!!)
    } matches { it == 1 }

    val latestCohortCreatedBy = jdbcTemplate.queryForObject(
      "select created_by from referral_cohort_history where referral_id = ? order by created_at desc limit 1",
      String::class.java,
      referralEntity.id!!,
    )

    assertThat(latestCohortCreatedBy).isEqualTo("SYSTEM")
  }

  @Test
  fun `cleanUpReferrals - deletes referral when NDelius returns 404`() {
    // Given
    stubAuthTokenEndpoint()
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(referralEntity)

    nDeliusApiStubs.stubNotFoundPersonalDetailsResponse(referralEntity.crn)
    nDeliusApiStubs.stubNotFoundSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber.toString())
    oasysApiStubs.stubNotFoundAssessmentsResponse(referralEntity.crn)

    // When
    performRequestAndExpectStatusNoBody(
      HttpMethod.POST,
      "/admin/clear-missing-data-referrals",
      expectedResponseStatus = 202,
    )

    assertThat(referralRepository.findByIdOrNull(referralEntity.id!!)).isNull()
  }

  @Test
  fun `cleanUpReferrals - deletes referral when NDelius returns 404 and there are appointments for referral`() {
    // Given
    stubAuthTokenEndpoint()
    val referralEntity = testReferralHelper.createReferral()
    val group = testGroupHelper.createGroup()
    testGroupHelper.allocateToGroup(group, referralEntity)

    nDeliusApiStubs.stubPersonalDetailsResponse()
    nDeliusApiStubs.stubNotFoundSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber.toString())

    // When
    performRequestAndExpectStatusNoBody(
      HttpMethod.POST,
      "/admin/clear-missing-data-referrals",
      expectedResponseStatus = 202,
    )

    assertThat(referralRepository.findByIdOrNull(referralEntity.id!!)).isNull()
  }

  @Test
  fun `should force update referral status`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(referralEntity)
    val referralStatusDescription = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val body = CreateReferralStatusHistoryFactory()
      .withReferralStatusDescriptionId(referralStatusDescription.id)
      .produce()

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/${referralEntity.id}/force-status",
      object : ParameterizedTypeReference<StatusUpdateResponse>() {},
      body = body,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response).isNotNull()
    assertThat(response.referralStatusHistory).isNotNull()
    assertThat(response.referralStatusHistory.referralStatusDescriptionId).isEqualTo(body.referralStatusDescriptionId)
    assertThat(response.referralStatusHistory.additionalDetails).isEqualTo(body.additionalDetails)

    val updatedReferralResult = referralRepository.findByIdOrNull(referralEntity.id!!)
    assertThat(updatedReferralResult).isNotNull()
    assertThat(updatedReferralResult!!.statusHistories).isNotEmpty()
    assertThat(updatedReferralResult.statusHistories.first().referralStatusDescription.id).isEqualTo(
      body.referralStatusDescriptionId,
    )
    assertThat(updatedReferralResult.statusHistories.first().additionalDetails).isEqualTo(body.additionalDetails)
  }

  @Test
  fun `should return 404 when force update referral status`() {
    val nonExistentReferralId = UUID.randomUUID()
    val body = CreateReferralStatusHistoryFactory().produce()

    performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/$nonExistentReferralId/force-status",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      body = body,
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `return 401 when unauthorised on force update referral status request`() {
    val nonExistentReferralId = UUID.randomUUID()
    val body = CreateReferralStatusHistoryFactory().produce()

    webTestClient
      .method(HttpMethod.POST)
      .uri("/admin/referral/$nonExistentReferralId/force-status")
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
  fun `should repoint sentence reference for a referral`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(referralEntity)
    val body = ReferralSentenceReferenceRequestFactory().produce()
    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU001",
      description = "East Sussex",
    )

    val primaryOffices = listOf(
      CodeDescription(
        code = "OFFICE-CODE-123",
        description = "Brighton and Hove: Probation Office",
      ),
    )
    val managerDetails = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(
        code = "STAFF001",
        name = FullName(forename = "Jane", surname = "Smith"),
      ),
      team = CodeDescription("TEAM001", "Primary Team"),
      probationDeliveryUnit = primaryPdu,
      officeLocations = primaryOffices,
    )
    val requirementResponse =
      NDeliusCaseRequirementOrLicenceConditionResponse(manager = managerDetails, eventNumber = 1)
    nDeliusApiStubs.stubSuccessfulRequirementManagerResponse(referralEntity.crn, body.eventId, requirementResponse)

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/${referralEntity.id}/repoint-sentence-reference",
      object : ParameterizedTypeReference<ReferralSentenceReferenceResponse>() {},
      body = body,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response).isNotNull()
    assertThat(response.message).isNotNull()
    assertThat(response.message).isEqualTo("Referral with ID: ${referralEntity.id} now has the sourceFrom: ${body.sourcedFrom.name} and eventId: ${body.eventId}.")

    val updatedReferralResult = referralRepository.findByIdOrNull(referralEntity.id!!)
    assertThat(updatedReferralResult).isNotNull()
    assertThat(updatedReferralResult!!.eventId).isEqualTo(body.eventId)
    assertThat(updatedReferralResult.sourcedFrom).isEqualTo(body.sourcedFrom)
  }

  @Test
  fun `should handle error response from nDelius on repoint sentence reference for a referral request`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(referralEntity)
    val body = ReferralSentenceReferenceRequestFactory().produce()
    nDeliusApiStubs.stubNotFoundRequirementManagerResponse(crn = referralEntity.crn, requirementId = body.eventId)

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/${referralEntity.id}/repoint-sentence-reference",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      body = body,
      expectedResponseStatus = HttpStatus.CONFLICT.value(),
    )

    // Then
    assertThat(response).isNotNull()
    assertThat(response.userMessage).isNotNull()
    assertThat(response.userMessage).isEqualTo("Conflict: Cannot repoint referral: the supplied ${body.sourcedFrom} id ${body.eventId} does not exist in nDelius for CRN ${referralEntity.crn}. Confirm the live id with the integration team and retry.")
  }

  @Test
  fun `should return 404 when repoint sentence reference for a referral`() {
    val nonExistentReferralId = UUID.randomUUID()
    val body = ReferralSentenceReferenceRequestFactory().produce()

    performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/$nonExistentReferralId/repoint-sentence-reference",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      body = body,
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `return 401 when unauthorised on repoint sentence reference for referral request`() {
    val nonExistentReferralId = UUID.randomUUID()
    val body = ReferralSentenceReferenceRequestFactory().produce()

    webTestClient
      .method(HttpMethod.POST)
      .uri("/admin/referral/$nonExistentReferralId/repoint-sentence-reference")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }

  private fun buildPopulatePersonalDetailsResponse(
    ids: List<String>,
  ): PopulatePersonalDetailsRequest = PopulatePersonalDetailsRequest(referralIds = ids)

  private fun countReferralCohortHistoryRows(referralId: java.util.UUID): Int = jdbcTemplate.queryForObject(
    "select count(*) from referral_cohort_history where referral_id = ?",
    Int::class.java,
    referralId,
  ) ?: 0
}
