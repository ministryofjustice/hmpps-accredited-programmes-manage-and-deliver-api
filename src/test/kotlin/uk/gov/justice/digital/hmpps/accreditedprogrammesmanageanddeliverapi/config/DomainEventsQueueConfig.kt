package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
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

  val interventionsQueue by lazy {
    hmppsQueueService.findByQueueId("interventionseventtestqueue")
      ?: throw MissingQueueException("interventionseventtestqueue queue not found")
  }

  val domainEventQueue: HmppsQueue by lazy {
    hmppsQueueService.findByQueueId("hmppsdomaineventsqueue")
      ?: throw MissingQueueException("HmppsQueue hmppsdomaineventsqueue not found")
  }

  val domainEventQueueDlqClient by lazy { domainEventQueue.sqsDlqClient }
  val domainEventQueueClient by lazy { domainEventQueue.sqsClient }
  val interventionsQueueDlqClient by lazy { interventionsQueue.sqsDlqClient }
  val interventionsQueueClient by lazy { interventionsQueue.sqsClient }

  val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtopic")
      ?: throw MissingTopicException("hmppseventtopic not found")
  }

  fun sendDomainEvent(event: DomainEventsMessage) {
    domainEventsTopic.publish(event.eventType, objectMapper.writeValueAsString(event))
  }

  fun HmppsQueue.countAllMessagesOnQueue(): Int? = sqsClient.countAllMessagesOnQueue(queueUrl).get()

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

    interventionsQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(interventionsQueue.queueUrl).build(),
    )?.get()

    interventionsQueueDlqClient?.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(interventionsQueue.dlqUrl).build(),
    )?.get()
  }
}
