package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomPrisonNumber
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.PersonReference
import java.time.LocalDateTime

class DomainEventsMessageFactory {
  private var eventType: String = "interventions.community-referral.created"
  private var description: String? = randomUppercaseString()
  private var additionalInformation: Map<String, Any>? = mapOf("nomsNumber" to randomPrisonNumber())
  private var personReference: PersonReference = PersonReference(
    listOf(
      PersonIdentifier(
        "NOMS",
        randomUppercaseString(),
      ),
    ),
  )
  private var occurredAt: LocalDateTime = LocalDateTime.now()
  private var detailUrl: String = "https://api.example.com/referral/c8e93888-8d54-406c-82e7-889e84d7cc7e"

  fun withEventType(eventType: String) = apply { this.eventType = eventType }
  fun withDetailUrl(detailUrl: String) = apply { this.detailUrl = detailUrl }
  fun withDescription(description: String?) = apply { this.description = description }
  fun withAdditionalInformation(additionalInformation: Map<String, Any>?) = apply { this.additionalInformation = additionalInformation }
  fun withPersonReference(personReference: PersonReference) = apply { this.personReference = personReference }
  fun withOccurredAt(occurredAt: LocalDateTime) = apply { this.occurredAt = occurredAt }

  fun produce() = DomainEventsMessage(
    eventType = this.eventType,
    description = this.description,
    additionalInformation = this.additionalInformation,
    personReference = this.personReference,
    detailUrl = this.detailUrl,
    occurredAt = this.occurredAt,
  )
}
