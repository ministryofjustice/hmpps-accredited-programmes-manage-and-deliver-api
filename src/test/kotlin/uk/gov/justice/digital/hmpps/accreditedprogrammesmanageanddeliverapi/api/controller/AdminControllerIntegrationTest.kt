package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

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
      body = buildPopulatePersonalDetailsResponse(listOf<String>("*")),
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

    // When
    performRequestAndExpectStatusNoBody(
      HttpMethod.POST,
      "/admin/clear-missing-data-referrals",
      expectedResponseStatus = 202,
    )

    Thread.sleep(2000)
    !referralRepository.findById(referralEntity.id!!).isPresent
  }

  private fun buildPopulatePersonalDetailsResponse(
    ids: List<String>,
  ): PopulatePersonalDetailsRequest = PopulatePersonalDetailsRequest(referralIds = ids)
}
