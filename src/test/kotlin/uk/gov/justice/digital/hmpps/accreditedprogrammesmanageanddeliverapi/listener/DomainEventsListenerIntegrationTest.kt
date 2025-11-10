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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.Duration.ofSeconds
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

// TODO remove this suppression when this issue is fixed
// https://youtrack.jetbrains.com/issue/KT-78352/False-positive-IDENTITYSENSITIVEOPERATIONSWITHVALUETYPE-when-comparing-with-equality-operator
@Suppress("IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE")
class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Autowired
  lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  lateinit var sourceReferralId: UUID

  @BeforeEach
  fun setUp() {
    testDataCleaner.cleanAllTables()

    sourceReferralId = UUID.randomUUID()
    stubAuthTokenEndpoint()
    log.info("Setting up ReferralDetails with id: $sourceReferralId")

    val crn = "X123456"
    val eventNumber = 1

    val findAndReferReferralDetails = FindAndReferReferralDetailsFactory()
      .withInterventionName("Test Intervention")
      .withInterventionType(InterventionType.ACP)
      .withReferralId(sourceReferralId)
      .withPersonReference(crn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withSourcedFromReference("LIC-12345")
      .withEventNumber(eventNumber)
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

    oasysApiStubs.stubSuccessfulPniResponse("X123456")

    val personalDetails = NDeliusPersonalDetailsFactory()
      .withName(
        FullName(
          forename = "John",
          middleNames = "Alex",
          surname = "Doe",
        ),
      )
      .withCrn(crn)
      .withSex(CodeDescription("M", "Male"))
      .withDateOfBirth(LocalDate.parse("2000-10-01"))
      .withTeam(
        CodeDescription(
          code = "1234",
          description = "TEAM_1",
        ),
      )
      .withProbationDeliveryUnit(
        CodeDescription(
          code = "1234",
          description = "PDU_1",
        ),
      )
      .withRegion(
        CodeDescription(
          code = "1234",
          description = "REGION_1",
        ),
      )
      .produce()
    nDeliusApiStubs.stubPersonalDetailsResponse(personalDetails)
    val sentenceInformation =
      NDeliusSentenceResponseFactory().withLicenceExpiryDate(LocalDate.parse("2025-10-01")).produce()
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(crn, eventNumber, sentenceInformation)
  }

  @Test
  fun `should create message history on receipt of community-referral created message`() {
    // Given
    val eventType = "interventions.community-referral.created"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("/referral/$sourceReferralId")
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    assertThat(referralRepository.count()).isEqualTo(0)
  }

  @Test
  fun `should create referral with status history on receipt of community referral creation message`() {
    // Given
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
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
      it.personName == "John Alex Doe"
      it.sex == "Male"
      it.dateOfBirth == LocalDate.parse("2000-10-01")
      it.referralReportingLocationEntity!!.reportingTeam == "TEAM_1"
      it.referralReportingLocationEntity!!.pduName == "PDU_1"
      it.referralReportingLocationEntity!!.regionName == "REGION_1"
      it.sentenceEndDate!! == LocalDate.parse("2025-10-01")
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
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
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
      it.personName == "John Alex Doe"
      it.sex == "Male"
      it.dateOfBirth == LocalDate.parse("2000-10-01")
      it.referralLdcHistories.first().hasLdc
      it.referralReportingLocationEntity!!.reportingTeam == "TEAM_1"
      it.referralReportingLocationEntity!!.pduName == "PDU_1"
      it.sentenceEndDate!! == LocalDate.parse("2025-10-01")
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
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
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
      it.sex == "Male"
      it.dateOfBirth == LocalDate.parse("2000-10-01")
      !it.referralLdcHistories.first().hasLdc
      it.referralLdcHistories.first().createdBy == "SYSTEM"
      it.personName == "John Alex Doe"
      it.referralReportingLocationEntity!!.reportingTeam == "TEAM_1"
      it.referralReportingLocationEntity!!.pduName == "PDU_1"
      it.sentenceEndDate!! == LocalDate.parse("2025-10-01")
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
  fun `should create referral and automatically assign ldc status false on receipt of community referral creation message when PNI doesn't exist`() {
    // Given
    val crn = "X123456"

    oasysApiStubs.stubNotFoundPniResponse(crn)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofSeconds(1) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
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
      it.sex == "Male"
      it.dateOfBirth == LocalDate.parse("2000-10-01")
      !it.referralLdcHistories.first().hasLdc
      it.referralLdcHistories.first().createdBy == "SYSTEM"
      it.personName == "John Alex Doe"
      it.referralReportingLocationEntity!!.reportingTeam == "TEAM_1"
      it.referralReportingLocationEntity!!.pduName == "PDU_1"
      it.sentenceEndDate!! == LocalDate.parse("2025-10-01")
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
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

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
