package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SentenceInformation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiOfficeLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescriptionList
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var preferredDeliveryLocationRepository: PreferredDeliveryLocationRepository
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
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
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

      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)
      assertThat(response.interventionName).isEqualTo(savedReferral.interventionName)
      assertThat(response.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      assertThat(response.createdAt).isEqualTo(savedReferral.createdAt.toLocalDate())
      assertThat(response.cohort).isEqualTo(savedReferral.cohort)
      assertThat(response.probationPractitionerName)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner!!.name.getNameAsString())
      assertThat(response.probationPractitionerEmail)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.email)
    }

    @Test
    fun `should return referral details object with personal details when access granted is true and no middle name`() {
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
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

      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)
      assertThat(response.interventionName).isEqualTo(savedReferral.interventionName)
      assertThat(response.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      assertThat(response.createdAt).isEqualTo(savedReferral.createdAt.toLocalDate())
      assertThat(response.cohort).isEqualTo(savedReferral.cohort)
      assertThat(response.probationPractitionerName)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner!!.name.getNameAsString())
      assertThat(response.probationPractitionerEmail)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.email)
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.SEXUAL_OFFENCE).produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.FORBIDDEN.value(),
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
      val referralEntity = ReferralEntityFactory()
        .withCrn(nDeliusPersonalDetails.crn)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
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

      assertThat(response.crn).isEqualTo(nDeliusPersonalDetails.crn)
      assertThat(response.name).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      assertThat(response.ethnicity).isEqualTo(nDeliusPersonalDetails.ethnicity?.description)
      assertThat(response.age).isEqualTo(nDeliusPersonalDetails.age)
      assertThat(response.gender).isEqualTo(nDeliusPersonalDetails.sex.description)
      assertThat(response.setting).isEqualTo(referralEntity.setting)
      assertThat(response.probationDeliveryUnit)
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit!!.description)
    }

    @Test
    fun `should return personal details when access granted is true when ethnicity is null`() {
      val nDeliusPersonalDetails =
        NDeliusPersonalDetailsFactory().withEthnicity(null).withDateOfBirth(LocalDate.of(1990, 1, 1)).produce()
      val referralEntity = ReferralEntityFactory()
        .withCrn(nDeliusPersonalDetails.crn)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
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

      assertThat(response.crn).isEqualTo(nDeliusPersonalDetails.crn)
      assertThat(response.name).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      assertThat(response.ethnicity).isNull()
      assertThat(response.age).isEqualTo(nDeliusPersonalDetails.age)
      assertThat(response.gender).isEqualTo(nDeliusPersonalDetails.sex.description)
      assertThat(response.setting).isEqualTo(referralEntity.setting)
      assertThat(response.probationDeliveryUnit)
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit!!.description)
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.SEXUAL_OFFENCE).produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/personal-details",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.FORBIDDEN.value(),
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
            .withDate(LocalDate.of(2023, 1, 1))
            .produce(),
        )
        .withAdditionalOffences(
          listOf(
            OffenceFactory()
              .withMainCategoryCode("23")
              .withSubCategoryCode("13")
              .withMainCategoryDescription("Jaywalking")
              .withSubCategoryDescription("Steeple chase")
              .withDate(LocalDate.of(2020, 6, 13))
              .produce(),
          ),
        ).produce()

      nDeliusApiStubs.stubSuccessfulOffencesResponse(
        referralEntity.crn,
        referralEntity.eventNumber.toString(),
        offences,
      )

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
      assertThat(response.mainOffence.offenceDate).isEqualTo(LocalDate.of(2023, 1, 1))
      assertThat(response.importedDate).isEqualTo(LocalDate.now())

      assertThat(response.additionalOffences).hasSize(1)
      val additionalOffence = response.additionalOffences[0]
      assertThat(additionalOffence.offence).isEqualTo("Jaywalking")
      assertThat(additionalOffence.offenceCode).isEqualTo("23")
      assertThat(additionalOffence.categoryCode).isEqualTo("13")
      assertThat(additionalOffence.category).isEqualTo("Steeple chase")
      assertThat(additionalOffence.offenceDate).isEqualTo(LocalDate.of(2020, 6, 13))
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
    fun `should return 404 when referral exists but no offence history exists for referral`() {
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

    @Test
    fun `should return BAD REQUEST 400 when attempting to find offence history for a referral without an event number`() {
      // Given
      val referralEntity = ReferralEntityFactory()
        .withEventNumber(null)
        .produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When & Then
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/offence-history",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get sentence information endpoint")
  inner class GetSentenceInformation {
    @Test
    fun `should return sentence information for a referral on a licence condition`() {
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val nDeliusSentenceResponse: NDeliusSentenceResponse = NDeliusSentenceResponseFactory()
        .withLicenceConditions(createCodeDescriptionList(2))
        .withLicenceExpiryDate(LocalDate.now().plusYears(1))
        .produce()
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralEntity.crn,
        referralEntity.eventNumber,
        nDeliusSentenceResponse,
      )

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${referralEntity.id}/sentence-information",
        returnType = object : ParameterizedTypeReference<SentenceInformation>() {},
      )

      assertThat(response).hasFieldOrProperty("sentenceType")
      assertThat(response).hasFieldOrProperty("releaseType")
      assertThat(response).hasFieldOrProperty("licenceConditions")
      assertThat(response).hasFieldOrProperty("licenceEndDate")
      assertThat(response).hasFieldOrProperty("postSentenceSupervisionStartDate")
      assertThat(response).hasFieldOrProperty("postSentenceSupervisionEndDate")
      assertThat(response).hasFieldOrProperty("twoThirdsPoint")
      assertThat(response).hasFieldOrProperty("orderRequirements")
      assertThat(response).hasFieldOrProperty("orderEndDate")
      assertThat(response).hasFieldOrProperty("dateRetrieved")

      assertThat(response.sentenceType).isEqualTo(nDeliusSentenceResponse.description)
      assertThat(response.releaseType).isEqualTo(nDeliusSentenceResponse.releaseType)
      assertThat(response.postSentenceSupervisionStartDate)
        .isEqualTo(nDeliusSentenceResponse.licenceExpiryDate!!.plusDays(1))
    }

    @Test
    fun `should throw error when event number is null`() {
      val referralEntity = ReferralEntityFactory().withEventNumber(null).produce()
      testDataGenerator.createReferral(referralEntity)

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${referralEntity.id}/sentence-information",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.BAD_REQUEST.value(),
      )
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralEntity.crn,
        referralEntity.eventNumber,
      )

      webTestClient
        .method(HttpMethod.GET)
        .uri("/referral-details/${referralEntity.id}/sentence-information")
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
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${UUID.randomUUID()}/sentence-information",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Update cohort of referral")
  inner class UpdateCohort {
    @Test
    fun `should update referral with cohort information`() {
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.GENERAL_OFFENCE).produce()
      testDataGenerator.createReferral(referralEntity)

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${referralEntity.id}/update-cohort",
        body = OffenceCohort.SEXUAL_OFFENCE,
        expectedResponseStatus = HttpStatus.NO_CONTENT.value(),
      )

      val referralById = testDataGenerator.getReferralById(referralEntity.id!!)
      assertThat(referralById.cohort.name).isEqualTo(OffenceCohort.SEXUAL_OFFENCE.name)
    }

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${UUID.randomUUID()}/update-cohort",
        body = OffenceCohort.SEXUAL_OFFENCE,
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get manager for referral endpoint")
  inner class GetManagerForReferral {
    @Test
    fun `should return manager details when requirement is found`() {
      val crn = "X123456"
      val eventId = "REQ001"
      val referralEntity = ReferralEntityFactory()
        .withCrn(crn)
        .withEventId(eventId)
        .withSourcedFrom(null)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()

      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(crn)[0]

      // Use the existing wiremock mappings that return successful responses for requirement lookup
      nDeliusApiStubs.stubAccessCheck(granted = true, crn)
      val expectedManager = RequirementOrLicenceConditionManager(
        staff = RequirementStaff(
          code = "STAFF001",
          name = FullName(forename = "Wiremocked-Sarah", surname = "Johnson"),
        ),
        team = CodeDescription(code = "TEAM001", description = "(Wiremocked) Community Offender Management Team"),
        probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(code = "PDU001", description = "(Wiremocked) London PDU"),
        officeLocations = listOf(
          NDeliusApiOfficeLocation(code = "OFF001", description = "(Wiremocked) Waterloo Office"),
          NDeliusApiOfficeLocation(code = "OFF002", description = "(Wiremocked) Victoria Office"),
        ),
      )

      val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponse(manager = expectedManager)
      nDeliusApiStubs.stubSuccessfulRequirementManagerResponse("X123456", "REQ001", requirementResponse)

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/manager",
        returnType = object : ParameterizedTypeReference<RequirementOrLicenceConditionManager>() {},
      )

      // Verify the response contains the mocked data from nDeliusMock.json
      assertThat(response.staff.code).isEqualTo("STAFF001")
      assertThat(response.staff.name.forename).isEqualTo("Wiremocked-Sarah")
      assertThat(response.staff.name.surname).isEqualTo("Johnson")
      assertThat(response.team.code).isEqualTo("TEAM001")
      assertThat(response.team.description).isEqualTo("(Wiremocked) Community Offender Management Team")
      assertThat(response.probationDeliveryUnit.code).isEqualTo("PDU001")
      assertThat(response.probationDeliveryUnit.description).isEqualTo("(Wiremocked) London PDU")
      assertThat(response.officeLocations).hasSize(2)
      assertThat(response.officeLocations[0].code).isEqualTo("OFF001")
      assertThat(response.officeLocations[0].description).isEqualTo("(Wiremocked) Waterloo Office")
      assertThat(response.officeLocations[1].code).isEqualTo("OFF002")
      assertThat(response.officeLocations[1].description).isEqualTo("(Wiremocked) Victoria Office")
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      val nonExistentReferralId = UUID.randomUUID()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/$nonExistentReferralId/manager",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get preferred delivery locations for referral endpoint")
  inner class GetPreferredDeliveryLocations {
    @Test
    fun `should return preferred delivery locations when referral has delivery location preferences`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val probationDeliveryUnit = PreferredDeliveryLocationProbationDeliveryUnitEntity(
        id = null,
        deliusCode = "PDU001",
        deliusDescription = "Test PDU",
      )
      testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(probationDeliveryUnit)

      val deliveryLocationPreference = DeliveryLocationPreferenceEntity(
        referral = savedReferral,
        locationsCannotAttendText = "Cannot attend evening sessions",
      )

      val preferredLocation1 = PreferredDeliveryLocationEntity(
        deliusCode = "LOC001",
        deliusDescription = "London Office",
        preferredDeliveryLocationProbationDeliveryUnit = probationDeliveryUnit,
      )
      val preferredLocation2 = PreferredDeliveryLocationEntity(
        deliusCode = "LOC002",
        deliusDescription = "Manchester Office",
        preferredDeliveryLocationProbationDeliveryUnit = probationDeliveryUnit,
      )

      deliveryLocationPreference.addPreferredDeliveryLocations(preferredLocation1, preferredLocation2)
      testDataGenerator.createDeliveryLocationPreference(deliveryLocationPreference)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/delivery-location-preferences",
        returnType = object : ParameterizedTypeReference<DeliveryLocationPreferences>() {},
      )

      // Then
      assertThat(response.canAttendLocations).hasSize(2)
      assertThat(response.canAttendLocations).containsExactlyInAnyOrder("London Office", "Manchester Office")
      assertThat(response.cannotAttendLocations).isEqualTo("Cannot attend evening sessions")
    }

    @Test
    fun `should return empty DeliveryLocationPreferences when referral has no delivery location preferences`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val probationDeliveryUnit = PreferredDeliveryLocationProbationDeliveryUnitEntity(
        deliusCode = "PDU001",
        deliusDescription = "Test PDU",
      )
      testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(probationDeliveryUnit)

      val deliveryLocationPreference = DeliveryLocationPreferenceEntity(
        id = null,
        referral = savedReferral,
      )

      testDataGenerator.createDeliveryLocationPreference(deliveryLocationPreference)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/delivery-location-preferences",
        returnType = object : ParameterizedTypeReference<DeliveryLocationPreferences>() {},
      )

      // Then
      assertThat(response.canAttendLocations).isEmpty()
      assertThat(response.cannotAttendLocations).isNull()
      assertThat(response.createdBy).isEqualTo("UNKNOWN_USER")
      assertThat(response.lastUpdatedAt).isNotNull
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      // Given
      val nonExistentReferralId = UUID.randomUUID()

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/$nonExistentReferralId/delivery-location-preferences",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when delivery location details do not exist for referral`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      testDataGenerator.createReferral(referralEntity)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/delivery-location-preferences",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
