package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class EntityFactoriesTest {

  @Test
  fun `MessageHistoryEntityFactory should create entity with default values`() {
    val messageHistory = MessageHistoryEntityFactory().produce()

    assertThat(messageHistory.id).isNull()
    assertThat(messageHistory.eventType).isNotNull()
    assertThat(messageHistory.detailUrl).isNotNull()
    assertThat(messageHistory.description).isNotNull()
    assertThat(messageHistory.occurredAt).isNotNull()
    assertThat(messageHistory.message).isNotNull()
    assertThat(messageHistory.createdAt).isNotNull()
    assertThat(messageHistory.referral).isNull()
  }

  @Test
  fun `MessageHistoryEntityFactory should create entity with custom values`() {
    val id = UUID.randomUUID()
    val eventType = "custom-event-type"
    val detailUrl = "https://custom.url"
    val description = "Custom description"
    val occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0)
    val message = "Custom message"
    val createdAt = LocalDateTime.of(2023, 1, 1, 12, 30)
    val referral = ReferralEntityFactory().produce()

    val messageHistory = MessageHistoryEntityFactory()
      .withId(id)
      .withEventType(eventType)
      .withDetailUrl(detailUrl)
      .withDescription(description)
      .withOccurredAt(occurredAt)
      .withMessage(message)
      .withCreatedAt(createdAt)
      .withReferral(referral)
      .produce()

    assertThat(messageHistory.id).isEqualTo(id)
    assertThat(messageHistory.eventType).isEqualTo(eventType)
    assertThat(messageHistory.detailUrl).isEqualTo(detailUrl)
    assertThat(messageHistory.description).isEqualTo(description)
    assertThat(messageHistory.occurredAt).isEqualTo(occurredAt)
    assertThat(messageHistory.message).isEqualTo(message)
    assertThat(messageHistory.createdAt).isEqualTo(createdAt)
    assertThat(messageHistory.referral).isEqualTo(referral)
  }

  @Test
  fun `ReferralEntityFactory should create entity with default values`() {
    val referral = ReferralEntityFactory().produce()

    assertThat(referral.id).isNull()
    assertThat(referral.personName).isNotNull()
    assertThat(referral.crn).isNotNull()
    assertThat(referral.createdAt).isNotNull()
    assertThat(referral.statusHistories).isNotEmpty
  }

  @Test
  fun `ReferralEntityFactory should create entity with custom values`() {
    val id = UUID.randomUUID()
    val personName = "Custom Person"
    val crn = "CUSTOM123"
    val createdAt = LocalDateTime.of(2023, 1, 1, 12, 0)
    val statusHistory = ReferralStatusHistoryEntity(
      status = "Custom Status",
      createdBy = "Test User",
      startDate = LocalDateTime.now(),
      endDate = null,
    )

    val referral = ReferralEntityFactory()
      .withId(id)
      .withPersonName(personName)
      .withCrn(crn)
      .withCreatedAt(createdAt)
      .withStatusHistories(mutableListOf(statusHistory))
      .produce()

    assertThat(referral.id).isEqualTo(id)
    assertThat(referral.personName).isEqualTo(personName)
    assertThat(referral.crn).isEqualTo(crn)
    assertThat(referral.createdAt).isEqualTo(createdAt)
    assertThat(referral.statusHistories).hasSize(1)
    assertThat(referral.statusHistories[0]).isEqualTo(statusHistory)
  }

  @Test
  fun `ReferralStatusHistoryEntityFactory should create entity with default values`() {
    val statusHistory = ReferralStatusHistoryEntityFactory().produce()

    assertThat(statusHistory.id).isNull()
    assertThat(statusHistory.status).isNotNull()
    assertThat(statusHistory.createdAt).isNotNull()
    assertThat(statusHistory.createdBy).isNotNull()
    assertThat(statusHistory.startDate).isNotNull()
    assertThat(statusHistory.endDate).isNull()
  }

  @Test
  fun `ReferralStatusHistoryEntityFactory should create entity with custom values`() {
    val id = UUID.randomUUID()
    val status = "Custom Status"
    val createdAt = LocalDateTime.of(2023, 1, 1, 12, 0)
    val createdBy = "Custom User"
    val startDate = LocalDateTime.of(2023, 1, 1, 12, 0)
    val endDate = LocalDateTime.of(2023, 1, 2, 12, 0)

    val statusHistory = ReferralStatusHistoryEntityFactory()
      .withId(id)
      .withStatus(status)
      .withCreatedAt(createdAt)
      .withCreatedBy(createdBy)
      .withStartDate(startDate)
      .withEndDate(endDate)
      .produce()

    assertThat(statusHistory.id).isEqualTo(id)
    assertThat(statusHistory.status).isEqualTo(status)
    assertThat(statusHistory.createdAt).isEqualTo(createdAt)
    assertThat(statusHistory.createdBy).isEqualTo(createdBy)
    assertThat(statusHistory.startDate).isEqualTo(startDate)
    assertThat(statusHistory.endDate).isEqualTo(endDate)
  }

  @Test
  fun `ReferralDetailsFactory should create entity with default values`() {
    val referralDetails = FindAndReferReferralDetailsFactory().produce()

    assertThat(referralDetails.interventionType).isNotNull()
    assertThat(referralDetails.interventionName).isNotNull()
    assertThat(referralDetails.personReference).isNotNull()
    assertThat(referralDetails.personReferenceType).isNotNull()
    assertThat(referralDetails.referralId).isNotNull()
    assertThat(referralDetails.setting).isNotNull()
  }

  @Test
  fun `ReferralDetailsFactory should create entity with custom values`() {
    val interventionType = InterventionType.CRS
    val interventionName = "Custom Intervention Name"
    val personReference = "CUSTOM123"
    val personReferenceType = PersonReferenceType.NOMS
    val referralId = UUID.randomUUID()
    val setting = SettingType.CUSTODY

    val referralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionType(interventionType)
      .withInterventionName(interventionName)
      .withPersonReference(personReference)
      .withPersonReferenceType(personReferenceType)
      .withReferralId(referralId)
      .withSetting(setting)
      .produce()

    assertThat(referralDetails.interventionType).isEqualTo(interventionType)
    assertThat(referralDetails.interventionName).isEqualTo(interventionName)
    assertThat(referralDetails.personReference).isEqualTo(personReference)
    assertThat(referralDetails.personReferenceType).isEqualTo(personReferenceType)
    assertThat(referralDetails.referralId).isEqualTo(referralId)
    assertThat(referralDetails.setting).isEqualTo(setting)
  }

  @Test
  fun `OffencesFactory should create entity with default values`() {
    val offences = OffencesFactory().produce()

    assertThat(offences.mainOffence).isNotNull
    assertThat(offences.mainOffence.date).isNotNull
    assertThat(offences.mainOffence.mainCategoryCode).isNotNull
    assertThat(offences.mainOffence.mainCategoryDescription).isNotNull
    assertThat(offences.mainOffence.subCategoryCode).isNotNull
    assertThat(offences.mainOffence.subCategoryDescription).isNotNull
    assertThat(offences.additionalOffences).isNotEmpty
  }

  @Test
  fun `OffencesFactory should create entity with no additional offences`() {
    val offences = OffencesFactory()
      .withNoAdditionalOffences()
      .produce()

    assertThat(offences.additionalOffences).isEmpty()
  }

  @Test
  fun `NDeliusSentenceResponseFactory should create entity with default values`() {
    val sentenceResponse = NDeliusSentenceResponseFactory().produce()

    assertThat(sentenceResponse.description).isNotNull()
    assertThat(sentenceResponse.startDate).isNotNull()
    assertThat(sentenceResponse.licenceExpiryDate).isNotNull()
    assertThat(sentenceResponse.postSentenceSupervisionEndDate).isNotNull()
    assertThat(sentenceResponse.twoThirdsSupervisionDate).isNotNull()
    assertThat(sentenceResponse.custodial).isNotNull()
    assertThat(sentenceResponse.releaseType).isNotNull()
    assertThat(sentenceResponse.licenceConditions).isNotEmpty
    assertThat(sentenceResponse.requirements).isNotEmpty
    assertThat(sentenceResponse.postSentenceSupervisionRequirements).isNotEmpty
  }

  @Test
  fun `NDeliusSentenceResponseFactory should create entity with custom values`() {
    val description = "Custom sentence description"
    val startDate = LocalDate.of(2023, 6, 1)
    val licenceExpiryDate = LocalDate.of(2025, 6, 1)
    val postSentenceSupervisionEndDate = LocalDate.of(2026, 6, 1)
    val twoThirdsSupervisionDate = LocalDate.of(2024, 12, 1)
    val custodial = false
    val releaseType = "Early Release"
    val licenceConditions = listOf(CodeDescription("LC", "Custom licence condition"))
    val requirements = listOf(
      CodeDescription("REQ1", "Custom requirement 1"),
      CodeDescription("REQ2", "Custom requirement 2"),
    )
    val postSentenceSupervisionRequirements = emptyList<CodeDescription>()

    val sentenceResponse = NDeliusSentenceResponseFactory()
      .withDescription(description)
      .withStartDate(startDate)
      .withLicenceExpiryDate(licenceExpiryDate)
      .withPostSentenceSupervisionEndDate(postSentenceSupervisionEndDate)
      .withTwoThirdsSupervisionDate(twoThirdsSupervisionDate)
      .withCustodial(custodial)
      .withReleaseType(releaseType)
      .withLicenceConditions(licenceConditions)
      .withRequirements(requirements)
      .withPostSentenceSupervisionRequirements(postSentenceSupervisionRequirements)
      .produce()

    assertThat(sentenceResponse.description).isEqualTo(description)
    assertThat(sentenceResponse.startDate).isEqualTo(startDate)
    assertThat(sentenceResponse.licenceExpiryDate).isEqualTo(licenceExpiryDate)
    assertThat(sentenceResponse.postSentenceSupervisionEndDate).isEqualTo(postSentenceSupervisionEndDate)
    assertThat(sentenceResponse.twoThirdsSupervisionDate).isEqualTo(twoThirdsSupervisionDate)
    assertThat(sentenceResponse.custodial).isEqualTo(custodial)
    assertThat(sentenceResponse.releaseType).isEqualTo(releaseType)
    assertThat(sentenceResponse.licenceConditions).isEqualTo(licenceConditions)
    assertThat(sentenceResponse.requirements).isEqualTo(requirements)
    assertThat(sentenceResponse.postSentenceSupervisionRequirements).isEqualTo(postSentenceSupervisionRequirements)
  }

  @Test
  fun `SentenceInformationFactory should create entity with default values`() {
    val sentenceInformation = SentenceInformationFactory().produce()

    assertThat(sentenceInformation.sentenceType).isNotNull()
    assertThat(sentenceInformation.releaseType).isNotNull()
    assertThat(sentenceInformation.licenceConditions).isNotNull()
    assertThat(sentenceInformation.licenceEndDate).isNotNull()
    assertThat(sentenceInformation.postSentenceSupervisionStartDate).isNotNull()
    assertThat(sentenceInformation.postSentenceSupervisionEndDate).isNotNull()
    assertThat(sentenceInformation.twoThirdsPoint).isNotNull()
    assertThat(sentenceInformation.orderRequirements).isNotNull()
    assertThat(sentenceInformation.orderEndDate).isNotNull()
    assertThat(sentenceInformation.dateRetrieved).isNotNull()
  }

  @Test
  fun `SentenceInformationFactory should create entity with custom values`() {
    val sentenceType = "ORA community order"
    val releaseType = "Standard Release"
    val licenceConditions = listOf(CodeDescription("OR1", "Custom order requirement 1"))
    val licenceEndDate = LocalDate.of(2025, 6, 1)
    val postSentenceSupervisionStartDate = LocalDate.of(2024, 6, 1)
    val postSentenceSupervisionEndDate = LocalDate.of(2026, 6, 1)
    val twoThirdsPoint = LocalDate.of(2024, 12, 1)
    val orderRequirements = listOf(
      CodeDescription("OR1", "Custom order requirement 1"),
      CodeDescription("OR2", "Custom order requirement 2"),
    )
    val orderEndDate = LocalDate.of(2024, 12, 31)
    val dateRetrieved = LocalDate.of(2024, 8, 1)

    val sentenceInformation = SentenceInformationFactory()
      .withSentenceType(sentenceType)
      .withReleaseType(releaseType)
      .withLicenceConditions(licenceConditions)
      .withLicenceEndDate(licenceEndDate)
      .withPostSentenceSupervisionStartDate(postSentenceSupervisionStartDate)
      .withPostSentenceSupervisionEndDate(postSentenceSupervisionEndDate)
      .withTwoThirdsPoint(twoThirdsPoint)
      .withOrderRequirements(orderRequirements)
      .withOrderEndDate(orderEndDate)
      .withDateRetrieved(dateRetrieved)
      .produce()

    assertThat(sentenceInformation.sentenceType).isEqualTo(sentenceType)
    assertThat(sentenceInformation.releaseType).isEqualTo(releaseType)
    assertThat(sentenceInformation.licenceConditions).isEqualTo(licenceConditions)
    assertThat(sentenceInformation.licenceEndDate).isEqualTo(licenceEndDate)
    assertThat(sentenceInformation.postSentenceSupervisionStartDate).isEqualTo(postSentenceSupervisionStartDate)
    assertThat(sentenceInformation.postSentenceSupervisionEndDate).isEqualTo(postSentenceSupervisionEndDate)
    assertThat(sentenceInformation.twoThirdsPoint).isEqualTo(twoThirdsPoint)
    assertThat(sentenceInformation.orderRequirements).isEqualTo(orderRequirements)
    assertThat(sentenceInformation.orderEndDate).isEqualTo(orderEndDate)
    assertThat(sentenceInformation.dateRetrieved).isEqualTo(dateRetrieved)
  }

  @Test
  fun `SentenceInformationFactory should create entity with null optional values`() {
    val sentenceInformation = SentenceInformationFactory()
      .withSentenceType(null)
      .withReleaseType(null)
      .withLicenceConditions(null)
      .withLicenceEndDate(null)
      .withPostSentenceSupervisionStartDate(null)
      .withPostSentenceSupervisionEndDate(null)
      .withTwoThirdsPoint(null)
      .withOrderRequirements(null)
      .withOrderEndDate(null)
      .produce()

    assertThat(sentenceInformation.sentenceType).isNull()
    assertThat(sentenceInformation.releaseType).isNull()
    assertThat(sentenceInformation.licenceConditions).isNull()
    assertThat(sentenceInformation.licenceEndDate).isNull()
    assertThat(sentenceInformation.postSentenceSupervisionStartDate).isNull()
    assertThat(sentenceInformation.postSentenceSupervisionEndDate).isNull()
    assertThat(sentenceInformation.twoThirdsPoint).isNull()
    assertThat(sentenceInformation.orderRequirements).isNull()
    assertThat(sentenceInformation.orderEndDate).isNull()
    assertThat(sentenceInformation.dateRetrieved).isNotNull() // This is required
  }
}
