package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

class MessageHistoryEntityFactory {
  private var id: UUID? = null
  private var eventType: String? = randomUppercaseString()
  private var detailUrl: String? = "https://api.example.com/message/${randomAlphanumericString()}"
  private var description: String? = randomSentence()
  private var occurredAt: LocalDateTime? = LocalDateTime.now()
  private var message: String? = randomSentence()
  private var createdAt: LocalDateTime? = LocalDateTime.now()
  private var referral: ReferralEntity? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withEventType(eventType: String?) = apply { this.eventType = eventType }
  fun withDetailUrl(detailUrl: String?) = apply { this.detailUrl = detailUrl }
  fun withDescription(description: String?) = apply { this.description = description }
  fun withOccurredAt(occurredAt: LocalDateTime?) = apply { this.occurredAt = occurredAt }
  fun withMessage(message: String?) = apply { this.message = message }
  fun withCreatedAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }
  fun withReferral(referral: ReferralEntity?) = apply { this.referral = referral }

  fun produce() = MessageHistoryEntity(
    id = this.id,
    eventType = this.eventType!!,
    detailUrl = this.detailUrl!!,
    description = this.description,
    occurredAt = this.occurredAt,
    message = this.message!!,
    createdAt = this.createdAt,
    referral = this.referral,
  )
}
