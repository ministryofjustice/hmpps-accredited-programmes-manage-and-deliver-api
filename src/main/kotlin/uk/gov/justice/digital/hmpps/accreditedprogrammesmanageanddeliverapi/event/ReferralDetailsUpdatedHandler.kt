package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AppInsightsConstants.APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AppInsightsConstants.APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@Component
@Transactional
class ReferralDetailsUpdatedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val APP_INSIGHTS_REFERRAL_ID_PROPERTY_KEY = "referralId"
    private const val APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE =
      "Referral.details-updated-event-processed.failure"
  }

  fun handle(sqsMessage: SQSMessage) {
    val messageId = sqsMessage.messageId
    log.info("Starting handle for messageId: $messageId")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val referralIdString = message.referralId
      if (referralIdString.isNullOrEmpty()) {
        telemetryClient.logToAppInsights(
          APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "referralId is blank",
            APP_INSIGHTS_REFERRAL_ID_PROPERTY_KEY to (referralIdString?.trim() ?: ""),
          ),
        )
        return log.warn("referralId is blank for referral details updated event with messageId: $messageId")
      }

      log.info("Received referral details updated event for referralId: $referralIdString")
      telemetryClient.logToAppInsights(
        "Referral.details-updated-event-received.success",
        mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_REFERRAL_ID_PROPERTY_KEY to referralIdString,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))
      val referralId = UUID.fromString(referralIdString)
      val result = runBlocking {
        referralService.refreshPersonalDetailsForReferral(referralId, false)
      }

      if (result == null) {
        log.warn("No referral found for referralId: $referralId — skipping refresh")
        telemetryClient.logToAppInsights(
          APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "referral not found",
            APP_INSIGHTS_REFERRAL_ID_PROPERTY_KEY to referralId.toString(),
          ),
        )
        return
      }

      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryClient.logToAppInsights(
        "Referral.details-updated-event-processed.success",
        mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_REFERRAL_ID_PROPERTY_KEY to referralId.toString(),
        ),
      )
    } catch (e: Exception) {
      log.error("Error handling ReferralDetailsUpdatedEvent: ${e.message}", e)
      telemetryClient.logToAppInsights(
        APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
        mapOf(
          APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to (e.message?.trim() ?: ""),
          "sqsMessage" to sqsMessage.toString(),
        ),
      )
      throw e
    }
  }
}
