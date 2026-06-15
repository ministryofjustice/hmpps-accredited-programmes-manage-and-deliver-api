package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@Service
@Transactional
class ReferralImportedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun handle(sqsMessage: SQSMessage) {
    log.info("Starting handle for messageId: ${sqsMessage.messageId}")
    try {
      val message: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
      val detailUrl =
        message.detailUrl ?: return log.info("Detail url is null for event with messageId: ${sqsMessage.messageId}")
      val referralId = UUID.fromString(detailUrl.split("/").last())
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
    } catch (e: Exception) {
      log.error("Error handling ReferralImportedEvent: ${e.message}", e)
      throw e
    }
  }
}
