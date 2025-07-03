package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
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
    assertThat(referral.statusHistories).isEmpty()
  }

  @Test
  fun `ReferralEntityFactory should create entity with custom values`() {
    val id = UUID.randomUUID()
    val personName = "Custom Person"
    val crn = "CUSTOM123"
    val cohort = "CUSTOM-COHORT"
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
      .addStatusHistory(statusHistory)
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
}
