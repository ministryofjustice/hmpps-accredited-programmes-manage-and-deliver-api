package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.util.*

class FindAndReferApiClientIntegrationTest : IntegrationTestBase() {
  @Autowired
  lateinit var findAndReferInterventionApiClient: FindAndReferInterventionApiClient

  @Test
  fun `should return referral details for known referral id`() {
    // Given
    stubAuthTokenEndpoint()
    val referralId = UUID.randomUUID()
    val referralDetails = ReferralDetails(
      interventionName = "Test Intervention",
      interventionType = "ACP",
      referralId = referralId,
      personReference = "X123456",
      personReferenceType = "CRN",
      setting = "Community",
    )

    wiremock.stubFor(
      get(urlEqualTo("/referral/$referralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(referralDetails)),
        ),
    )

    // When
    when (val response = findAndReferInterventionApiClient.getReferral(referralId)) {
      // Then

      is ClientResult.Success<*> -> {
        assertThat(response.body).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val referralDetails = response.body as ReferralDetails
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown  referral id`() {
    // Given
    stubAuthTokenEndpoint()
    val referralId = UUID.randomUUID()
    wiremock.stubFor(
      get(urlEqualTo("/referral/$referralId"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )

    // When
    when (val response = findAndReferInterventionApiClient.getReferral(referralId)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }
}
