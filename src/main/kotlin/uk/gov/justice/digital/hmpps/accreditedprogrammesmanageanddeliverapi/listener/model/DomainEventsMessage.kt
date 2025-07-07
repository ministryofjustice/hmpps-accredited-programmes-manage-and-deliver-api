package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import java.time.LocalDateTime

data class DomainEventsMessage(
  val eventType: String,
  val detailUrl: String,
  val description: String? = null,
  val additionalInformation: Map<String, Any>? = mapOf(),
  val personReference: PersonReference = PersonReference(),
  val occurredAt: LocalDateTime,
)

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsNumber() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}
data class PersonIdentifier(val type: String, val value: String)

fun DomainEventsMessage.toEntity(messageAsJson: String): MessageHistoryEntity = MessageHistoryEntity(
  id = null,
  eventType = eventType,
  detailUrl = detailUrl,
  description = description,
  occurredAt = occurredAt,
  message = messageAsJson,
  createdAt = LocalDateTime.now(),
)
