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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RemoveReferralFromGroupStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SentenceInformation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.LdcStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralLdcHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateReferralStatusHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update.UpdateCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.Utils.createCodeDescriptionList
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ReferralControllerIntegrationTest(@Autowired private val programmeGroupMembershipService: ProgrammeGroupMembershipService) : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()

    stubAuthTokenEndpoint()
  }

  @Nested
  @DisplayName("Get referral-details endpoint")
  inner class GetReferralDetails {
    @Test
    fun `should return referral details object with personal details when access granted is true`() {
      // Given
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()

      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )

      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

      val secondStatus = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 16, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
        )

      testDataGenerator.createReferralStatusHistory(secondStatus)

      val groupCode = "AAA111"
      val group = ProgrammeGroupFactory().withCode(groupCode).produce()
      testDataGenerator.createGroup(group)

      val groupMembership =
        ProgrammeGroupMembershipFactory().withReferral(referralEntity).withProgrammeGroup(group).produce()
      testDataGenerator.createGroupMembership(groupMembership)

      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)
      assertThat(response.interventionName).isEqualTo(savedReferral.interventionName)
      assertThat(response.currentStatusDescription).isEqualTo("Awaiting allocation")
      assertThat(response.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
      assertThat(response.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
      assertThat(response.createdAt).isEqualTo(savedReferral.createdAt.toLocalDate())
      assertThat(response.cohort).isEqualTo(savedReferral.cohort)
      assertThat(response.probationPractitionerName)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner!!.name.getNameAsString())
      assertThat(response.probationPractitionerEmail)
        .isEqualTo(nDeliusPersonalDetails.probationPractitioner.email)
      assertThat(response.currentlyAllocatedGroupCode).isEqualTo(groupCode)
      assertThat(response.currentlyAllocatedGroupId).isEqualTo(group.id)
    }

    @Test
    fun `should return referral details object default false LDC status when access granted is true`() {
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
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
      assertThat(response.hasLdc).isEqualTo(LdcStatus.NO_LDC.value)
      assertThat(response.hasLdcDisplayText).isEqualTo(LdcStatus.NO_LDC.displayText)
      assertThat(response.currentlyAllocatedGroupId).isNull()
      assertThat(response.currentlyAllocatedGroupCode).isNull()
    }

    @Test
    fun `should return referral details object with true LDC status when access granted is true`() {
      // Given
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val ldcHistories = ReferralLdcHistoryFactory()
        .withReferral(referralEntity)
        .withHasLdc(true)
        .withCreatedBy("MOCK_USER")
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      testDataGenerator.createLdcHistoryForAReferral(ldcHistories)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      oasysApiStubs.stubSuccessfulPniResponseWithLdc(referralEntity.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
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
      assertThat(response.hasLdc).isEqualTo(LdcStatus.HAS_LDC.value)
      assertThat(response.hasLdcDisplayText).isEqualTo(LdcStatus.HAS_LDC.displayText)
      assertThat(response.currentlyAllocatedGroupId).isNull()
      assertThat(response.currentlyAllocatedGroupCode).isNull()
    }

    @Test
    fun `should return referral details object with personal details when access granted is true and no middle name`() {
      // Given
      val createdAt = LocalDateTime.now()
      val referralEntity = ReferralEntityFactory()
        .withCreatedAt(createdAt)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      val fullName = randomFullName(middleName = null)

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().withName(fullName).produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)
      oasysApiStubs.stubSuccessfulPniResponseWithLdc(referralEntity.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
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
      assertThat(response.currentlyAllocatedGroupId).isNull()
      assertThat(response.currentlyAllocatedGroupCode).isNull()
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.SEXUAL_OFFENCE).produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      oasysApiStubs.stubSuccessfulPniResponseWithLdc(referralEntity.crn)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

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

    @Test
    fun `should update LDC status when PNI response indicates LDC is present with subTotal 3 or higher`() {
      // Given
      val referralEntity = ReferralEntityFactory()
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      // Stub PNI response with LDC subTotal >= 3 (threshold)
      val ldcWithHighSubTotal = Ldc(
        score = 4,
        subTotal = 4, // >= 3, so hasLdc() should return true
      )
      oasysApiStubs.stubSuccessfulPniResponseWithLdc(referralEntity.crn, ldcWithHighSubTotal)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)

      val updatedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      assertThat(updatedReferral.referralLdcHistories).isNotEmpty()
      val latestLdcEntry = updatedReferral.referralLdcHistories.maxByOrNull { it.createdAt!! }
      assertThat(latestLdcEntry?.hasLdc).isTrue()
    }

    @Test
    fun `should not indicate LDC when PNI response has low LDC subTotal below 3`() {
      // Given
      val referralEntity = ReferralEntityFactory()
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      // Stub PNI response with LDC subTotal < 3 (threshold)
      val ldcWithLowSubTotal = Ldc(
        score = 2,
        subTotal = 2, // < 3, so hasLdc() should return false
      )
      oasysApiStubs.stubSuccessfulPniResponseWithLdc(referralEntity.crn, ldcWithLowSubTotal)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)

      val updatedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      assertThat(updatedReferral.referralLdcHistories).isNotEmpty()
      val latestLdcEntry = updatedReferral.referralLdcHistories.maxByOrNull { it.createdAt!! }
      assertThat(latestLdcEntry?.hasLdc).isFalse()
    }

    @Test
    fun `should not update LDC when PNI response has null LDC assessment`() {
      // Given
      val referralEntity = ReferralEntityFactory()
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

      nDeliusApiStubs.stubAccessCheck(granted = true, referralEntity.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(referralEntity.crn, referralEntity.eventNumber)

      // Stub PNI response without LDC (default factory creates null LDC)
      oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralDetails>() {},
      )

      // Then
      assertThat(response.id).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)

      val updatedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      assertThat(updatedReferral.referralLdcHistories).isNotEmpty()
      val latestLdcEntry = updatedReferral.referralLdcHistories.maxByOrNull { it.createdAt!! }
      assertThat(latestLdcEntry?.hasLdc).isFalse()
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit.description)
    }

    @Test
    fun `should return personal details when access granted is true when ethnicity is null`() {
      val nDeliusPersonalDetails =
        NDeliusPersonalDetailsFactory().withEthnicity(null).withDateOfBirth(LocalDate.of(1990, 1, 1)).produce()
      val referralEntity = ReferralEntityFactory()
        .withCrn(nDeliusPersonalDetails.crn)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
        .isEqualTo(nDeliusPersonalDetails.probationDeliveryUnit.description)
    }

    @Test
    fun `should return forbidden when access denied`() {
      nDeliusApiStubs.stubAccessCheck(granted = false)
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.SEXUAL_OFFENCE).produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
  @DisplayName("Update the Status of a Referral")
  inner class UpdateReferralStatusHistory {
    @Test
    fun `should update the Status of a Referral that exists`() {
      // Given
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.GENERAL_OFFENCE).produce()

      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )

      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

      assertThat(testDataGenerator.getReferralById(referralEntity.id!!).statusHistories).hasSize(1)

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/referral/${referralEntity.id}/status-history",
        body = CreateReferralStatusHistory(
          referralStatusDescriptionId = UUID.fromString("76b2f8d8-260c-4766-a716-de9325292609"),
          additionalDetails = "This is a test comment",
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val referralById = testDataGenerator.getReferralById(referralEntity.id!!)
      assertThat(referralById.statusHistories).hasSize(2)
      assertThat(
        referralById.statusHistories.first().additionalDetails,
      ).isEqualTo("This is a test comment")
    }
  }

  @Nested
  @DisplayName("Get the Referral Status history for a Referral")
  inner class GetReferralStatusHistory {
    @Test
    fun `should get the Referral Status history for a Referral that exists`() {
      // Given
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.GENERAL_OFFENCE).produce()

      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 1, 12, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )

      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

      val secondStatusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 15, 20, 30))
        .produce(referralEntity, referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription())

      testDataGenerator.createReferralStatusHistory(secondStatusHistory)

      assertThat(testDataGenerator.getReferralById(referralEntity.id!!).statusHistories).hasSize(2)

      // When
      val body = performRequestAndExpectStatusAndReturnBody(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referralEntity.id}/status-history",
        body = CreateReferralStatusHistory(
          referralStatusDescriptionId = UUID.fromString("76b2f8d8-260c-4766-a716-de9325292609"),
          additionalDetails = "This is a test comment",
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      body.jsonPath("$[0].referralStatusDescriptionId")
        .isEqualTo(referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription().id)
      body.jsonPath("$[0].referralStatusDescriptionName").isEqualTo("Awaiting assessment")

      body.jsonPath("$[1].referralStatusDescriptionId")
        .isEqualTo(referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription().id)
      body.jsonPath("$[1].referralStatusDescriptionName").isEqualTo("Awaiting allocation")
    }
  }

  @Nested
  @DisplayName("Update cohort of referral")
  inner class UpdateReferralCohort {
    @Test
    fun `should update referral with cohort information`() {
      val referralEntity = ReferralEntityFactory().withCohort(OffenceCohort.GENERAL_OFFENCE).produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${referralEntity.id}/update-cohort",
        body = UpdateCohort(OffenceCohort.SEXUAL_OFFENCE),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val referralById = testDataGenerator.getReferralById(referralEntity.id!!)
      assertThat(referralById.cohort.name).isEqualTo(OffenceCohort.SEXUAL_OFFENCE.name)
    }

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.PUT,
        uri = "/referral/${UUID.randomUUID()}/update-cohort",
        body = UpdateCohort(OffenceCohort.SEXUAL_OFFENCE),
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

      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(crn)[0]

      // Use the existing wiremock mappings that return successful responses for requirement lookup
      nDeliusApiStubs.stubAccessCheck(granted = true, crn)
      val expectedManager = RequirementOrLicenceConditionManager(
        staff = RequirementStaff(
          code = "STAFF001",
          name = FullName(forename = "Wiremocked-Sarah", surname = "Johnson"),
        ),
        team = CodeDescription(code = "TEAM001", description = "(Wiremocked) Community Offender Management Team"),
        probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(
          code = "PDU001",
          description = "(Wiremocked) London PDU",
        ),
        officeLocations = listOf(
          CodeDescription(code = "OFF001", description = "(Wiremocked) Waterloo Office"),
          CodeDescription(code = "OFF002", description = "(Wiremocked) Victoria Office"),
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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
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
      testDataGenerator.createPreferredDeliveryLocation(preferredLocation1)
      val preferredLocation2 = PreferredDeliveryLocationEntity(
        deliusCode = "LOC002",
        deliusDescription = "Manchester Office",
        preferredDeliveryLocationProbationDeliveryUnit = probationDeliveryUnit,
      )
      testDataGenerator.createPreferredDeliveryLocation(preferredLocation2)

      deliveryLocationPreference.preferredDeliveryLocations = mutableSetOf(preferredLocation1, preferredLocation2)

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
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      val probationDeliveryUnit = PreferredDeliveryLocationProbationDeliveryUnitEntity(
        deliusCode = "PDU001",
        deliusDescription = "Test PDU",
      )
      testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(probationDeliveryUnit)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/delivery-location-preferences",
        returnType = object : ParameterizedTypeReference<DeliveryLocationPreferences>() {},
      )

      // Then
      assertThat(response.canAttendLocations).isEmpty()
      assertThat(response.cannotAttendLocations).isNull()
      assertThat(response.createdBy).isNull()
      assertThat(response.lastUpdatedAt).isNull()
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
    fun `should return 200 with empty delivery location preferences when delivery location details do not exist for referral`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When
      val result = performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral-details/${savedReferral.id}/delivery-location-preferences",
        object : ParameterizedTypeReference<DeliveryLocationPreferences>() {},
        HttpStatus.OK.value(),
      )

      assertThat(result.cannotAttendLocations).isNull()
      assertThat(result.canAttendLocations).isEmpty()
      assertThat(result.lastUpdatedAt).isNull()
    }
  }

  @Nested
  @DisplayName("Get possible transitions endpoint")
  inner class GetStatusTransitionsForReferral {
    @Test
    fun `should return the status data for the ui form based on a referral id`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/status-transitions/referral/${savedReferral.id}",
        returnType = object : ParameterizedTypeReference<ReferralStatusTransitions>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.currentStatus.title).isEqualTo("Awaiting assessment")
      assertThat(response.availableStatuses).isNotEmpty
    }

    @Test
    fun `should return empty list when referral status description does not exist`() {
      // Given
      val nonExistentId = UUID.randomUUID()

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "bff/status-transitions/referral/$nonExistentId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }

  @Nested
  @DisplayName("Get possible transitions endpoint for Remove from Group form")
  inner class GetRemoveFromGroupStatusTransitions {
    @Test
    fun `should return the status data for the ui form based on a referral id`() {
      // Given
      val theGroup = testDataGenerator.createGroup(ProgrammeGroupFactory().withCode("AAA111").produce())

      val referralEntity = testReferralHelper.createReferralWithStatus(
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )

      programmeGroupMembershipService.allocateReferralToGroup(referralEntity.id!!, theGroup.id!!, "", "")

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/remove-from-group/${referralEntity.id}",
        returnType = object : ParameterizedTypeReference<RemoveReferralFromGroupStatusTransitions>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.currentStatus.title).isEqualTo("Scheduled")
      assertThat(response.availableStatuses).isNotEmpty
      assertThat(response.availableStatuses).hasSize(6)
    }

    @Test
    fun `should return the correct status for a referral with an On programme status`() {
      // Given
      testDataCleaner.cleanAllTables();
      val theGroup = testDataGenerator.createGroup(ProgrammeGroupFactory().withCode("AAA1112").produce())

      val referralEntity = testReferralHelper.createReferralWithStatus(
        referralStatusDescriptionRepository.getOnProgrammeStatusDescription(),
      )

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/remove-from-group/${referralEntity.id}",
        returnType = object : ParameterizedTypeReference<RemoveReferralFromGroupStatusTransitions>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.currentStatus.title).isEqualTo("On programme")
      assertThat(response.availableStatuses).isNotEmpty
      assertThat(response.availableStatuses).hasSize(5)
      assertThat(response.availableStatuses.map { it.status }).containsOnly(
        "Awaiting assessment",
        "Awaiting allocation",
        "Deprioritised",
        "Recall",
        "Return to court"
      )
    }
  }
}
