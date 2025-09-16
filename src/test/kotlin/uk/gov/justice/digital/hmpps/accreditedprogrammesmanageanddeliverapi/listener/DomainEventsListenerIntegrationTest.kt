package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.Duration.ofSeconds
import java.time.ZoneOffset
import java.util.UUID

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Autowired
  lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  lateinit var sourceReferralId: UUID
  lateinit var oasysApiStubs: OasysApiStubs

  @BeforeEach
  fun setUp() {
    testDataCleaner.cleanAllTables()

    sourceReferralId = UUID.randomUUID()
    stubAuthTokenEndpoint()
    log.info("Setting up ReferralDetails with id: $sourceReferralId")

    val findAndReferReferralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionName("Test Intervention")
      .withInterventionType(InterventionType.ACP)
      .withReferralId(sourceReferralId)
      .withPersonReference("X123456")
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withSourcedFromReference("LIC-12345")
      .produce()

    wiremock.stubFor(
      get(urlEqualTo("/referral/$sourceReferralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(findAndReferReferralDetails)),
        ),
    )

    oasysApiStubs = OasysApiStubs(wiremock, objectMapper)
    oasysApiStubs.stubSuccessfulPniResponse("X123456")
  }

  @Test
  fun `should create message history on receipt of community-referral created message`() {
    // Given
    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("/referral/$sourceReferralId")
      .withEventType(eventType)
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo { domainEventQueue.countAllMessagesOnQueue() } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.occurredAt).isEqualToIgnoringNanos(
        domainEventsMessage.occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
      )
      assertThat(it.message).isEqualTo(
        objectMapper.writeValueAsString(domainEventsMessage),
      )
    }
  }

  @Test
  fun `should create message history on receipt of community-referral created message but not insert referral when detail url is null`() {
    // Given
    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl(null)
      .withEventType(eventType)
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    assertThat(referralRepository.count()).isEqualTo(0)
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
    await withPollDelay ofSeconds(1) untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await withPollDelay ofSeconds(1) untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      it!!.setting == SettingType.COMMUNITY
      it.crn == "X123456"
      it.interventionName == "Test Intervention"
      it.interventionType == InterventionType.ACP
      it.sourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION
      it.eventId == "LIC-12345"
    }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(domainEventsMessage.eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.occurredAt).isEqualToIgnoringNanos(
        domainEventsMessage.occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
      )
      assertThat(it.message).isEqualTo(
        objectMapper.writeValueAsString(domainEventsMessage),
      )
    }
  }

  @Test
  fun `should create referral and automatically assign ldc status true when score greater than or equal to 3 and on receipt of community referral creation message`() {
    // Given
    val crn = "X123456"
    val ldc = Ldc(
      score = 4,
      subTotal = 4,
    )
    oasysApiStubs.stubSuccessfulPniResponseWithLdc(crn, ldc)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      it!!.setting == SettingType.COMMUNITY
      it.crn == "X123456"
      it.interventionName == "Test Intervention"
      it.interventionType == InterventionType.ACP
      it.statusHistories.first().referralStatusDescription.description == "Awaiting assessment"
      it.sourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION
      it.eventId == "LIC-12345"
      it.referralLdcHistories.first().hasLdc
    }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(domainEventsMessage.eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.occurredAt).isEqualToIgnoringNanos(
        domainEventsMessage.occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
      )
      assertThat(it.message).isEqualTo(
        objectMapper.writeValueAsString(domainEventsMessage),
      )
    }
  }

  @Test
  fun `should create referral and automatically assign ldc status false when score less than 3 and on receipt of community referral creation message`() {
    // Given
    val crn = "X123456"
    val ldc = Ldc(
      score = 2,
      subTotal = 2,
    )
    oasysApiStubs.stubSuccessfulPniResponseWithLdc(crn, ldc)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .produce()

    // When
    sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      it!!.setting == SettingType.COMMUNITY
      it.crn == "X123456"
      it.interventionName == "Test Intervention"
      it.interventionType == InterventionType.ACP
      it.statusHistories.first().referralStatusDescription.description == "Awaiting assessment"
      it.sourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION
      it.eventId == "LIC-12345"
      !it.referralLdcHistories.first().hasLdc
      it.referralLdcHistories.first().createdBy == "SYSTEM"
    }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(domainEventsMessage.eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.occurredAt).isEqualToIgnoringNanos(
        domainEventsMessage.occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
      )
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
    await withPollDelay ofSeconds(1) untilCallTo {
      try {
        throw IllegalStateException("Unexpected event type received: unknown.event.type.created")
      } catch (e: IllegalStateException) {
        e.message
      }
    } matches { it == "Unexpected event type received: unknown.event.type.created" }
  }
}
