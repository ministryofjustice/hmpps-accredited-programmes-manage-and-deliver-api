package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.model.BankHolidaysResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class GovUkApiClientIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var govUkApiClient: GovUkApiClient

  @Test
  fun `should return a list of holidays for the uk`() {
    stubAuthTokenEndpoint()
    when (val response = govUkApiClient.getHolidays()) {
      is ClientResult.Success<BankHolidaysResponse> -> {
        assertThat(response.body).isNotNull()
        assertThat(response.body.englandAndWales).isNotNull
        assertThat(response.body.englandAndWales.events).isNotEmpty
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
}
