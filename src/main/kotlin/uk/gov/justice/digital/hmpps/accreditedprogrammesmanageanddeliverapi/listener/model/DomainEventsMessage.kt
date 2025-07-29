package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
  fun findNomsNumber() = get(NOMS_NUMBER_TYPE)

  operator fun get(key: String) = identifiers.find { it.type == key }?.value

  companion object {
    const val NOMS_NUMBER_TYPE = "NOMS"
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
