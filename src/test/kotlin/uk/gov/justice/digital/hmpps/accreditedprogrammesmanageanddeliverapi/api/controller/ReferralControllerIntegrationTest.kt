package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
    stubAuthTokenEndpoint()
  }

  @Nested
  @DisplayName("Get referral-details endpoint")
  inner class GetReferralDetails {
    @Test
    fun `should return referral details object with personal details when access granted is true`() {
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory().withCreatedAt(createdAt).produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      Assertions.assertThat(response.id).isEqualTo(savedReferral.id)
      Assertions.assertThat(response.crn).isEqualTo(savedReferral.crn)
      Assertions.assertThat(response.interventionName).isEqualTo(savedReferral.interventionName)
      Assertions.assertThat(response.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      Assertions.assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      Assertions.assertThat(response.createdAt).isEqualTo(savedReferral.createdAt.toLocalDate())
      Assertions.assertThat(response.probationPractitionerName)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.name.getNameAsString())
      Assertions.assertThat(response.probationPractitionerEmail)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.email)
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
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

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${UUID.randomUUID()}",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get personal details endpoint")
  inner class GetPersonalDetails {
    @Test
    fun `should return personal details when access granted is true`() {
      val nDeliusPersonalDetails =
        NDeliusPersonalDetailsFactory().withDateOfBirth(LocalDate.of(1990, 1, 1)).produce()
      val referralEntity = ReferralEntityFactory().withCrn(nDeliusPersonalDetails.crn).produce()
      testDataGenerator.createReferral(referralEntity)
      referralRepository.findByCrn(referralEntity.crn)[0]

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${referralEntity.id}/personal-details",
        returnType = object : ParameterizedTypeReference<PersonalDetails>() {},
      )

      assertThat(response).hasFieldOrProperty("crn")
      assertThat(response).hasFieldOrProperty("name")
      assertThat(response).hasFieldOrProperty("dateOfBirth")
      assertThat(response).hasFieldOrProperty("ethnicity")
      assertThat(response).hasFieldOrProperty("age")
      assertThat(response).hasFieldOrProperty("gender")
      assertThat(response).hasFieldOrProperty("setting")
      assertThat(response).hasFieldOrProperty("probationDeliveryUnit")

      Assertions.assertThat(response.crn).isEqualTo(nDeliusPersonalDetails.crn)
      Assertions.assertThat(response.name).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      Assertions.assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      Assertions.assertThat(response.ethnicity).isEqualTo(nDeliusPersonalDetails.ethnicity.description)
      Assertions.assertThat(response.age).isEqualTo(nDeliusPersonalDetails.age)
      Assertions.assertThat(response.gender).isEqualTo(nDeliusPersonalDetails.sex.description)
      Assertions.assertThat(response.setting).isEqualTo(referralEntity.setting)
      Assertions.assertThat(response.probationDeliveryUnit)
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit.description)
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/personal-details",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = 403,
      )
    }

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${UUID.randomUUID()}/personal-details",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
