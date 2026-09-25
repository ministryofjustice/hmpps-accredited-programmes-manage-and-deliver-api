package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService

/**
 * Handler for intervention.session-appointment.session-feedback-submitted domain events.
 * Processes session feedback events when participants provide attendance outcomes/notes.
 */
@Component
@Transactional
class SessionFeedbackSubmittedHandler(
  private val objectMapper: ObjectMapper,
  private val telemetryService: TelemetryService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val SESSION_ID_KEY = "sessionId"
    private const val FEEDBACK_ID_KEY = "feedbackId"
    private const val ATTENDANCE_ID_KEY = "attendanceId"
  }

  fun handle(sqsMessage: SQSMessage) {
    try {
      log.info("Processing session feedback submitted event: ${sqsMessage.eventId}")

      val additionalInfo = sqsMessage.additionalInformation
      if (additionalInfo == null) {
        log.warn("No additionalInformation found in session feedback event with messageId: ${sqsMessage.messageId}")
        return
      }

      val sessionId = additionalInfo[SESSION_ID_KEY]?.toString()
      val feedbackId = additionalInfo[FEEDBACK_ID_KEY]?.toString()
      val attendanceId = additionalInfo[ATTENDANCE_ID_KEY]?.toString()

      if (sessionId == null) {
        log.warn("Session ID is null for event with messageId: ${sqsMessage.messageId}")
        return
      }

      log.info(
        "Session feedback submitted: sessionId=$sessionId, feedbackId=$feedbackId, attendanceId=$attendanceId",
      )

      telemetryService.logToAppInsights(
        eventName = "SessionFeedback.submitted-event-received",
        properties = mapOf(
          "sessionId" to sessionId,
          "feedbackId" to (feedbackId ?: ""),
          "attendanceId" to (attendanceId ?: ""),
          "eventType" to sqsMessage.eventType,
        ),
      )

      // TODO: Implement business logic to sync feedback to external systems
      // Examples:
      // - Update nDelius appointment status
      // - Sync to Activities Management API
      // - Update any related records

      log.info("Successfully processed session feedback for sessionId=$sessionId")

      telemetryService.logToAppInsights(
        eventName = "SessionFeedback.submitted-event-processed.success",
        properties = mapOf(
          "sessionId" to sessionId,
          "attendanceId" to (attendanceId ?: ""),
        ),
      )
    } catch (e: Exception) {
      log.error("Error processing session feedback submitted event: ${e.message}", e)
      telemetryService.logToAppInsights(
        eventName = "SessionFeedback.submitted-event-processed.failure",
        properties = mapOf(
          "errorMessage" to (e.message?.trim() ?: "Unknown error"),
        ),
      )
      throw e
    }
  }
}
