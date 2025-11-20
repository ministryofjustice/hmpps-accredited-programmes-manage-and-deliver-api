package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.DeliveryLocationPreferenceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationProbationDeliveryUnitEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

@WithMockAuthUser("PROB_PRACTITIONER_1")
class DeliveryLocationPreferencesServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var deliveryLocationPreferencesService: DeliveryLocationPreferencesService

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()

    stubAuthTokenEndpoint()
  }

  @Test
  @Transactional
  fun `getDeliveryLocationPreferencesFormDataForReferral should return form data with all required information`() {
    // Given
    val crn = "X123456"
    val eventId = "REQ001"
    val referralEntity = ReferralEntityFactory()
      .withCrn(crn)
      .withEventId(eventId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()

    testDataGenerator.createReferralWithStatusHistory(referralEntity)

    // Create existing delivery location preferences
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory()
      .withDeliusCode("PDU001")
      .withDeliusDescription("Test PDU")
      .produce()

    val preferredDeliveryLocations = PreferredDeliveryLocationEntityFactory()
      .withDeliusCode("OFFICE-001")
      .withDeliusDescription("Test Office")
      .withPreferredDeliveryLocationProbationDeliveryUnit(pdu)
      .produce()

    val deliveryLocationPreference = DeliveryLocationPreferenceEntityFactory()
      .withReferral(referralEntity)
      .withPreferredDeliveryLocations(mutableSetOf(preferredDeliveryLocations))
      .withLocationsCannotAttendText("The cannot attend locations free text value")
      .produce()

    testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(pdu)
    testDataGenerator.createPreferredDeliveryLocation(preferredDeliveryLocations)
    testDataGenerator.createDeliveryLocationPreference(deliveryLocationPreference)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetailsFactory()
      .withCrn(crn)
      .withName(FullName(forename = "John", surname = "Doe"))
      .withDateOfBirth(LocalDate.parse("1980-01-01"))
      .withSex(CodeDescription("M", "Male"))
      .withEthnicity(CodeDescription("WHITE", "White"))
      .withProbationPractitioner(null)
      .withProbationDeliveryUnit(CodeDescription("PDU001", "Primary PDU"))
      .produce()

    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU001",
      description = "Primary PDU",
    )

    val primaryOffices = listOf(
      CodeDescription(
        code = "OFFICE-001",
        description = "Brighton and Hove: Probation Office",
      ),
      CodeDescription(
        code = "OFFICE-002",
        description = "Eastbourne: Probation Office",
      ),
    )

    val managerDetails = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(
        code = "STAFF001",
        name = FullName(forename = "Jane", surname = "Smith"),
      ),
      team = CodeDescription("TEAM001", "Primary Team"),
      probationDeliveryUnit = primaryPdu,
      officeLocations = primaryOffices,
    )

    val additionalPdus = listOf(
      NDeliusApiProbationDeliveryUnitWithOfficeLocations(
        code = "PDU002",
        description = "West Sussex",
        officeLocations = listOf(
          CodeDescription(
            code = "OFFICE-003",
            description = "Guildford: Office Name",
          ),
        ),
      ),
    )

    val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponse(
      manager = managerDetails,
      probationDeliveryUnits = additionalPdus,
    )

    nDeliusApiStubs.stubAccessCheck(true, crn)
    nDeliusApiStubs.stubPersonalDetailsResponseForCrn(crn, personalDetails)
    nDeliusApiStubs.stubSuccessfulRequirementManagerResponse(crn, eventId, requirementResponse)

    // When
    val result =
      deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

    // Then
    assertThat(result.personOnProbation.name).isEqualTo("John Doe")
    assertThat(result.personOnProbation.crn).isEqualTo(crn)
    assertThat(result.personOnProbation.dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
    assertThat(result.personOnProbation.tier).isEqualTo("Primary PDU")

    assertThat(result.existingDeliveryLocationPreferences).isNotNull
    val existingPrefs = result.existingDeliveryLocationPreferences!!
    assertThat(existingPrefs.canAttendLocationsValues).hasSize(1)
    assertThat(existingPrefs.canAttendLocationsValues[0].value).isEqualTo("OFFICE-001")
    assertThat(existingPrefs.canAttendLocationsValues[0].label).isEqualTo("Test Office")
    assertThat(existingPrefs.cannotAttendLocations).isEqualTo("The cannot attend locations free text value")

    assertThat(result.primaryPdu.name).isEqualTo("Primary PDU")
    assertThat(result.primaryPdu.code).isEqualTo("PDU001")
    assertThat(result.primaryPdu.deliveryLocations).hasSize(2)
    assertThat(result.primaryPdu.deliveryLocations[0].value).isEqualTo("OFFICE-001")
    assertThat(result.primaryPdu.deliveryLocations[0].label).isEqualTo("Brighton and Hove: Probation Office")

    assertThat(result.otherPdusInSameRegion).hasSize(1)
    assertThat(result.otherPdusInSameRegion[0].name).isEqualTo("West Sussex")
    assertThat(result.otherPdusInSameRegion[0].code).isEqualTo("PDU002")
    assertThat(result.otherPdusInSameRegion[0].deliveryLocations).hasSize(1)
    assertThat(result.otherPdusInSameRegion[0].deliveryLocations[0].value).isEqualTo("OFFICE-003")
  }

  @Test
  fun `getDeliveryLocationPreferencesFormDataForReferral should return form data without existing preferences`() {
    // Given
    val crn = "X123456"
    val eventId = "REQ001"
    val referralEntity = ReferralEntityFactory()
      .withCrn(crn)
      .withEventId(eventId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()

    testDataGenerator.createReferralWithStatusHistory(referralEntity)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetailsFactory()
      .withCrn(crn)
      .withName(FullName(forename = "Alex", surname = "River"))
      .withDateOfBirth(LocalDate.parse("2000-01-01"))
      .withAge("25")
      .withSex(CodeDescription("F", "Female"))
      .withEthnicity(null)
      .withProbationPractitioner(null)
      .withProbationDeliveryUnit(CodeDescription("C2", "C2"))
      .produce()

    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU001",
      description = "East Sussex",
    )

    val primaryOffices = listOf(
      CodeDescription(
        code = "OFFICE-CODE-123",
        description = "Brighton and Hove: Probation Office",
      ),
    )

    val managerDetails = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(
        code = "STAFF001",
        name = FullName(forename = "Jane", surname = "Smith"),
      ),
      team = CodeDescription("TEAM001", "Primary Team"),
      probationDeliveryUnit = primaryPdu,
      officeLocations = primaryOffices,
    )

    val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponse(
      manager = managerDetails,
      probationDeliveryUnits = emptyList(),
    )

    nDeliusApiStubs.stubAccessCheck(true, crn)
    nDeliusApiStubs.stubPersonalDetailsResponseForCrn(crn, personalDetails)
    nDeliusApiStubs.stubSuccessfulRequirementManagerResponse(crn, eventId, requirementResponse)

    // When
    val result =
      deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

    // Then
    assertThat(result.personOnProbation.name).isEqualTo("Alex River")
    assertThat(result.personOnProbation.crn).isEqualTo(crn)
    assertThat(result.personOnProbation.dateOfBirth).isEqualTo(LocalDate.of(2000, 1, 1))
    assertThat(result.personOnProbation.tier).isEqualTo("C2")

    assertThat(result.existingDeliveryLocationPreferences).isNull()

    assertThat(result.primaryPdu.name).isEqualTo("East Sussex")
    assertThat(result.primaryPdu.deliveryLocations).hasSize(1)
    assertThat(result.primaryPdu.deliveryLocations[0].value).isEqualTo("OFFICE-CODE-123")
    assertThat(result.primaryPdu.deliveryLocations[0].label).isEqualTo("Brighton and Hove: Probation Office")

    assertThat(result.otherPdusInSameRegion).hasSize(0)
  }

  @Test
  fun `getDeliveryLocationPreferencesFormDataForReferral should handle licence condition referrals`() {
    // Given
    val crn = "X123456"
    val eventId = "LIC001"
    val referralEntity = ReferralEntityFactory()
      .withCrn(crn)
      .withEventId(eventId)
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withCohort(OffenceCohort.SEXUAL_OFFENCE)
      .produce()

    testDataGenerator.createReferralWithStatusHistory(referralEntity)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetailsFactory()
      .withCrn(crn)
      .withName(FullName(forename = "Test", surname = "Person"))
      .withDateOfBirth(LocalDate.parse("1990-06-15"))
      .withAge("35")
      .withSex(CodeDescription("M", "Male"))
      .withEthnicity(CodeDescription("ASIAN", "Asian"))
      .withProbationPractitioner(null)
      .withProbationDeliveryUnit(CodeDescription("PDU002", "Test PDU"))
      .produce()

    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU002",
      description = "Test PDU",
    )

    val managerDetails = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(
        code = "STAFF002",
        name = FullName(forename = "Bob", surname = "Manager"),
      ),
      team = CodeDescription("TEAM002", "Licence Team"),
      probationDeliveryUnit = primaryPdu,
      officeLocations = emptyList(),
    )

    val licenceConditionResponse = NDeliusCaseRequirementOrLicenceConditionResponse(
      manager = managerDetails,
      probationDeliveryUnits = emptyList(),
    )

    nDeliusApiStubs.stubAccessCheck(true, crn)
    nDeliusApiStubs.stubPersonalDetailsResponseForCrn(crn, personalDetails)
    nDeliusApiStubs.stubSuccessfulLicenceConditionManagerResponse(crn, eventId, licenceConditionResponse)

    // When
    val result =
      deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

    // Then
    assertThat(result.personOnProbation.name).isEqualTo("Test Person")
    assertThat(result.primaryPdu.name).isEqualTo("Test PDU")
    assertThat(result.primaryPdu.deliveryLocations).hasSize(0)
  }

  @Test
  fun `getDeliveryLocationPreferencesFormDataForReferral should throw NotFoundException when referral not found`() {
    // Given
    val nonExistentReferralId = UUID.randomUUID()

    // When/Then
    assertThrows<NotFoundException> {
      deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(nonExistentReferralId)
    }
  }

  @Test
  fun `getDeliveryLocationPreferencesFormDataForReferral should throw NotFoundException when manager details not found`() {
    // Given
    val crn = "X123456"
    val eventId = "REQ001"
    val referralEntity = ReferralEntityFactory()
      .withCrn(crn)
      .withEventId(eventId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withCohort(OffenceCohort.GENERAL_OFFENCE)
      .produce()

    testDataGenerator.createReferralWithStatusHistory(referralEntity)

    val personalDetails = NDeliusPersonalDetailsFactory()
      .withCrn(crn)
      .withName(FullName(forename = "John", surname = "Doe"))
      .withDateOfBirth(LocalDate.parse("1980-01-01"))
      .withAge("44")
      .withSex(CodeDescription("M", "Male"))
      .withEthnicity(null)
      .withProbationPractitioner(null)
      .produce()

    nDeliusApiStubs.stubAccessCheck(true, crn)
    nDeliusApiStubs.stubPersonalDetailsResponseForCrn(crn, personalDetails)
    nDeliusApiStubs.stubNotFoundRequirementManagerResponse(crn, eventId)
    nDeliusApiStubs.stubNotFoundLicenceConditionManagerResponse(crn, eventId)

    // When/Then
    assertThrows<NotFoundException> {
      deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)
    }
  }
}
