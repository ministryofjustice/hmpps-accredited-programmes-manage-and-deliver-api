package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AppInsightsConstants.APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AppInsightsConstants.APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService

@Component
@Transactional
class ReferralMergedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryService: TelemetryService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val APP_INSIGHTS_SOURCE_CRN_PROPERTY_KEY = "sourceCrn"
    private const val APP_INSIGHTS_TARGET_CRN_PROPERTY_KEY = "targetCrn"
    private const val APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE =
      "Referral.merged-event-processed.failure"
  }

  fun handle(sqsMessage: SQSMessage) {
    val messageId = sqsMessage.messageId
    log.info("Starting handle for messageId: $messageId")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val sourceCrn = message.sourceCrn
      val targetCrn = message.targetCrn
      if (sourceCrn.isNullOrEmpty() || targetCrn.isNullOrEmpty()) {
        telemetryService.logToAppInsights(
          eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          properties = mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "sourceCrn or targetCrn is null",
            APP_INSIGHTS_SOURCE_CRN_PROPERTY_KEY to (sourceCrn?.trim() ?: ""),
            APP_INSIGHTS_TARGET_CRN_PROPERTY_KEY to (targetCrn?.trim() ?: ""),
          ),
        )
        return log.warn("sourceCrn or targetCrn is null for referral merged event with messageId: $messageId")
      }

      log.info("Received referral merged event for sourceCrn: $sourceCrn and targetCrn: $targetCrn")
      telemetryService.logToAppInsights(
        eventName = "Referral.merged-event-received.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_SOURCE_CRN_PROPERTY_KEY to sourceCrn,
          APP_INSIGHTS_TARGET_CRN_PROPERTY_KEY to targetCrn,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))
      referralService.updateReferralCrn(sourceCrn, targetCrn)

      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryService.logToAppInsights(
        eventName = "Referral.merged-event-processed.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_SOURCE_CRN_PROPERTY_KEY to sourceCrn,
          APP_INSIGHTS_TARGET_CRN_PROPERTY_KEY to targetCrn,
        ),
      )
    } catch (e: Exception) {
      log.error("Error handling ReferralMergedEvent: ${e.message}", e)
      telemetryService.logToAppInsights(
        eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
        properties = mapOf(
          APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to (e.message?.trim() ?: ""),
          "sqsMessage" to sqsMessage.toString(),
        ),
      )
      throw e
    }
  }
}
