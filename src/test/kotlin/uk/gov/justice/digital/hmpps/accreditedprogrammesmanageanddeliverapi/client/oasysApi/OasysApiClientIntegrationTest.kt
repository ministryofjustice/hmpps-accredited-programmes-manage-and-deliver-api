package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.ScoredAnswer
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class OasysApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var oasysApiClient: OasysApiClient

  @Test
  fun `should return a pni calculation for known crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    stubSuccessfulPniResponse(crn)

    // When
    when (val response = oasysApiClient.getPniCalculation(crn)) {
      // Then
      is ClientResult.Success<*> -> {
        assertThat(response.body).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val pniResponse = response.body as PniResponse
        assertThat(pniResponse).isNotNull()
        assertThat(pniResponse.pniCalculation?.pni).isEqualTo(Type.H)
        assertThat(pniResponse.assessment?.offenderAge).isEqualTo(32)
        assertThat(pniResponse.assessment?.questions?.impulsivity?.score).isEqualTo(ScoredAnswer.Problem.NONE.score)
        assertThat(pniResponse.assessment?.questions?.hostileOrientation?.score).isEqualTo(ScoredAnswer.Problem.SOME.score)
        assertThat(pniResponse.assessment?.questions?.sexualPreOccupation?.score).isEqualTo(ScoredAnswer.Problem.SIGNIFICANT.score)
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

  private fun stubSuccessfulPniResponse(crn: String) {
    val pniResponse = PniResponseFactory().produce()
    wiremock.stubFor(
      get(urlEqualTo("/assessments/pni/$crn?community=true"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(pniResponse)),
        ),
    )
  }

  @Test
  fun `should return NOT FOUND for unknown crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = randomAlphanumericString()
    wiremock.stubFor(
      get(urlEqualTo("/assessments/pni/$crn?community=true"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )

    // When
    when (val response = oasysApiClient.getPniCalculation(crn)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }
}
