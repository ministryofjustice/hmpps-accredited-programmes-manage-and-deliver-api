package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import java.time.ZonedDateTime

@Service
@Transactional
class DomainEventService(
  private val domainEventPublisher: DomainEventPublisher,

  @Value("\${services.manage-and-deliver-api.base-url}")
  private val madBaseUrl: String,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun publishReferralStatusUpdatedEvent(referral: ReferralEntity) {
    val hmppsDomainEvent = DomainEventsMessage(
      eventType = HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_STATUS_UPDATED.value,
      version = 1,
      detailUrl = "$madBaseUrl/referral/${referral.id}/status-change-details",
      occurredAt = ZonedDateTime.now(),
      description = "An Accredited Programmes referral in community has had it's status updated.",
      additionalInformation = mutableMapOf(),
      personReference = PersonReference.fromCrn(referral.crn),
    )
    log.info("Publishing ${HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_STATUS_UPDATED.value} event for referralId: ${referral.id}")
    domainEventPublisher.publish(hmppsDomainEvent)
  }
}
