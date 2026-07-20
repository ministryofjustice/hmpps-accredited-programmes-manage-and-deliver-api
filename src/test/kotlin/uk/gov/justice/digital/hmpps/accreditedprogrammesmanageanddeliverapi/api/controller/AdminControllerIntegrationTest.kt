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
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.StatusUpdateResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateReferralStatusHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.Duration.ofMillis
import java.util.UUID

class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
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
    val referralStatusDescriptionId = UUID.fromString("76b2f8d8-260c-4766-a716-de9325292609")
    val additionalDetails = "This is a test comment"

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/admin/referral/${referralEntity.id}/force-status",
      object : ParameterizedTypeReference<StatusUpdateResponse>() {},
      body = CreateReferralStatusHistory(
        referralStatusDescriptionId = referralStatusDescriptionId,
        additionalDetails = additionalDetails,
      ),
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response).isNotNull()
    assertThat(response.referralStatusHistory).isNotNull()
    assertThat(response.referralStatusHistory.referralStatusDescriptionId).isEqualTo(referralStatusDescriptionId)
    assertThat(response.referralStatusHistory.additionalDetails).isEqualTo(additionalDetails)

    val updatedReferralResult = referralRepository.findByIdOrNull(referralEntity.id!!)
    assertThat(updatedReferralResult).isNotNull()
    assertThat(updatedReferralResult!!.statusHistories).isNotEmpty()
    assertThat(updatedReferralResult.statusHistories.first().referralStatusDescription.id).isEqualTo(
      referralStatusDescriptionId,
    )
    assertThat(updatedReferralResult.statusHistories.first().additionalDetails).isEqualTo(additionalDetails)
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
