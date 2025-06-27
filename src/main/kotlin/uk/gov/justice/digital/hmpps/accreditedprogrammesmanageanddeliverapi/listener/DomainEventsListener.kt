package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.SQSMessage

@Service
class DomainEventsListener(
  private val objectMapper: ObjectMapper,
  val referralCreatedHandler: ReferralCreatedHandler,
) {

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(sqsMessage: SQSMessage) {
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(sqsMessage.message)
    when (domainEventMessage.eventType) {
      REFERRAL_CREATED -> referralCreatedHandler.handle(domainEventMessage)
    }
  }

  companion object {
    const val REFERRAL_CREATED = "interventions.community-referral.created"
  }
}
