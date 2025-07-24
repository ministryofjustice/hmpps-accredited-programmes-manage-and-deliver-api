package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderIdentifiers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import java.time.LocalDate

class ServiceUserControllerIntegrationTest : IntegrationTestBase() {

  private val identifier = "X123456"
  private val username = "AUTH_ADM"

  @Test
  fun `should return service user when access granted`() {
    stubAuthTokenEndpoint()
    stubAccessCheck(granted = true)
    stubPersonalDetailsResponse()

    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/service-user/$identifier",
      returnType = object : ParameterizedTypeReference<ServiceUser>() {},
    )

    assertThat(response.crn).isEqualTo("X123456")
    assertThat(response.name).isEqualTo("John H Doe")
    assertThat(response.dateOfBirth).isEqualTo(LocalDate.of(1990, 1, 1))
    assertThat(response.gender).isEqualTo("Male")
    assertThat(response.ethnicity).isEqualTo("White")
    assertThat(response.currentPdu).isEqualTo("PDU1")
  }

  @Test
  fun `should return forbidden when access denied`() {
    stubAuthTokenEndpoint()
    stubAccessCheck(granted = false)

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/service-user/$identifier",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = 403,
    )
  }

  @Test
  fun `should return exception for invalid identifier`() {
    val invalidIdentifier = "123INVALID"

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/service-user/$invalidIdentifier",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = 400,
    )
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
      post(urlEqualTo("/user/$username/access"))
        .willReturn(okJson(body)),
    )
  }

  private fun stubPersonalDetailsResponse() {
    val body = objectMapper.writeValueAsString(
      OffenderIdentifiers(
        crn = identifier,
        name = FullName(forename = "John", middleNames = "H", surname = "Doe"),
        dateOfBirth = LocalDate.of(1990, 1, 1).toString(),
        age = "33",
        sex = CodeDescription("M", "Male"),
        ethnicity = CodeDescription("W1", "White"),
        probationPractitioner = ProbationPractitioner(
          name = FullName("Prob", "", "Officer"),
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
