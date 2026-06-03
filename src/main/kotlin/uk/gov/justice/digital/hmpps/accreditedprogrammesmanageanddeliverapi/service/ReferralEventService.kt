package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_STATUS_UPDATED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.REFERRAL_COMPLETED_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.UPDATE_REFERRAL_STATUS_N_DELIUS
import java.time.ZonedDateTime
import java.util.UUID

@Service
class ReferralEventService(
  private val domainEventPublisher: DomainEventPublisher,
  private val telemetryClient: TelemetryClient,
  @Value("\${services.manage-and-deliver-api.base-url}") private val madBaseUrl: String,
  private val referralService: ReferralService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun publishReferralStatusUpdatedEvent(referralId: UUID) {
    val referral = referralService.getReferralById(referralId)
    val message = DomainEventsMessage(
      eventType = ACP_COMMUNITY_REFERRAL_STATUS_UPDATED.value,
      version = 1,
      detailUrl = "$madBaseUrl/referral/${referral.id}/status-change-details",
      occurredAt = ZonedDateTime.now(),
      description = "An Accredited Programmes referral in community has had it's status updated.",
      additionalInformation = mutableMapOf(),
      personReference = PersonReference.fromCrn(referral.crn),
    )
    log.info("Publishing ${ACP_COMMUNITY_REFERRAL_STATUS_UPDATED.value} event for referralId: ${referral.id}")
    publishEvent(
      message,
      referral,
      UPDATE_REFERRAL_STATUS_N_DELIUS.name,
      UPDATE_REFERRAL_STATUS_N_DELIUS.eventName,
    )
  }

  fun publishReferralCompletedEvent(referralId: UUID) {
    val referral = referralService.getReferralById(referralId)
    val message = DomainEventsMessage(
      eventType = HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value,
      version = 1,
      detailUrl = "$madBaseUrl/referral/${referral.id}/completion-data",
      occurredAt = ZonedDateTime.now(),
      description = "An Accredited Programmes referral in community has been completed.",
      additionalInformation = mutableMapOf(),
      personReference = PersonReference.fromCrn(referral.crn),
    )
    log.info("Publishing ${HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value} event for referralId: ${referral.id}")
    publishEvent(
      message,
      referral,
      REFERRAL_COMPLETED_N_DELIUS.name,
      REFERRAL_COMPLETED_N_DELIUS.eventName,
    )
  }

  private fun publishEvent(
    domainEventsMessage: DomainEventsMessage,
    referral: ReferralEntity,
    integrationActionType: String,
    eventName: String,
  ) {
    try {
      domainEventPublisher.publish(domainEventsMessage)
      telemetryClient.logToAppInsights(
        "$eventName.success",
        mapOf(
          "integrationActionType" to integrationActionType,
          "regionName" to (referral.referralReportingLocation?.regionName ?: ""),
          "deliveryUnitCode" to (referral.referralReportingLocation?.pduName ?: ""),
          "outcome" to "success",
        ),
      )
    } catch (exception: Exception) {
      telemetryClient.logToAppInsights(
        "$eventName.failure",
        mapOf(
          "integrationActionType" to integrationActionType,
          "regionName" to (referral.referralReportingLocation?.regionName ?: ""),
          "deliveryUnitCode" to (referral.referralReportingLocation?.pduName ?: ""),
          "outcome" to "failure",
        ),
      )

      throw exception
    }
  }
}
