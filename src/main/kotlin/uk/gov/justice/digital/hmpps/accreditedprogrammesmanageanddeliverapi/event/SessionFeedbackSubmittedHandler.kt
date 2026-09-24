package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage

@Service
class SessionFeedbackSubmittedHandler(
  val objectMapper: ObjectMapper,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  fun handle(sqsMessage: SQSMessage) {
    try {
      logger.info("Processing session feedback submitted event: ${sqsMessage.eventId}")

      // Extract the additionalInformation from the message
      val additionalInfo = sqsMessage.additionalInformation

      if (additionalInfo == null) {
        logger.warn("No additionalInformation found in session feedback event")
        return
      }

      val sessionId = additionalInfo["sessionId"]?.toString()
      val feedbackId = additionalInfo["feedbackId"]?.toString()
      val attendanceId = additionalInfo["attendanceId"]?.toString()

      logger.info(
        "Session feedback submitted: sessionId=$sessionId, feedbackId=$feedbackId, attendanceId=$attendanceId",
      )

      // TODO: Implement business logic to sync feedback to external systems
      // Examples:
      // - Update nDelius appointment status
      // - Sync to Activities Management API
      // - Update any related records

      logger.info("Successfully processed session feedback for sessionId=$sessionId")
    } catch (e: Exception) {
      logger.error("Error processing session feedback submitted event", e)
      throw e
    }
  }
}
