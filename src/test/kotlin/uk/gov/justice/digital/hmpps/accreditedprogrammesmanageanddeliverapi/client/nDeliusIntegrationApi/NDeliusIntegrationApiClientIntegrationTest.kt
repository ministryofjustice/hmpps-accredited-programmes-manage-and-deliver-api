package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderIdentifiers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class NDeliusIntegrationApiClientIntegrationTest : IntegrationTestBase() {
  @Autowired
  lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Test
  fun `should return offender identifiers for known CRN`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val identifiers = OffenderIdentifiers(
      crn = crn,
      nomsNumber = "A1234BC",
      name = OffenderName(forename = "John", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      ethnicity = "White",
      gender = "Male",
      probationDeliveryUnit = ProbationDeliveryUnit(code = "PDU123", description = "North London"),
      setting = "Community",
    )

    wiremock.stubFor(
      get(urlEqualTo("/person/find/$crn"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(identifiers))
        )
    )

    // When
    when (val response = nDeliusIntegrationApiClient.getOffenderIdentifiers(crn)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as OffenderIdentifiers
        assertThat(body.crn).isEqualTo("X123456")
        assertThat(body.nomsNumber).isEqualTo("A1234BC")
        assertThat(body.name.forename).isEqualTo("John")
        assertThat(body.name.surname).isEqualTo("Doe")
        assertThat(body.dateOfBirth).isEqualTo("1980-01-01")
        assertThat(body.ethnicity).isEqualTo("White")
        assertThat(body.gender).isEqualTo("Male")
        assertThat(body.probationDeliveryUnit.code).isEqualTo("PDU123")
        assertThat(body.probationDeliveryUnit.description).isEqualTo("North London")
        assertThat(body.setting).isEqualTo("Community")
      }
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN`() {
    // Given
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN123"

    wiremock.stubFor(
      get(urlEqualTo("/person/find/$crn"))
        .willReturn(aResponse().withStatus(404))
    )

    // When
    when (val response = nDeliusIntegrationApiClient.getOffenderIdentifiers(crn)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return access check success for valid user and CRN`() {
    // Given
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
      get(urlEqualTo("/users/$username/access/$crn"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(accessCheck))
        )
    )

    // When
    when (val response = nDeliusIntegrationApiClient.verifyLaoc(username, crn)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as LimitedAccessOffenderCheck
        assertThat(body.crn).isEqualTo("X654321")
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
    // Given
    stubAuthTokenEndpoint()
    val username = "john.doe"
    val crn = "X987654"

    wiremock.stubFor(
      get(urlEqualTo("/users/$username/access/$crn"))
        .willReturn(aResponse().withStatus(403))
    )

    // When
    when (val response = nDeliusIntegrationApiClient.verifyLaoc(username, crn)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.FORBIDDEN)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }
}
