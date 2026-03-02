package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * This class represent the HmppsDomainEventSchema
 *
 * @see <a href="https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_MessageAttributeValue.html">AWS SQS MessageAttributeValue</a>
 *
 */
data class DomainEventsMessage(
  val eventType: String,
  val version: Int,
  val detailUrl: String? = null,
  val occurredAt: ZonedDateTime,
  val description: String? = null,
  val additionalInformation: Map<String, Any>?,
  val personReference: PersonReference,
)

data class PersonReference(val identifiers: List<Identifier> = listOf()) {
  fun findCrn() = get("CRN")

  operator fun get(key: String) = identifiers.find { it.type == key }?.value

  companion object {
    fun fromCrn(crn: String) = PersonReference(
      identifiers = listOf(Identifier(type = "CRN", value = crn)),
    )
  }

  data class Identifier(val type: String, val value: String)
}

fun DomainEventsMessage.toEntity(messageAsJson: String): MessageHistoryEntity = MessageHistoryEntity(
  id = null,
  eventType = eventType,
  detailUrl = detailUrl,
  description = description,
  occurredAt = occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
  message = messageAsJson,
  createdAt = LocalDateTime.now(),
)
