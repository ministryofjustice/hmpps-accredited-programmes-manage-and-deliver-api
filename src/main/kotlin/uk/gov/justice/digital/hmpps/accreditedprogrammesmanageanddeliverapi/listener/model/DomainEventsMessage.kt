package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SQSMessageHistoryEntity
import java.time.LocalDateTime

data class DomainEventsMessage(
  val eventType: String,
  val detailUrl: String,
  val description: String? = null,
  val additionalInformation: Map<String, Any>? = mapOf(),
  val personReference: PersonReference = PersonReference(),
  val occurredAt: LocalDateTime,
) {
  companion object {
    fun toEntity(domainEventsMessage: DomainEventsMessage, objectMapper: ObjectMapper): SQSMessageHistoryEntity = SQSMessageHistoryEntity(
      id = null,
      eventType = domainEventsMessage.eventType,
      detailUrl = domainEventsMessage.detailUrl,
      description = domainEventsMessage.description,
      occurredAt = domainEventsMessage.occurredAt,
      message = objectMapper.writeValueAsString(domainEventsMessage),
      createdAt = LocalDateTime.now(),
    )
  }
}

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsNumber() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}
data class PersonIdentifier(val type: String, val value: String)
