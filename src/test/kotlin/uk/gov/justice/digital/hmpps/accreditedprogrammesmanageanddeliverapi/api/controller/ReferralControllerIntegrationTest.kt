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
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
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
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner!!.name.getNameAsString())
      Assertions.assertThat(response.probationPractitionerEmail)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.email)
    }

    @Test
    fun `should return referral details object with personal details when access granted is true and no middle name`() {
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory().withCreatedAt(createdAt).produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      val fullName = randomFullName(middleName = null)

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().withName(fullName).produce()

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
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner!!.name.getNameAsString())
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
      Assertions.assertThat(response.ethnicity).isEqualTo(nDeliusPersonalDetails.ethnicity!!.description)
      Assertions.assertThat(response.age).isEqualTo(nDeliusPersonalDetails.age)
      Assertions.assertThat(response.gender).isEqualTo(nDeliusPersonalDetails.sex.description)
      Assertions.assertThat(response.setting).isEqualTo(referralEntity.setting)
      Assertions.assertThat(response.probationDeliveryUnit)
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit!!.description)
    }

    @Test
    fun `should return personal details when access granted is true when ethnicity is null`() {
      val nDeliusPersonalDetails =
        NDeliusPersonalDetailsFactory().withEthnicity(null).withDateOfBirth(LocalDate.of(1990, 1, 1)).produce()
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
      Assertions.assertThat(response.ethnicity).isNull()
      Assertions.assertThat(response.age).isEqualTo(nDeliusPersonalDetails.age)
      Assertions.assertThat(response.gender).isEqualTo(nDeliusPersonalDetails.sex.description)
      Assertions.assertThat(response.setting).isEqualTo(referralEntity.setting)
      Assertions.assertThat(response.probationDeliveryUnit)
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit!!.description)
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

  @Nested
  @DisplayName("Get offence history endpoint")
  inner class GetOffenceHistory {
    @Test
    fun `should return offence history for existing referral with offence history`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      val offences = OffencesFactory()
        .withMainOffence(
          OffenceFactory()
            .withMainCategoryCode("56")
            .withSubCategoryCode("01")
            .withMainCategoryDescription("Stealing from shops and stalls")
            .withSubCategoryDescription("Not eating enough vegetables")
            .produce(),
        )
        .withAdditionalOffences(
          listOf(
            OffenceFactory()
              .withMainCategoryCode("23")
              .withSubCategoryCode("13")
              .withMainCategoryDescription("Jaywalking")
              .withSubCategoryDescription("Steeple chase")
              .produce(),
          ),
        ).produce()

      nDeliusApiStubs.stubSuccessfulOffencesResponse(referralEntity.crn, referralEntity.eventNumber.toString(), offences)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/offence-history",
        returnType = object : ParameterizedTypeReference<OffenceHistory>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.mainOffence.offence).isEqualTo("Stealing from shops and stalls")
      assertThat(response.mainOffence.offenceCode).isEqualTo("56")
      assertThat(response.mainOffence.categoryCode).isEqualTo("01")
      assertThat(response.mainOffence.category).isEqualTo("Not eating enough vegetables")

      assertThat(response.additionalOffences).hasSize(1)
      val additionalOffence = response.additionalOffences[0]
      assertThat(additionalOffence.offence).isEqualTo("Jaywalking")
      assertThat(additionalOffence.offenceCode).isEqualTo("23")
      assertThat(additionalOffence.categoryCode).isEqualTo("13")
      assertThat(additionalOffence.category).isEqualTo("Steeple chase")
    }

    @Test
    fun `should return forbidden when access denied`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/referral-details/${savedReferral.id}/offence-history")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `should return 404 when referral is not found`() {
      // Given
      val unknownReferralId = UUID.randomUUID()

      // When & Then
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/$unknownReferralId/offence-history",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when referral exists by not offence history exists for referral`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      nDeliusApiStubs.stubNotFoundOffencesResponse(savedReferral.crn, savedReferral.eventNumber.toString())

      // When & Then
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/offence-history",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
