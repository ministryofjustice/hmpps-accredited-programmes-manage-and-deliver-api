package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

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

    //    Then
    assertEquals(response.ids, listOf("*"))
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

  private fun buildPopulatePersonalDetailsResponse(
    ids: List<String>,
  ): PopulatePersonalDetailsRequest = PopulatePersonalDetailsRequest(referralIds = ids)
}
