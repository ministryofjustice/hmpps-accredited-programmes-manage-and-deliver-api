package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SQSMessageHistoryRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.temporal.ChronoUnit

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var sqsMessageHistoryRepository: SQSMessageHistoryRepository

  @Autowired
  lateinit var testDataCleaner: TestDataCleaner

  @BeforeEach
  fun setUp() {
    testDataCleaner.cleanAllTables()
  }

  fun sendDomainEvent(message: DomainEventsMessage, queueUrl: String = domainEventQueue.queueUrl): SendMessageResponse = domainEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(SQSMessage(objectMapper.writeValueAsString(message))),
      ).build(),
  ).get()

  @Test
  fun `should handle create referral message successfully`() {
    // Given
    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory().withEventType(eventType).produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    // Then
    assertEquals(0, domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get())
    sqsMessageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.occurredAt?.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(domainEventsMessage.occurredAt.truncatedTo(ChronoUnit.SECONDS))
      assertThat(it.message).isEqualTo(
        objectMapper.writeValueAsString(domainEventsMessage),
      )
    }
  }

  @Test
  fun `should throw exception for unknown message event type`() {
    // Given
    val eventType = "unknown.event.type.created"
    val domainEventsMessage = DomainEventsMessageFactory().withEventType(eventType).produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await untilCallTo {
      try {
        throw IllegalStateException("Unexpected event type received: unknown.event.type.created")
      } catch (e: IllegalStateException) {
        e.message
      }
    } matches { it == "Unexpected event type received: unknown.event.type.created" }
  }
}
