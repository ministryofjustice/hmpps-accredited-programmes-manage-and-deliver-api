package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import uk.gov.justice.hmpps.sqs.publish

@TestComponent
class DomainEventsQueueConfig {
  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val domainEventQueue: HmppsQueue by lazy {
    hmppsQueueService.findByQueueId("hmppsdomaineventsqueue")
      ?: throw MissingQueueException("HmppsQueue hmppsdomaineventsqueue not found")
  }

  val domainEventQueueDlqClient by lazy { domainEventQueue.sqsDlqClient }
  val domainEventQueueClient by lazy { domainEventQueue.sqsClient }

  val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppsdomaineventstopic")
      ?: throw MissingTopicException("hmppsdomaineventstopic not found")
  }

  fun sendDomainEvent(event: DomainEventsMessage) {
    domainEventsTopic.publish(event.eventType, objectMapper.writeValueAsString(event))
  }

  fun HmppsQueue.countAllMessagesOnQueue() = sqsClient.countAllMessagesOnQueue(queueUrl).get()

  fun HmppsQueue.receiveMessageOnQueue() = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build())
    .get()
    .messages()
    .single()

  fun purgeAllQueues() {
    domainEventQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(domainEventQueue.queueUrl).build(),
    ).get()

    domainEventQueueDlqClient?.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(domainEventQueue.dlqUrl).build(),
    )?.get()
  }
}
