package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderIdentifiers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class NDeliusIntegrationApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Test
  fun `should return offender identifiers for known CRN`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val identifiers = OffenderIdentifiers(
      crn = crn,
      name = OffenderFullName(forename = "John", middleNames = "William", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      age = "45",
      sex = CodeDescription(code = "M", description = "Male"),
      ethnicity = CodeDescription(code = "W1", description = "White"),
      probationPractitioner = ProbationPractitioner(
        name = OffenderFullName("Sam", "A", "Smith"),
        code = "X321",
        email = "sam.smith@probation.gov.uk",
      ),
      probationDeliveryUnit = ProbationDeliveryUnit(code = "PDU123", description = "North London"),
    )

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(identifiers)),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.getOffenderIdentifiers(crn)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as OffenderIdentifiers
        assertThat(body.crn).isEqualTo("X123456")
        assertThat(body.name.forename).isEqualTo("John")
        assertThat(body.name.middleNames).isEqualTo("William")
        assertThat(body.name.surname).isEqualTo("Doe")
        assertThat(body.dateOfBirth).isEqualTo("1980-01-01")
        assertThat(body.sex.description).isEqualTo("Male")
        assertThat(body.ethnicity.description).isEqualTo("White")
        assertThat(body.probationDeliveryUnit.code).isEqualTo("PDU123")
        assertThat(body.probationDeliveryUnit.description).isEqualTo("North London")
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN`() {
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN123"

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(aResponse().withStatus(404)),
    )

    when (val response = nDeliusIntegrationApiClient.getOffenderIdentifiers(crn)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return access check success for valid user and CRN`() {
    stubAuthTokenEndpoint()
    val username = "jane.doe"
    val crn = "X654321"
    val accessCheck = LimitedAccessOffenderCheck(
      crn = crn,
      userExcluded = false,
      userRestricted = false,
      exclusionMessage = null,
      restrictionMessage = null,
    )

    wiremock.stubFor(
      post(urlEqualTo("/users/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(crn))))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                LimitedAccessOffenderCheckResponse(listOf(accessCheck)),
              ),
            ),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.verifyLaoc(username, listOf(crn))) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = (response.body as LimitedAccessOffenderCheckResponse).access.first()
        assertThat(body.crn).isEqualTo(crn)
        assertThat(body.userExcluded).isFalse()
        assertThat(body.userRestricted).isFalse()
        assertThat(body.exclusionMessage).isNull()
        assertThat(body.restrictionMessage).isNull()
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return FORBIDDEN when user does not have access`() {
    stubAuthTokenEndpoint()
    val username = "john.doe"
    val crn = "X987654"

    wiremock.stubFor(
      post(urlEqualTo("/users/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(crn))))
        .willReturn(aResponse().withStatus(403)),
    )

    when (val response = nDeliusIntegrationApiClient.verifyLaoc(username, listOf(crn))) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.FORBIDDEN)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }
}
