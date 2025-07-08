package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.temporal.ChronoUnit
import java.util.UUID

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Autowired
  lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @Autowired
  lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  lateinit var testDataCleaner: TestDataCleaner

  lateinit var sourceReferralId: UUID

  @BeforeEach
  fun setUp() {
    testDataCleaner.cleanAllTables()

    sourceReferralId = UUID.randomUUID()
    stubAuthTokenEndpoint()
    log.info("Setting up ReferralDetails with id: $sourceReferralId")

    val referralDetails = ReferralDetails(
      interventionName = "Test Intervention",
      interventionType = "ACP",
      referralId = sourceReferralId,
      personReference = "X123456",
      personReferenceType = "CRN",
      setting = "Community",
    )

    wiremock.stubFor(
      get(urlEqualTo("/referral/$sourceReferralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(referralDetails)),
        ),
    )
  }

  fun sendDomainEvent(message: DomainEventsMessage, queueUrl: String = domainEventQueue.queueUrl): SendMessageResponse = domainEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(SQSMessage(objectMapper.writeValueAsString(message))),
      ).build(),
  ).get()

  @Test
  fun `should create message history on receipt of community-referral created message`() {
    // Given
    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withEventType(eventType)
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
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
  fun `should create referral with status history on receipt of community referral creation message`() {
    // Given
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      it?.setting == "Community"
      it?.crn == "X123456"
      it?.interventionName == "Test Intervention"
      it?.interventionType == "ACP"
    }

    val statusHistories = referralStatusHistoryRepository.findAll()
    assertThat(statusHistories.size).isEqualTo(1)
    assertThat(statusHistories[0].status).isEqualTo("Created")

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(domainEventsMessage.eventType)
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
