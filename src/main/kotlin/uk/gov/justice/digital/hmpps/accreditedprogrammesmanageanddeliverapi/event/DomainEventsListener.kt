package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.INTERVENTIONS_COMMUNITY_REFERRAL_CREATED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage

@Service
class DomainEventsListener(
  val objectMapper: ObjectMapper,
  val referralCreatedHandler: ReferralCreatedHandler,
  val referralImportedHandler: ReferralImportedHandler,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(sqsMessage: SQSMessage) {
    logger.info("Received Event of type: ${sqsMessage.eventType}")
    when (sqsMessage.eventType) {
      INTERVENTIONS_COMMUNITY_REFERRAL_CREATED.value -> referralCreatedHandler.handle(sqsMessage)
      REFERRAL_IMPORTED -> referralImportedHandler.handle(sqsMessage)
      else -> logger.info("Unknown event type ${sqsMessage.eventType}")
    }
  }

  companion object {
    const val REFERRAL_CREATED = "interventions.community-referral.created"
    const val REFERRAL_IMPORTED = "interventions.community-referral.imported"
  }
}
