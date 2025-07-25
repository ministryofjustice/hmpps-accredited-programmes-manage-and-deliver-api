package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  private val identifier = "X123456"
  private val username = "AUTH_ADM"

  @Test
  fun `should return service user when access granted`() {
    val createdAt = LocalDateTime.now()
    val referralEntity = ReferralEntityFactory().withCreatedAt(createdAt).produce()
    testDataGenerator.createReferral(referralEntity)
    val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

    stubAuthTokenEndpoint()
    stubAccessCheck(granted = true, savedReferral.crn)
    stubPersonalDetailsResponse(savedReferral.crn)
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral-details/${savedReferral.id}",
      returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
    )

    Assertions.assertThat(response.id).isEqualTo(savedReferral.id)
    Assertions.assertThat(response.interventionName).isEqualTo(savedReferral.interventionName)
    Assertions.assertThat(response.personName).isEqualTo("John H Doe")
    Assertions.assertThat(response.createdAt).isEqualToIgnoringNanos(createdAt)
    Assertions.assertThat(response.probationPractitionerName).isEqualTo("Prob Officer")
    Assertions.assertThat(response.probationPractitionerEmail).isEqualTo("prob.officer@example.com")
  }

  @Test
  fun `should return forbidden when access denied`() {
    stubAuthTokenEndpoint()
    stubAccessCheck(granted = false, null)
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.GET,
      uri = "/referral-details/${savedReferral.id}",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = 403,
    )
  }

  private fun stubAccessCheck(granted: Boolean, crn: String?) {
    val id = crn ?: identifier
    val body = """
    {
      "access": [
        {
          "crn": "$id",
          "userExcluded": ${!granted},
          "userRestricted": false
        }
      ]
    }
    """.trimIndent()

    wiremock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/user/$username/access"))
        .willReturn(WireMock.okJson(body)),
    )
  }

  private fun stubPersonalDetailsResponse(crn: String?) {
    val id = crn ?: identifier
    val body = objectMapper.writeValueAsString(
      PersonalDetails(
        crn = id,
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
        probationDeliveryUnit = CodeDescription(code = "PDU1", description = "Central PDU"),
      ),
    )

    wiremock.stubFor(
      WireMock.get(WireMock.urlPathTemplate("/case/$id/personal-details"))
        .willReturn(WireMock.okJson(body)),
    )
  }
}
