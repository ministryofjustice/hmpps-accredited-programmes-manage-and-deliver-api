package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage

@Service
class DomainEventsListener(
  val objectMapper: ObjectMapper,
  val referralCreatedHandler: ReferralCreatedHandler,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(sqsMessage: SQSMessage) {
    logger.info("Received Event of type: ${sqsMessage.eventType}")
    when (sqsMessage.eventType) {
      REFERRAL_CREATED -> referralCreatedHandler.handle(sqsMessage)
      else -> logger.info("Unknown event type ${sqsMessage.eventType}")
    }
  }

  companion object {
    const val REFERRAL_CREATED = "interventions.community-referral.created"
  }
}
