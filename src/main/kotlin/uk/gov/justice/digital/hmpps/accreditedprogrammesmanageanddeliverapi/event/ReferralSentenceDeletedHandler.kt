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
class ReferralSentenceDeletedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryService: TelemetryService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val APP_INSIGHTS_SENTENCE_DELETED_CASE_REFERENCE_NUMBER_PROPERTY_KEY = "caseReferenceNumber"
    private const val APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE =
      "Referral.sentence-deleted-event-processed.failure"
  }

  fun handle(sqsMessage: SQSMessage) {
    val messageId = sqsMessage.messageId
    log.info("Starting handle for messageId: $messageId")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val caseReferenceNumber = message.personReference.findCrn()
      if (caseReferenceNumber.isNullOrEmpty()) {
        telemetryService.logToAppInsights(
          eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          properties = mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "case reference number is null",
            APP_INSIGHTS_SENTENCE_DELETED_CASE_REFERENCE_NUMBER_PROPERTY_KEY to (caseReferenceNumber?.trim() ?: ""),
          ),
        )
        return log.warn("case reference number is null for referral sentence deleted event with messageId: $messageId")
      }

      log.info("Received referral sentence deleted event for case reference number: $caseReferenceNumber")
      telemetryService.logToAppInsights(
        eventName = "Referral.sentence-deleted-event-received.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_SENTENCE_DELETED_CASE_REFERENCE_NUMBER_PROPERTY_KEY to caseReferenceNumber,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))
      referralService.deleteReferralByCaseReferenceNumber(caseReferenceNumber)

      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryService.logToAppInsights(
        eventName = "Referral.sentence-deleted-event-processed.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_SENTENCE_DELETED_CASE_REFERENCE_NUMBER_PROPERTY_KEY to caseReferenceNumber,
        ),
      )
    } catch (exception: Exception) {
      log.error("Error handling ReferralSentenceDeletedEvent: ${exception.message}", exception)
      telemetryService.logToAppInsights(
        eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
        properties = mapOf(
          APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to (exception.message?.trim() ?: ""),
        ),
      )
      throw exception
    }
  }
}
