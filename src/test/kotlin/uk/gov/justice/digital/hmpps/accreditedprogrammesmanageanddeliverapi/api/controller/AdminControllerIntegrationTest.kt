package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService

class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralService: ReferralService

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
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

  private fun buildPopulatePersonalDetailsResponse(
    ids: List<String>,
  ): PopulatePersonalDetailsRequest = PopulatePersonalDetailsRequest(referralIds = ids)
}
