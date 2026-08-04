package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TelemetryUtils
import java.util.UUID
import kotlin.system.measureTimeMillis

@Service
@Transactional
class ReferralCreatedHandler(
  private val objectMapper: ObjectMapper,
  private val messageHistoryRepository: MessageHistoryRepository,
  private val referralService: ReferralService,
  private val telemetryUtils: TelemetryUtils,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun handle(sqsMessage: SQSMessage) {
    val domainEventMessage: DomainEventsMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)

    if (domainEventMessage.detailUrl == null) {
      messageHistoryRepository.save(domainEventMessage.toEntity(objectMapper.writeValueAsString(domainEventMessage)))
      return log.info("Detail url is null for event with messageId: ${sqsMessage.messageId}")
    }

    val referralId = UUID.fromString(domainEventMessage.detailUrl.split("/").last())
    log.info("Received referral created event for referral id: $referralId for CRN: ${domainEventMessage.personReference.findCrn()}")

    telemetryUtils.logToAppInsights(
      eventName = "Probation.case-requirement.created event received",
      properties = mapOf(
        "eventType" to domainEventMessage.eventType,
        "referralId" to referralId.toString(),
        "crn" to domainEventMessage.personReference.findCrn()!!,
      ),
    )

    val savedMessage =
      messageHistoryRepository.save(domainEventMessage.toEntity(objectMapper.writeValueAsString(domainEventMessage)))

    val time = measureTimeMillis {
      val referralDetails = referralService.getFindAndReferReferralDetails(referralId)
      val savedReferral = referralService.createReferral(referralDetails)
      savedMessage.referral = savedReferral
      messageHistoryRepository.save(savedMessage)
    }

    savedMessage.msToProcess = time
    messageHistoryRepository.save(savedMessage)
  }
}
