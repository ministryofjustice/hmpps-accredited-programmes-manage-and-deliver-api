package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
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

@Component
@Transactional
class ReferralUnmergedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val APP_INSIGHTS_UNMERGED_CRN_PROPERTY_KEY = "unmergedCrn"
    private const val APP_INSIGHTS_REACTIVATED_CRN_PROPERTY_KEY = "reactivatedCrn"
    private const val APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE =
      "Referral.unmerged-event-processed.failure"
  }

  fun handle(sqsMessage: SQSMessage) {
    val messageId = sqsMessage.messageId
    log.info("Starting handle for messageId: $messageId")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val unmergedCrn = message.unmergedCrn
      val reactivatedCrn = message.reactivatedCrn
      if (unmergedCrn.isNullOrEmpty() || reactivatedCrn.isNullOrEmpty()) {
        telemetryClient.logToAppInsights(
          APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "unmergedCrn or reactivatedCrn is null",
            APP_INSIGHTS_UNMERGED_CRN_PROPERTY_KEY to (unmergedCrn?.trim() ?: ""),
            APP_INSIGHTS_REACTIVATED_CRN_PROPERTY_KEY to (reactivatedCrn?.trim() ?: ""),
          ),
        )
        return log.warn("unmergedCrn or reactivatedCrn is null for referral unmerged event with messageId: $messageId")
      }

      log.info("Received referral unmerged event for unmergedCrn: $unmergedCrn and reactivatedCrn: $reactivatedCrn")
      telemetryClient.logToAppInsights(
        "Referral.unmerged-event-received.success",
        mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_UNMERGED_CRN_PROPERTY_KEY to unmergedCrn,
          APP_INSIGHTS_REACTIVATED_CRN_PROPERTY_KEY to reactivatedCrn,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))
      referralService.updateReferralCrn(unmergedCrn, reactivatedCrn)

      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryClient.logToAppInsights(
        "Referral.unmerged-event-processed.success",
        mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_UNMERGED_CRN_PROPERTY_KEY to unmergedCrn,
          APP_INSIGHTS_REACTIVATED_CRN_PROPERTY_KEY to reactivatedCrn,
        ),
      )
    } catch (e: Exception) {
      log.error("Error handling ReferralUnmergedEvent: ${e.message}", e)
      telemetryClient.logToAppInsights(
        APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
        mapOf(
          APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to (e.message?.trim() ?: ""),
        ),
      )
      throw e
    }
  }
}
