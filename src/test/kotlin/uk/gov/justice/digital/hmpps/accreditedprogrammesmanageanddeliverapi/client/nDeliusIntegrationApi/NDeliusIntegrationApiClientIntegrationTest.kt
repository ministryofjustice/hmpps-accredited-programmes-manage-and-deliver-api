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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDate

class NDeliusIntegrationApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Test
  fun `should return offender identifiers for known CRN`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val identifiers = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "John", middleNames = "William", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      age = "45",
      sex = CodeDescription(code = "M", description = "Male"),
      ethnicity = CodeDescription(code = "W1", description = "White"),
      probationPractitioner = ProbationPractitioner(
        name = FullName("Jane", "A", "Doe"),
        code = "X321",
        email = "jane.doe@probation.gov.uk",
      ),
      probationDeliveryUnit = CodeDescription(code = "PDU123", description = "North London"),
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

    when (val response = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as NDeliusPersonalDetails
        assertThat(body.crn).isEqualTo("X123456")
        assertThat(body.name.forename).isEqualTo("John")
        assertThat(body.name.middleNames).isEqualTo("William")
        assertThat(body.name.surname).isEqualTo("Doe")
        assertThat(body.dateOfBirth).isEqualTo("1980-01-01")
        assertThat(body.sex.description).isEqualTo("Male")
        assertThat(body.ethnicity!!.description).isEqualTo("White")
        assertThat(body.probationDeliveryUnit!!.code).isEqualTo("PDU123")
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

    when (val response = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
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
      post(urlEqualTo("/user/$username/access"))
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

    when (val response = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(crn))) {
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
      post(urlEqualTo("/user/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(crn))))
        .willReturn(aResponse().withStatus(403)),
    )

    when (val response = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(crn))) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.FORBIDDEN)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return offences for known CRN and event number`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val eventNumber = 1

    val mainOffenceDate = LocalDate.of(2022, 5, 15)

    val mainOffence = OffenceFactory()
      .withDate(mainOffenceDate)
      .withMainCategoryCode("63")
      .withMainCategoryDescription("Theft from the person of another")
      .withSubCategoryCode("01")
      .withSubCategoryDescription("Theft from the person of another")
      .produce()

    val additionalOffenceDate1 = LocalDate.of(2021, 7, 23)
    val additionalOffence1 = OffenceFactory()
      .withDate(additionalOffenceDate1)
      .withMainCategoryCode("05")
      .withMainCategoryDescription("Criminal damage")
      .withSubCategoryCode("10")
      .withSubCategoryDescription("Criminal damage - value under £5000")
      .produce()

    val additionalOffenceDate2 = LocalDate.of(2021, 9, 5)
    val additionalOffence2 = OffenceFactory()
      .withDate(additionalOffenceDate2)
      .withMainCategoryCode("04")
      .withMainCategoryDescription("Assault")
      .withSubCategoryCode("01")
      .withSubCategoryDescription("Common assault and battery")
      .produce()

    val offences = OffencesFactory()
      .withMainOffence(mainOffence)
      .withAdditionalOffences(listOf(additionalOffence1, additionalOffence2))
      .produce()

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(offences)),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.getOffences(crn, eventNumber)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as Offences

        // Verify main offence
        assertThat(body.mainOffence.date).isEqualTo(mainOffenceDate)
        assertThat(body.mainOffence.mainCategoryCode).isEqualTo("63")
        assertThat(body.mainOffence.mainCategoryDescription).isEqualTo("Theft from the person of another")
        assertThat(body.mainOffence.subCategoryCode).isEqualTo("01")
        assertThat(body.mainOffence.subCategoryDescription).isEqualTo("Theft from the person of another")

        // Verify additional offences
        assertThat(body.additionalOffences).hasSize(2)

        // First additional offence
        assertThat(body.additionalOffences[0].date).isEqualTo(additionalOffenceDate1)
        assertThat(body.additionalOffences[0].mainCategoryCode).isEqualTo("05")
        assertThat(body.additionalOffences[0].mainCategoryDescription).isEqualTo("Criminal damage")
        assertThat(body.additionalOffences[0].subCategoryCode).isEqualTo("10")
        assertThat(body.additionalOffences[0].subCategoryDescription).isEqualTo("Criminal damage - value under £5000")

        // Second additional offence
        assertThat(body.additionalOffences[1].date).isEqualTo(additionalOffenceDate2)
        assertThat(body.additionalOffences[1].mainCategoryCode).isEqualTo("04")
        assertThat(body.additionalOffences[1].mainCategoryDescription).isEqualTo("Assault")
        assertThat(body.additionalOffences[1].subCategoryCode).isEqualTo("01")
        assertThat(body.additionalOffences[1].subCategoryDescription).isEqualTo("Common assault and battery")
      }
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN or event number when getting offences`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val eventNumber = 999

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(aResponse().withStatus(404)),
    )

    when (val response = nDeliusIntegrationApiClient.getOffences(crn, eventNumber)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }
}
