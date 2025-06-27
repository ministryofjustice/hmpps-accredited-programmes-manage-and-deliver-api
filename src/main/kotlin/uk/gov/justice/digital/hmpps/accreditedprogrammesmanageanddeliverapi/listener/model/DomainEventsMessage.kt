package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

data class DomainEventsMessage(
  val eventType: String,
  val detailUrl: String,
  val additionalInformation: Map<String, Any>? = mapOf(),
  val personReference: PersonReference = PersonReference(),
)

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsNumber() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}
data class PersonIdentifier(val type: String, val value: String)
