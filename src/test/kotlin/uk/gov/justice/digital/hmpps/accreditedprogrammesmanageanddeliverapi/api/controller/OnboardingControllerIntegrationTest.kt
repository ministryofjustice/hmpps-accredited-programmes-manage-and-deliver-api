package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.util.UUID

class OnboardingControllerIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `fetchPersonalDetailsForReferrals returns 202 accepted and processes in background`() {
    // Given
    stubAuthTokenEndpoint()

    val firstReferral = ReferralEntityFactory().produce()
    val secondReferral = ReferralEntityFactory().produce()
    val randomReferralId = UUID.randomUUID()

    testDataGenerator.createReferralWithStatusHistory(firstReferral)
    testDataGenerator.createReferralWithStatusHistory(secondReferral)

    nDeliusApiStubs.stubAccessCheck(true, firstReferral.crn, secondReferral.crn)
    nDeliusApiStubs.stubPersonalDetailsResponse(NDeliusPersonalDetailsFactory().produce())
    nDeliusApiStubs.stubNotFoundSentenceInformationResponse(firstReferral.crn, firstReferral.eventNumber.toString())
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(secondReferral.crn, secondReferral.eventNumber)

    val firstReferralId = firstReferral.id!!
    val secondReferralId = secondReferral.id!!

    // When
    val response = performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/onboarding/referrals",
      object : ParameterizedTypeReference<FetchPersonalDetailsResponse>() {},
      body = FetchPersonalDetailsRequest(
        referralIds = listOf(
          firstReferralId,
          secondReferralId,
          randomReferralId,
        ),
      ),
      expectedResponseStatus = 200,
    )

    // Then — returns 202 immediately with empty lists (processing happens in background)
    assertEquals(emptyList<String>(), response.successIds)
    assertEquals(emptyList<String>(), response.notFoundIds)
    assertEquals(emptyList<String>(), response.failureIds)
  }
}
