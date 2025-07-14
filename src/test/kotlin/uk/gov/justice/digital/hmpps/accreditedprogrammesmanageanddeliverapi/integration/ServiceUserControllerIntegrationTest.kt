package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.*
import java.time.LocalDate

class ServiceUserControllerIntegrationTest : IntegrationTestBase() {

  private val identifier = "X123456"
  private val username = "AUTH_ADM"

  @Test
  fun `should return service user when access granted`() {
    stubAuthTokenEndpoint()
    stubAccessCheck(granted = true)
    stubPersonalDetailsResponse()

    webTestClient.get()
      .uri("/service-user/$identifier")
      .headers(setAuthorisation(roles = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.crn").isEqualTo("X123456")
      .jsonPath("$.name").isEqualTo("John H Doe")
      .jsonPath("$.dob").isEqualTo(listOf(1990, 1, 1))
      .jsonPath("$.gender").isEqualTo("Male")
      .jsonPath("$.ethnicity").isEqualTo("White")
      .jsonPath("$.currentPdu").isEqualTo("PDU1")
  }

  @Test
  fun `should return forbidden when access denied`() {
    stubAuthTokenEndpoint()
    stubAccessCheck(granted = false)

    webTestClient.get()
      .uri("/service-user/$identifier")
      .headers(setAuthorisation(roles = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return exception for invalid identifier`() {
    val invalidIdentifier = "123INVALID"

    webTestClient.get()
      .uri("/service-user/$invalidIdentifier")
      .headers(setAuthorisation(roles = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is5xxServerError
  }

  private fun stubAccessCheck(granted: Boolean) {
    val body = """
    {
      "access": [
        {
          "crn": "$identifier",
          "userExcluded": ${!granted},
          "userRestricted": false
        }
      ]
    }
    """.trimIndent()

    wiremock.stubFor(
      post(urlEqualTo("/users/$username/access"))
        .willReturn(okJson(body)),
    )
  }

  private fun stubPersonalDetailsResponse() {
    val body = objectMapper.writeValueAsString(
      OffenderIdentifiers(
        crn = identifier,
        name = OffenderFullName(forename = "John", middleNames = "H", surname = "Doe"),
        dateOfBirth = LocalDate.of(1990, 1, 1).toString(),
        age = "33",
        sex = CodeDescription("M", "Male"),
        ethnicity = CodeDescription("W1", "White"),
        probationPractitioner = ProbationPractitioner(
          name = OffenderFullName("Prob", "", "Officer"),
          code = "PRAC01",
          email = "prob.officer@example.com",
        ),
        probationDeliveryUnit = ProbationDeliveryUnit(code = "PDU1", description = "Central PDU"),
      ),
    )

    wiremock.stubFor(
      get(urlEqualTo("/case/$identifier/personal-details"))
        .willReturn(okJson(body)),
    )
  }
}
