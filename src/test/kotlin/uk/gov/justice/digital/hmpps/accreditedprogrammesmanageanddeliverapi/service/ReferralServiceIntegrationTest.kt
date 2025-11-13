package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiOfficeLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomDateOfBirth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

class ReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var reportingLocationRepository: ReferralReportingLocationRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var membershipService: ProgrammeGroupMembershipService

  val personalDetails = NDeliusPersonalDetailsFactory()
    .withName(
      FullName(
        forename = "John",
        middleNames = "Alex",
        surname = "Doe",
      ),
    )
    .withCrn("X123456")
    .withDateOfBirth(LocalDate.parse("2010-10-01"))
    .withSex(CodeDescription("M", "Male"))
    .withTeam(
      CodeDescription(
        code = "1234",
        description = "TEAM_1",
      ),
    )
    .withProbationDeliveryUnit(
      CodeDescription(
        code = "1234",
        description = "PDU_1",
      ),
    )
    .withRegion(
      CodeDescription(
        code = "1234",
        description = "REGION_1",
      ),
    )
    .produce()

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()

    stubAuthTokenEndpoint()

    nDeliusApiStubs.stubPersonalDetailsResponse(personalDetails)
  }

  @Nested
  @DisplayName("AttemptToFindManagerForReferral")
  inner class AttemptToFindManagerForReferral {
    @Test
    fun `attemptToFindManagerForReferral should return manager when requirement endpoint returns 200`() {
      // Given
      val crn = "X123456"
      val eventId = "REQ001"
      val referralEntity = ReferralEntityFactory()
        .withCrn(crn)
        .withEventId(eventId)
        .withSourcedFrom(null)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()

      testDataGenerator.createReferralWithStatusHistory(referralEntity)
      val savedReferral = referralRepository.findByCrn(crn)[0]
      val referralId = savedReferral.id!!.toString()

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
          NDeliusApiOfficeLocation(code = "OFF001", description = "(Wiremocked) Waterloo Office"),
          NDeliusApiOfficeLocation(code = "OFF002", description = "(Wiremocked) Victoria Office"),
        ),
      )

      val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponse(manager = expectedManager)
      nDeliusApiStubs.stubSuccessfulRequirementManagerResponse("X123456", eventId, requirementResponse)

      // When
      val result = referralService.attemptToFindManagerForReferral(UUID.fromString(referralId))

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.staff.code).isEqualTo("STAFF001")
      assertThat(result.staff.name.forename).isEqualTo("Wiremocked-Sarah")
      assertThat(result.staff.name.surname).isEqualTo("Johnson")
      assertThat(result.team.code).isEqualTo("TEAM001")
      assertThat(result.team.description).isEqualTo("(Wiremocked) Community Offender Management Team")
      assertThat(result.probationDeliveryUnit.code).isEqualTo("PDU001")
      assertThat(result.probationDeliveryUnit.description).isEqualTo("(Wiremocked) London PDU")
      assertThat(result.officeLocations).hasSize(2)
      assertThat(result.officeLocations[0].code).isEqualTo("OFF001")
      assertThat(result.officeLocations[0].description).isEqualTo("(Wiremocked) Waterloo Office")
      assertThat(result.officeLocations[1].code).isEqualTo("OFF002")
      assertThat(result.officeLocations[1].description).isEqualTo("(Wiremocked) Victoria Office")
    }

    @Test
    fun `attemptToFindManagerForReferral should return manager when requirement returns 404 but licence condition returns 200`() {
      // Given
      val crn = "X654321"
      val eventId = "LC001"
      val referralEntity = ReferralEntityFactory()
        .withCrn(crn)
        .withEventId(eventId)
        .withSourcedFrom(null)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()

      testDataGenerator.createReferralWithStatusHistory(referralEntity)
      val savedReferral = referralRepository.findByCrn(crn)[0]
      val referralId = savedReferral.id!!.toString()

      val expectedManager = RequirementOrLicenceConditionManager(
        staff = RequirementStaff(
          code = "N03UATU",
          name = FullName(forename = "Wiremock Sam", surname = "Surname"),
        ),
        team = CodeDescription(code = "N03UAT", description = "Unallocated Team(N03)"),
        probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(
          code = "N03UAT",
          description = "Unallocated Level 2(N03)",
        ),
        officeLocations = listOf(
          NDeliusApiOfficeLocation(code = "N03ANPS", description = "All Location"),
        ),
      )

      val licenceConditionResponse = NDeliusCaseRequirementOrLicenceConditionResponse(manager = expectedManager)

      // Stub requirement endpoint to return 404
      nDeliusApiStubs.stubNotFoundRequirementManagerResponse("X654321", eventId)
      // Stub licence condition endpoint to return 200
      nDeliusApiStubs.stubSuccessfulLicenceConditionManagerResponse("X654321", eventId, licenceConditionResponse)

      // When
      val result = referralService.attemptToFindManagerForReferral(UUID.fromString(referralId))

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.staff.code).isEqualTo("N03UATU")
      assertThat(result.staff.name.forename).isEqualTo("Wiremock Sam")
      assertThat(result.staff.name.surname).isEqualTo("Surname")
      assertThat(result.team.code).isEqualTo("N03UAT")
      assertThat(result.team.description).isEqualTo("Unallocated Team(N03)")
      assertThat(result.probationDeliveryUnit.code).isEqualTo("N03UAT")
      assertThat(result.probationDeliveryUnit.description).isEqualTo("Unallocated Level 2(N03)")
      assertThat(result.officeLocations).hasSize(1)
      assertThat(result.officeLocations[0].code).isEqualTo("N03ANPS")
      assertThat(result.officeLocations[0].description).isEqualTo("All Location")
    }

    @Test
    fun `attemptToFindManagerForReferral should return null when both requirement and licence condition return 404`() {
      // Given
      val crn = "X999999"
      val eventId = "UNKNOWN001"
      val referralEntity = ReferralEntityFactory()
        .withCrn(crn)
        .withEventId(eventId)
        .withSourcedFrom(null)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()

      testDataGenerator.createReferralWithStatusHistory(referralEntity)
      val savedReferral = referralRepository.findByCrn(crn)[0]

      // Stub both endpoints to return 404
      nDeliusApiStubs.stubNotFoundRequirementManagerResponse("X999999", eventId)
      nDeliusApiStubs.stubNotFoundLicenceConditionManagerResponse("X999999", eventId)

      // When
      val exception = assertThrows<NotFoundException> {
        referralService.attemptToFindManagerForReferral(savedReferral.id!!)
      }

      assertThat(exception.message).isEqualTo("No LicenceCondition or Requirement found with id UNKNOWN001")
    }

    @Test
    fun `attemptToFindManagerForReferral should throw NotFoundException when referral does not exist`() {
      // Given
      val nonExistentReferralId = UUID.randomUUID()

      // When & Then
      val exception = assertThrows<NotFoundException> {
        referralService.attemptToFindManagerForReferral(nonExistentReferralId)
      }

      assertThat(exception.message).isEqualTo("No Referral found for id: $nonExistentReferralId")
    }

    @Test
    fun `attemptToFindManagerForReferral should throw NotFoundException when eventId is null or empty`() {
      // Given
      val crn = "X888888"
      val referralEntity = ReferralEntityFactory()
        .withCrn(crn)
        .withEventId("") // Empty eventId
        .withSourcedFrom(null)
        .withCohort(OffenceCohort.GENERAL_OFFENCE)
        .produce()

      testDataGenerator.createReferralWithStatusHistory(referralEntity)

      val savedReferral = referralRepository.findByCrn(crn)[0]
      val referralId = savedReferral.id!!.toString()

      // When & Then
      val exception = assertThrows<NotFoundException> {
        referralService.attemptToFindManagerForReferral(savedReferral.id!!)
      }

      assertThat(exception.message).isEqualTo("Referral with id: $referralId exists, but has no eventId")
    }
  }

  @Nested
  @DisplayName(value = "GetStatusHistoryIntegrationTests for Delius")
  inner class GetStatusHistoryIntegrationTest {
    @Test
    fun `getStatusHistoryIntegration should return status records when they exist`() {
      // Given
      val theCrnNumber = randomUppercaseString()
      val awaitingAllocation = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(theCrnNumber, 1)
      oasysApiStubs.stubSuccessfulPniResponse(theCrnNumber)

      val referral =
        referralService.createReferral(
          FindAndReferReferralDetailsFactory().withPersonReference(theCrnNumber).withEventNumber(1).produce(),
        )

      // When
      referralService.updateStatus(referral, awaitingAllocation.id, "Additional Details", "The User's name")
      val getResult = referralService.getStatusHistory(referral.id!!)

      // Then
      assertThat(getResult).hasSize(2)

      assertThat(getResult[0].referralStatusDescriptionName).isEqualTo("Awaiting assessment")
      assertThat(getResult[0].updatedBy).isEqualTo("SYSTEM")
      assertThat(getResult[0].additionalDetails).isEqualTo(null)
      assertThat(getResult[0].tagColour).isEqualTo("purple")

      assertThat(getResult[1].referralStatusDescriptionName).isEqualTo("Awaiting allocation")
      assertThat(getResult[1].updatedBy).isEqualTo("The User's name")
      assertThat(getResult[1].additionalDetails).isEqualTo("Additional Details")
      assertThat(getResult[1].tagColour).isEqualTo("light-blue")
    }
  }

  @Nested
  @DisplayName("UpdateStatusIntegrationTests")
  inner class UpdateStatusIntegrationTests {
    @Test
    fun `updateStatus should create a new entry in the ReferralStatusHistory log`() {
      // Given
      val theCrnNumber = randomUppercaseString()
      val theGroup = testDataGenerator.createGroup(ProgrammeGroupFactory().withCode("GROUP-001").produce())
      oasysApiStubs.stubSuccessfulPniResponse(theCrnNumber)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(theCrnNumber, 1)

      val referral =
        referralService.createReferral(
          FindAndReferReferralDetailsFactory().withPersonReference(theCrnNumber).withEventNumber(1).produce(),
        )

      val referralFromAllocateToGroup =
        membershipService.allocateReferralToGroup(
          referral.id!!,
          theGroup.id!!,
          "THE_USER_WHO_ADDED_TO_GROUP",
          "",
        )

      // When
      val numberOfHistoriesBeforeUpdate = referralFromAllocateToGroup.statusHistories.size
      val result = referralService.updateStatus(
        referral,
        UUID.fromString("76b2f8d8-260c-4766-a716-de9325292609"),
        "Additional details string",
        createdBy = "THE_USER_ID",
      )

      // Then
      val foundReferral = referralRepository.findByCrn(theCrnNumber).firstOrNull()
        ?: throw NotFoundException("No Referral found for crn: $theCrnNumber")

      assertThat(result.referralStatusDescriptionId).isEqualTo(UUID.fromString("76b2f8d8-260c-4766-a716-de9325292609"))
      assertThat(result.referralStatusDescriptionName).isEqualTo("Awaiting assessment")

      assertThat(foundReferral.statusHistories).hasSize(numberOfHistoriesBeforeUpdate + 1)
      assertThat(foundReferral.programmeGroupMemberships).isNotNull()
      assertThat(foundReferral.statusHistories.first().createdBy).isEqualTo("THE_USER_ID")
      assertThat(foundReferral.statusHistories.first().id).isEqualTo(result.id)
      assertThat(foundReferral.statusHistories.first().additionalDetails).isEqualTo("Additional details string")
    }

    @Test
    fun `updateStatus should throw a NotFoundError if the ReferralStatusDescription does not exist`() {
      // Given
      val aRandomUuid = UUID.randomUUID()
      val referral = ReferralEntityFactory().produce()
      testDataGenerator.createReferralWithStatusHistory(referral)

      // When/Then
      assertThrows<NotFoundException> {
        referralService.updateStatus(
          referral,
          aRandomUuid,
          createdBy = "DOES NOT MATTER",
        )
      } shouldHaveMessage "Unable to find Referral Status Description with ID $aRandomUuid"
    }

    @Test
    fun `updateStatus should remove Group Membership if the transition is not 'continuing'`() {
      // Given
      val theGroup = testDataGenerator.createGroup(ProgrammeGroupFactory().produce())

      val theCrnNumber = randomUppercaseString()
      oasysApiStubs.stubSuccessfulPniResponse(theCrnNumber)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(theCrnNumber, 1)

      val recallStatusDescription = referralStatusDescriptionRepository.getRecallStatusDescription()

      val theReferral =
        referralService.createReferral(
          FindAndReferReferralDetailsFactory().withPersonReference(theCrnNumber).withEventNumber(1).produce(),
        )

      membershipService.allocateReferralToGroup(theReferral.id!!, theGroup.id!!, "SYSTEM", "")

      val theReferralWithGroup = referralRepository.findByCrn(theCrnNumber).first()

      // When
      assertThat(theReferralWithGroup.programmeGroupMemberships.find { it.programmeGroup.id == theGroup.id }).isNotNull()
      referralService.updateStatus(theReferralWithGroup, recallStatusDescription.id, createdBy = "SYSTEM")

      // Then
      val theUpdatedReferral = referralRepository.findByCrn(theCrnNumber).first()
      assertThat(referralService.getCurrentlyAllocatedGroup(theUpdatedReferral)).isNull()
      assertThat(referralService.getCurrentStatusHistory(theUpdatedReferral)!!.referralStatusDescription.description).isEqualTo(
        "Recall",
      )
      assertThat(theUpdatedReferral.statusHistories.maxByOrNull { it.createdAt }?.referralStatusDescription?.description).isEqualTo(
        "Recall",
      )
    }
  }

  @Nested
  @DisplayName("CreateReferral")
  inner class CreateReferral {

    val referralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionType(InterventionType.TOOLKITS)
      .withInterventionName("The Intervention Name")
      .withPersonReference("CRN-12345")
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withReferralId(UUID.randomUUID())
      .withSetting(SettingType.COMMUNITY)
      .withSourcedFromReference("LICENCE-12345")
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventNumber(1)
      .produce()

    @Test
    fun `createReferral should create a Referral, its Status History, and Reporting Location`() {
      //    Given

      oasysApiStubs.stubSuccessfulPniResponse("CRN-12345")
      nDeliusApiStubs.stubPersonalDetailsResponse(
        NDeliusPersonalDetailsFactory()
          .withDateOfBirth(LocalDate.parse("2010-10-01"))
          .withSex(CodeDescription("M", "Male"))
          .withProbationDeliveryUnit(CodeDescription("PDU001", "Primary PDU"))
          .withRegion(CodeDescription("REGION-FOR-TEST", "THE REGION DESCRIPTION"))
          .withTeam(CodeDescription("TEAM-CODE", "The test reporting team"))
          .produce(),
      )
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        "CRN-12345",
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      //    When
      val referral = referralService.createReferral(referralDetails)

      //    Then
      val referralFromRepo =
        referralRepository.findByCrn("CRN-12345").firstOrNull() ?: throw NotFoundException("Referral with CRN-12345")

      val reportingLocation = reportingLocationRepository.findByReferralId(referral.id)

      assertThat(referralFromRepo.id).isEqualTo(referral.id)
      assertThat(referralFromRepo.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
      assertThat(referralFromRepo.interventionType).isEqualTo(InterventionType.TOOLKITS)
      assertThat(referralFromRepo.interventionName).isEqualTo("The Intervention Name")
      assertThat(referralFromRepo.setting).isEqualTo(SettingType.COMMUNITY)
      assertThat(referralFromRepo.statusHistories).hasSize(1)
      assertThat(referralFromRepo.statusHistories.firstOrNull()?.referralStatusDescription?.description).isEqualTo("Awaiting assessment")
      assertThat(referralFromRepo.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(referralFromRepo.sex).isEqualTo("Male")
      assertThat(referralFromRepo.sentenceEndDate).isEqualTo(LocalDate.parse("2027-11-02"))

      assertThat(reportingLocation).isNotNull()
      assertThat(reportingLocation!!.regionName).isEqualTo("THE REGION DESCRIPTION")
      assertThat(reportingLocation.pduName).isEqualTo("Primary PDU")
      assertThat(reportingLocation.reportingTeam).isEqualTo("The test reporting team")
    }

    @Test
    fun `createReferral should save referral and add status history and determine ldc status true when score greater than or equal to 3`() {
      // Given
      val cohort = OffenceCohort.SEXUAL_OFFENCE
      val ldcResponse = Ldc(
        score = 4,
        subTotal = 4,
      )
      val pniAssessment = PniAssessmentFactory().withLdc(ldcResponse).produce()
      val pniResponse = PniResponseFactory().withAssessment(pniAssessment).produce()
      oasysApiStubs.stubSuccessfulPniResponse(referralDetails.personReference, pniResponse)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      // When
      referralService.createReferral(referralDetails)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(cohort)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isTrue
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(savedReferral.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(savedReferral.sex).isEqualTo("Male")
      assertThat(savedReferral.sentenceEndDate).isEqualTo(LocalDate.parse("2027-11-02"))
    }

    @Test
    fun `createReferral should save referral and add status history and determine ldc status false when score less than 3`() {
      // Given
      val cohort = OffenceCohort.SEXUAL_OFFENCE
      val ldcResponse = Ldc(
        score = 2,
        subTotal = 2,
      )
      val pniAssessment = PniAssessmentFactory().withLdc(ldcResponse).produce()
      val pniResponse = PniResponseFactory().withAssessment(pniAssessment).produce()
      oasysApiStubs.stubSuccessfulPniResponse(referralDetails.personReference, pniResponse)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      // When
      referralService.createReferral(referralDetails)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(cohort)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isFalse
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(savedReferral.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(savedReferral.sex).isEqualTo("Male")
      assertThat(savedReferral.sentenceEndDate).isEqualTo(LocalDate.parse("2027-11-02"))
    }

    @Test
    fun `createReferral should save referral and add status history and determine ldc status = false when ldc is null from pni`() {
      // Given
      val cohort = OffenceCohort.SEXUAL_OFFENCE

      val pniAssessment = PniAssessmentFactory().withLdc(null).produce()
      val pniResponse = PniResponseFactory().withAssessment(pniAssessment).produce()
      oasysApiStubs.stubSuccessfulPniResponse(referralDetails.personReference, pniResponse)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      // When
      referralService.createReferral(referralDetails)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(cohort)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isFalse
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(savedReferral.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(savedReferral.sex).isEqualTo("Male")
      assertThat(savedReferral.sentenceEndDate).isEqualTo(LocalDate.parse("2027-11-02"))
    }

    @Test
    fun `createReferral should save referral and add status history with ldc status as false and cohort as general offence a when no data found from PNI`() {
      // Given

      oasysApiStubs.stubNotFoundPniResponse(referralDetails.personReference)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      // When
      referralService.createReferral(referralDetails)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isFalse
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
    }

    @Test
    fun `createReferral should save referral and add name reporting locations`() {
      // Given

      oasysApiStubs.stubNotFoundPniResponse(referralDetails.personReference)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )

      // When
      referralService.createReferral(referralDetails)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isFalse
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(savedReferral.personName).isEqualTo(personalDetails.name.getNameAsString())
      assertThat(savedReferral.referralReportingLocationEntity?.pduName).isEqualTo(personalDetails.probationDeliveryUnit.description)
      assertThat(savedReferral.referralReportingLocationEntity?.reportingTeam).isEqualTo(personalDetails.team.description)
      assertThat(savedReferral.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(savedReferral.sex).isEqualTo("Male")
      assertThat(savedReferral.sentenceEndDate).isEqualTo(LocalDate.parse("2027-11-02"))
    }

    @Test
    fun `createReferral should save referral and use expected end date for Community Order type`() {
      // Given

      oasysApiStubs.stubNotFoundPniResponse(referralDetails.personReference)
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referralDetails.personReference,
        1,
        NDeliusSentenceResponseFactory().withExpectedEndDate(LocalDate.parse("2026-04-26")).produce(),
      )

      // When
      val referralDetailsOrder = referralDetails.copy(sourcedFromReferenceType = ReferralEntitySourcedFrom.REQUIREMENT)
      referralService.createReferral(referralDetailsOrder)

      val savedReferral = referralRepository.findByCrn(referralDetails.personReference).first()

      // Then
      assertThat(savedReferral.crn).isEqualTo(referralDetails.personReference)
      assertThat(savedReferral.interventionType).isEqualTo(referralDetails.interventionType)
      assertThat(savedReferral.interventionName).isEqualTo(referralDetails.interventionName)
      assertThat(savedReferral.cohort).isEqualTo(OffenceCohort.GENERAL_OFFENCE)
      assertThat(savedReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(savedReferral.referralLdcHistories.first().hasLdc).isFalse
      assertThat(savedReferral.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(savedReferral.personName).isEqualTo(personalDetails.name.getNameAsString())
      assertThat(savedReferral.referralReportingLocationEntity?.pduName).isEqualTo(personalDetails.probationDeliveryUnit.description)
      assertThat(savedReferral.referralReportingLocationEntity?.reportingTeam).isEqualTo(personalDetails.team.description)
      assertThat(savedReferral.dateOfBirth).isEqualTo(LocalDate.parse("2010-10-01"))
      assertThat(savedReferral.sex).isEqualTo("Male")
      assertThat(savedReferral.sentenceEndDate).isEqualTo(LocalDate.parse("2026-04-26"))
    }
  }

  @Nested
  @DisplayName("GetReferralDetails")
  @WithMockAuthUser("TEST_USER")
  inner class GetReferralDetails {
    @Test
    fun `retrieve referralDetails when referral exists and update referral values from nDelius and Oasys responses`() = runTest {
      val referral = ReferralEntityFactory().produce()
      val name = randomFullName()
      val dateOfBirth = randomDateOfBirth()
      testDataGenerator.createReferralWithStatusHistory(referral)
      oasysApiStubs.stubSuccessfulPniResponse(referral.crn)
      nDeliusApiStubs.stubPersonalDetailsResponse(
        NDeliusPersonalDetailsFactory()
          .withName(name)
          .withDateOfBirth(dateOfBirth)
          .produce(),
      )
      nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(
        referral.crn,
        referral.eventNumber,
        NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2027-11-02")).produce(),
      )
      nDeliusApiStubs.stubAccessCheck(granted = true, referral.crn)

      val referralDetails = referralService.refreshPersonalDetailsForReferral(referral.id!!)!!

      assertThat(referralDetails.id).isEqualTo(referral.id!!)
      assertThat(referralDetails.crn).isEqualTo(referral.crn)
      assertThat(referralDetails.personName).isEqualTo(name.getNameAsString())
      assertThat(referralDetails.interventionName).isEqualTo(referral.interventionName)
      assertThat(referralDetails.createdAt).isEqualTo(referral.createdAt.toLocalDate())
      assertThat(referralDetails.dateOfBirth).isEqualTo(dateOfBirth)
    }
  }
}
