package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.SQSMessage
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

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
    val domainEventsMessage = DomainEventsMessage(
      eventType,
      detailUrl = "https://find-and-refer-dev.justice.gov.uk/api/referral/X234234",
      additionalInformation = emptyMap(),
    )

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    // Then
    assertEquals(0, domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get())
    // TODO verify a referral has been persisted etc
  }
}
