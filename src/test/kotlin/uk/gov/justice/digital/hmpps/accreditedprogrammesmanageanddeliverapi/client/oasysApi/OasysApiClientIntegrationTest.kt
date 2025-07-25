package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.ScoredAnswer
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs

class OasysApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var oasysApiClient: OasysApiClient

  private lateinit var oasysApiStubs: OasysApiStubs

  @BeforeEach
  fun setup() {
    oasysApiStubs = OasysApiStubs(wiremock, objectMapper)
  }

  @Test
  fun `should return a pni calculation for known crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    oasysApiStubs.stubSuccessfulPniResponse(crn)

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

  @Test
  fun `should return NOT FOUND for unknown crn`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = randomAlphanumericString()
    oasysApiStubs.stubNotFoundPniResponse(crn)

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
