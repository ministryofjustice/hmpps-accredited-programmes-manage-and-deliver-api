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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@Component
@Transactional
class ReferralImportedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val ADDITIONAL_INFORMATION_REFERRAL_ID_KEY = "REFERRAL_ID"
  }

  fun handle(sqsMessage: SQSMessage) {
    log.info("Starting handle for messageId: ${sqsMessage.messageId}")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      if (message.detailUrl == null) {
        return log.warn("Detail url is null for event with messageId: ${sqsMessage.messageId}")
      }
      val referralId = message.additionalInformation?.get(ADDITIONAL_INFORMATION_REFERRAL_ID_KEY)?.toString()
        ?.let { UUID.fromString(it) }
        ?: return log.warn("Referral ID is null for event with messageId: ${sqsMessage.messageId}")
      log.info("Received referral imported event for referral id: $referralId")

      telemetryClient.logToAppInsights(
        "Referral.imported-event-received.success",
        mapOf(
          "eventType" to message.eventType,
          "referralId" to referralId.toString(),
          "crn" to message.personReference.findCrn()!!,
        ),
      )

      messageHistoryRepository.save(message.toEntity(objectMapper.writeValueAsString(message)))

      runBlocking {
        referralService.refreshPersonalDetailsForReferral(referralId, false)
      }
      log.info("Ending handle for messageId: ${sqsMessage.messageId}")
      telemetryClient.logToAppInsights(
        "Referral.imported-event-processed.success",
        mapOf(
          "eventType" to message.eventType,
          "referralId" to referralId.toString(),
          "crn" to message.personReference.findCrn()!!,
        ),
      )
    } catch (e: Exception) {
      log.error("Error handling ReferralImportedEvent: ${e.message}", e)
      telemetryClient.logToAppInsights(
        "Referral.imported-event-processed.failure",
        mapOf(
          "errorMessage" to (e.message?.trim() ?: ""),
        ),
      )
      throw e
    }
  }
}
