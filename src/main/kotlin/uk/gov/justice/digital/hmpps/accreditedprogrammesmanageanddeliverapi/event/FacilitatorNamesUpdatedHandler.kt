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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.FacilitatorService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService
import java.util.UUID

@Component
@Transactional
class FacilitatorNamesUpdatedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val facilitatorService: FacilitatorService,
  private val telemetryService: TelemetryService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val APP_INSIGHTS_FACILITATOR_ID_PROPERTY_KEY = "facilitatorId"
    private const val APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE =
      "Facilitator.names-updated-event-processed.failure"
  }

  fun handle(sqsMessage: SQSMessage) {
    val messageId = sqsMessage.messageId
    log.info("Starting handle for messageId: $messageId")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val facilitatorIdString = message.facilitatorId
      if (facilitatorIdString.isNullOrEmpty()) {
        telemetryService.logToAppInsights(
          eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          properties = mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "facilitatorId is blank",
            APP_INSIGHTS_FACILITATOR_ID_PROPERTY_KEY to (facilitatorIdString?.trim() ?: ""),
          ),
        )
        return log.warn("facilitatorId is blank for facilitator names updated event with messageId: $messageId")
      }

      log.info("Received facilitator names updated event for facilitatorId: $facilitatorIdString")
      telemetryService.logToAppInsights(
        eventName = "Facilitator.details-updated-event-received.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_FACILITATOR_ID_PROPERTY_KEY to facilitatorIdString,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))
      val facilitatorId = UUID.fromString(facilitatorIdString)
      val result = facilitatorService.updateNameByFacilitatorId(facilitatorId)

      if (result == null) {
        log.warn("No facilitator found for ID: $facilitatorId — skipping refresh")
        telemetryService.logToAppInsights(
          eventName = APP_INSIGHTS_PROCESSED_FAILURE_EVENT_NAME_PROPERTY_VALUE,
          properties = mapOf(
            APP_INSIGHTS_ERROR_MESSAGE_PROPERTY_KEY to "facilitator not found",
            APP_INSIGHTS_FACILITATOR_ID_PROPERTY_KEY to facilitatorId.toString(),
          ),
        )
        return
      }

      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryService.logToAppInsights(
        eventName = "Facilitator.names-updated-event-processed.success",
        properties = mapOf(
          APP_INSIGHTS_TARGET_EVENT_TYPE_PROPERTY_KEY to message.eventType,
          APP_INSIGHTS_FACILITATOR_ID_PROPERTY_KEY to facilitatorId.toString(),
        ),
      )
    } catch (e: Exception) {
      log.error("Error handling FacilitatorNamesUpdatedEvent: ${e.message}", e)
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
