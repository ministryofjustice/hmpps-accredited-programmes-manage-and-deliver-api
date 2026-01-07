package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.model.BankHolidaysResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class GovUkApiClientIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var govUkApiClient: GovUkApiClient

  @BeforeEach
  fun setup() {
    wiremock.resetAll()
  }

  @Test
  fun `should return a list of holidays for the uk`() {
    stubAuthTokenEndpoint()
    govUkApiStubs.stubBankHolidaysResponse()
    when (val response = govUkApiClient.getHolidays()) {
      is ClientResult.Success<BankHolidaysResponse> -> {
        assertThat(response.body).isNotNull()
        assertThat(response.body.englandAndWales).isNotNull
        assertThat(response.body.englandAndWales.events).isNotEmpty
        // Attempt to find Xmas day (obvious bank holiday)
        assertThat(response.body.englandAndWales.events.find { it.date == "2025-12-25" }).isNotNull
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
  fun `should retry twice then succeed when client throws`() {
    stubAuthTokenEndpoint()
    val failureResponse = """{"error":"Service unavailable"}"""
    val successResponse = govUkApiStubs.bankHolidaysSuccessResponse

    val scenarioName = "Bank Holidays Retry"

    // First attempt - fail
    wiremock.stubFor(
      get(urlEqualTo("/bank-holidays.json"))
        .inScenario(scenarioName)
        .whenScenarioStateIs(STARTED)
        .willReturn(
          aResponse()
            .withStatus(500)
            .withHeader("Content-Type", "application/json")
            .withBody(failureResponse),
        )
        .willSetStateTo("Attempt 1 Failed"),
    )

    // Second attempt - fail
    wiremock.stubFor(
      get(urlEqualTo("/bank-holidays.json"))
        .inScenario(scenarioName)
        .whenScenarioStateIs("Attempt 1 Failed")
        .willReturn(
          aResponse()
            .withStatus(500)
            .withHeader("Content-Type", "application/json")
            .withBody(failureResponse),
        )
        .willSetStateTo("Attempt 2 Failed"),
    )

    // Third attempt - succeed
    wiremock.stubFor(
      get(urlEqualTo("/bank-holidays.json"))
        .inScenario(scenarioName)
        .whenScenarioStateIs("Attempt 2 Failed")
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(successResponse),
        )
        .willSetStateTo("Succeeded"),
    )

    val result = runCatching {
      govUkApiClient.getHolidays()
    }.getOrElse {
      fail("Call should eventually succeed after retries, but threw: $it")
    }

    // Final successful result
    assertThat(result)
      .isInstanceOf(ClientResult.Success::class.java)

    val success = result as ClientResult.Success<BankHolidaysResponse>

    assertThat(success.body.englandAndWales).isNotNull
    assertThat(success.body.englandAndWales.events).isNotEmpty
    assertThat(
      success.body.englandAndWales.events.any { it.date == "2025-12-25" },
    ).isTrue

    wiremock.verify(
      3,
      getRequestedFor(urlEqualTo("/bank-holidays.json")),
    )
  }

  @Test
  fun `should not retry on bad request`() {
    stubAuthTokenEndpoint()

    val failureResponse = """{"error":"Bad request"}"""

    wiremock.stubFor(
      get(urlEqualTo("/bank-holidays.json"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(failureResponse),
        ),
    )

    val response = govUkApiClient.getHolidays()
    assertThat(response)
      .isInstanceOf(ClientResult.Failure::class.java)

    wiremock.verify(
      1,
      getRequestedFor(urlEqualTo("/bank-holidays.json")),
    )
  }
}
