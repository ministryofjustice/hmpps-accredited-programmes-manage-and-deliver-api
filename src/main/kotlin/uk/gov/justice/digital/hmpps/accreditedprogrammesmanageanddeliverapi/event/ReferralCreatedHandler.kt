package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
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
class ReferralCreatedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun handle(sqsMessage: SQSMessage) {
    val domainEventMessage: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)

    if (domainEventMessage.detailUrl == null) {
      return log.info("Detail url is null for event with messageId: ${sqsMessage.messageId}")
    }
    val referralId = extractReferralId(domainEventMessage.detailUrl)
    log.info("Received referral created event for referral id: $referralId")

    telemetryClient.logToAppInsights(
      "Probation.case-requirement.created event received",
      mapOf(
        "eventType" to domainEventMessage.eventType,
        "referralId" to referralId.toString(),
        "crn" to domainEventMessage.personReference.findCrn()!!,
      ),
    )

    messageHistoryRepository.save(domainEventMessage.toEntity(objectMapper.writeValueAsString(domainEventMessage)))

    val referralDetails = referralService.getFindAndReferReferralDetails(referralId)
    referralService.createReferral(referralDetails)
  }

  private fun extractReferralId(detailUrl: String) = UUID.fromString(detailUrl.split("/").last())
}
