package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiOfficeLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

@WithMockAuthUser("PROB_PRACTITIONER_1")
class DeliveryLocationPreferencesServiceIntegrationTest : IntegrationTestBase() {

  private lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  private lateinit var deliveryLocationPreferencesService: DeliveryLocationPreferencesService

  @Autowired
  private lateinit var entityManager: EntityManager

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs = NDeliusApiStubs(wiremock, objectMapper)
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

    testDataGenerator.createReferral(referralEntity)

    // Create existing delivery location preferences
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntity(
      deliusCode = "PDU001",
      deliusDescription = "Test PDU",
    )
    testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(pdu)

    val deliveryLocationPreference = DeliveryLocationPreferenceEntity(
      referral = referralEntity,
      locationsCannotAttendText = "The cannot attend locations free text value",
    )

    val preferredLocation = PreferredDeliveryLocationEntity(
      deliusCode = "OFFICE-001",
      deliusDescription = "Test Office",
      preferredDeliveryLocationProbationDeliveryUnit = pdu,
    )
    testDataGenerator.createPreferredDeliveryLocation(preferredLocation)

    deliveryLocationPreference.addPreferredDeliveryLocations(preferredLocation)
    testDataGenerator.createDeliveryLocationPreference(deliveryLocationPreference)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "John", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      age = "44",
      sex = CodeDescription("M", "Male"),
      ethnicity = CodeDescription("WHITE", "White"),
      probationPractitioner = null,
      probationDeliveryUnit = CodeDescription("PDU001", "Primary PDU"),
    )

    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU001",
      description = "Primary PDU",
    )

    val primaryOffices = listOf(
      NDeliusApiOfficeLocation(
        code = "OFFICE-001",
        description = "Brighton and Hove: Probation Office",
      ),
      NDeliusApiOfficeLocation(
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
          NDeliusApiOfficeLocation(
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
    val result = deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

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

    testDataGenerator.createReferral(referralEntity)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "Alex", surname = "River"),
      dateOfBirth = "2000-01-01",
      age = "25",
      sex = CodeDescription("F", "Female"),
      ethnicity = null,
      probationPractitioner = null,
      probationDeliveryUnit = CodeDescription("C2", "C2"),
    )

    val primaryPdu = NDeliusApiProbationDeliveryUnit(
      code = "PDU001",
      description = "East Sussex",
    )

    val primaryOffices = listOf(
      NDeliusApiOfficeLocation(
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
    val result = deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

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
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENSE_CONDITION)
      .withCohort(OffenceCohort.SEXUAL_OFFENCE)
      .produce()

    testDataGenerator.createReferral(referralEntity)

    // Mock NDelius responses
    val personalDetails = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "Test", surname = "Person"),
      dateOfBirth = "1990-06-15",
      age = "35",
      sex = CodeDescription("M", "Male"),
      ethnicity = CodeDescription("ASIAN", "Asian"),
      probationPractitioner = null,
      probationDeliveryUnit = CodeDescription("PDU002", "Test PDU"),
    )

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
    val result = deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralEntity.id!!)

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

    testDataGenerator.createReferral(referralEntity)

    val personalDetails = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "John", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      age = "44",
      sex = CodeDescription("M", "Male"),
      ethnicity = null,
      probationPractitioner = null,
      probationDeliveryUnit = null,
    )

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
