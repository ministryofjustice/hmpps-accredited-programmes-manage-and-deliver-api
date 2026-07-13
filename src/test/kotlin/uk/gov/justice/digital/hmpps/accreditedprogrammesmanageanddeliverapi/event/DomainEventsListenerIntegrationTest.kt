package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

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
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumberAsInt
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusSentenceResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.Duration.ofMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.properties.Delegates

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  lateinit var sourceReferralId: UUID

  lateinit var crn: String
  var eventNumber by Delegates.notNull<Int>()

  @BeforeEach
  fun setUp() {
    crn = randomCrn()
    eventNumber = randomNumberAsInt(11)
    testDataCleaner.cleanAllTables()
    wiremock.resetAll()

    sourceReferralId = UUID.randomUUID()
    stubAuthTokenEndpoint()

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

    oasysApiStubs.stubSuccessfulPniResponse(crn)

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
      NDeliusSentenceResponseFactory().withExpectedEndDate(LocalDate.parse("2025-10-01")).produce()
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
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
      assertThat(it.detailUrl).isEqualTo(domainEventsMessage.detailUrl)
      assertThat(it.description).isEqualTo(domainEventsMessage.description)
      assertThat(it.msToProcess).isGreaterThan(0) // impossible to know ahead of time
      assertThat(it.occurredAt).isEqualToIgnoringNanos(
        domainEventsMessage.occurredAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
      )
      assertThat(it.message).isEqualTo(
        objectMapper.writeValueAsString(domainEventsMessage),
      )
    }
  }

  @Test
  fun `should create message history on receipt of community-referral imported message`() {
    // Given
    val eventType = "interventions.community-referral.imported"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(mapOf("REFERRAL_ID" to sourceReferralId.toString()))
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", "X957673"))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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
  fun `should create message history on receipt of probation case merge completed message`() {
    // Given
    val eventType = "probation-case.merge.completed"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(
        mapOf(
          "sourceCRN" to "X777878",
          "targetCRN" to "X777879",
        ),
      )
      .withEventType(eventType)
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
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
  fun `should create message history on receipt of probation case unmerge completed message`() {
    // Given
    val eventType = "probation-case.unmerge.completed"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(
        mapOf(
          "reactivatedCRN" to "X334583",
          "unmergedCRN" to "X776371",
        ),
      )
      .withEventType(eventType)
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
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
  fun `should create message history on receipt of ACP M&D referral details updated message`() {
    // Given
    val eventType = "accredited-programmes-manage-and-deliver.referral.details-updated"
    val referralId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(
        mapOf(
          "referralId" to referralId.toString(),
        ),
      )
      .withEventType(eventType)
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo(eventType)
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
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    assertThat(referralRepository.count()).isEqualTo(0)

    val messageHistory = messageHistoryRepository.findAll().firstOrNull()
    assertThat(messageHistory).isNotNull
    assertThat(messageHistory!!.referral).isNull()
    assertThat(messageHistory.msToProcess).isNull()
  }

  @Test
  fun `should create referral with status history on receipt of community referral creation message`() {
    // Given
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await withPollDelay ofMillis(100) untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      assertThat(it).isNotNull()
      assertThat(it!!.setting).isEqualTo(SettingType.COMMUNITY)
      assertThat(it.crn).isEqualTo(crn)
      assertThat(it.interventionName).isEqualTo("Test Intervention")
      assertThat(it.interventionType).isEqualTo(InterventionType.ACP)
      assertThat(it.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      assertThat(it.eventId).isEqualTo("LIC-12345")
      assertThat(it.personName).isEqualTo("John Alex Doe")
      assertThat(it.sex).isEqualTo("Male")
      assertThat(it.dateOfBirth).isEqualTo(LocalDate.parse("2000-10-01"))
      assertThat(it.referralReportingLocation!!.reportingTeam).isEqualTo("TEAM_1")
      assertThat(it.referralReportingLocation!!.pduName).isEqualTo("PDU_1")
      assertThat(it.referralReportingLocation!!.regionName).isEqualTo("REGION_1")
      assertThat(it.sentenceEndDate).isEqualTo(LocalDate.parse("2025-10-01"))
      true
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
    val ldc = Ldc(
      score = 4,
      subTotal = 4,
    )
    oasysApiStubs.stubSuccessfulPniResponseWithLdc(crn, ldc)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      assertThat(it!!.setting).isEqualTo(SettingType.COMMUNITY)
      assertThat(it.crn).isEqualTo(crn)
      assertThat(it.interventionName).isEqualTo("Test Intervention")
      assertThat(it.interventionType).isEqualTo(InterventionType.ACP)
      assertThat(it.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(it.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      assertThat(it.eventId).isEqualTo("LIC-12345")
      assertThat(it.personName).isEqualTo("John Alex Doe")
      assertThat(it.sex).isEqualTo("Male")
      assertThat(it.dateOfBirth).isEqualTo(LocalDate.parse("2000-10-01"))
      assertThat(it.referralLdcHistories.first().hasLdc).isTrue
      assertThat(it.referralReportingLocation!!.reportingTeam).isEqualTo("TEAM_1")
      assertThat(it.referralReportingLocation!!.pduName).isEqualTo("PDU_1")
      assertThat(it.sentenceEndDate!!).isEqualTo(LocalDate.parse("2025-10-01"))
      true
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
    val ldc = Ldc(
      score = 2,
      subTotal = 2,
    )
    oasysApiStubs.stubSuccessfulPniResponseWithLdc(crn, ldc)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      assertThat(it!!.setting).isEqualTo(SettingType.COMMUNITY)
      assertThat(it.crn).isEqualTo(crn)
      assertThat(it.interventionName).isEqualTo("Test Intervention")
      assertThat(it.interventionType).isEqualTo(InterventionType.ACP)
      assertThat(it.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(it.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      assertThat(it.eventId).isEqualTo("LIC-12345")
      assertThat(it.sex).isEqualTo("Male")
      assertThat(it.dateOfBirth).isEqualTo(LocalDate.parse("2000-10-01"))
      assertThat(it.referralLdcHistories.first().hasLdc).isFalse()
      assertThat(it.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(it.personName).isEqualTo("John Alex Doe")
      assertThat(it.referralReportingLocation!!.reportingTeam).isEqualTo("TEAM_1")
      assertThat(it.referralReportingLocation!!.pduName).isEqualTo("PDU_1")
      assertThat(it.sentenceEndDate).isEqualTo(LocalDate.parse("2025-10-01"))
      true
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

    oasysApiStubs.stubNotFoundPniResponse(crn)
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull()
    } matches {
      assertThat(it).isNotNull()
      assertThat(it!!.setting).isEqualTo(SettingType.COMMUNITY)
      assertThat(it.crn).isEqualTo(crn)
      assertThat(it.interventionName).isEqualTo("Test Intervention")
      assertThat(it.interventionType).isEqualTo(InterventionType.ACP)
      assertThat(it.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting assessment")
      assertThat(it.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      assertThat(it.eventId).isEqualTo("LIC-12345")
      assertThat(it.sex).isEqualTo("Male")
      assertThat(it.dateOfBirth).isEqualTo(LocalDate.parse("2000-10-01"))
      assertThat(it.referralLdcHistories.first().hasLdc).isFalse()
      assertThat(it.referralLdcHistories.first().createdBy).isEqualTo("SYSTEM")
      assertThat(it.personName).isEqualTo("John Alex Doe")
      assertThat(it.referralReportingLocation!!.reportingTeam).isEqualTo("TEAM_1")
      assertThat(it.referralReportingLocation!!.pduName).isEqualTo("PDU_1")
      assertThat(it.sentenceEndDate).isEqualTo(LocalDate.parse("2025-10-01"))
      true
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
    await withPollDelay ofMillis(100) untilCallTo {
      try {
        throw IllegalStateException("Unexpected event type received: unknown.event.type.created")
      } catch (e: IllegalStateException) {
        e.message
      }
    } matches { it == "Unexpected event type received: unknown.event.type.created" }
  }

  @Test
  fun `should not duplicate referral when eventId, CRN and sourcedFrom are identical to existing referral`() {
    // Given
    val findAndReferReferralDetails = FindAndReferReferralDetailsFactory()
      .withReferralId(sourceReferralId)
      .withPersonReference(crn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.LICENCE_CONDITION)
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
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$sourceReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    // Send identical event twice
    repeat(2) { domainEventsQueueConfig.sendDomainEvent(domainEventsMessage) }

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findByCrn(crn)
    } matches { it?.size == 1 }

    await untilCallTo {
      messageHistoryRepository.findAll()
    } matches { it?.size == 2 }
  }

  @Test
  fun `should insert referral when eventId and CRN are identical but sourcedFrom is different to existing referral`() {
    // Given
    val farLicenceConditionReferralId = UUID.randomUUID()
    val farRequirementReferralId = UUID.randomUUID()
    val farLicenceConditionDetails = FindAndReferReferralDetailsFactory()
      .withReferralId(farLicenceConditionReferralId)
      .withPersonReference(crn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventNumber(eventNumber)
      .produce()
    val farRequirementDetails = FindAndReferReferralDetailsFactory()
      .withReferralId(farRequirementReferralId)
      .withPersonReference(crn)
      .withPersonReferenceType(PersonReferenceType.CRN)
      .withSourcedFromReferenceType(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventNumber(eventNumber)
      .produce()

    wiremock.stubFor(
      get(urlEqualTo("/referral/$farLicenceConditionReferralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(farLicenceConditionDetails)),
        ),
    )
    wiremock.stubFor(
      get(urlEqualTo("/referral/$farRequirementReferralId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(farRequirementDetails)),
        ),
    )
    val licenceConditionMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$farLicenceConditionReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    val requirementMessage = DomainEventsMessageFactory()
      .withDetailUrl("http://find-and-refer/referral/$farRequirementReferralId")
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", crn))))
      .produce()

    // When
    // Send separate events with same eventId but different sourcedFrom types
    domainEventsQueueConfig.sendDomainEvent(licenceConditionMessage)
    domainEventsQueueConfig.sendDomainEvent(requirementMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findByCrn(crn)
    } matches { it?.size == 2 }

    await untilCallTo {
      messageHistoryRepository.findAll()
    } matches { it?.size == 2 }
  }

  @Test
  fun `should update referral personal details on receipt of community referral imported message`() {
    // Given
    val createdAt = LocalDateTime.now()
    val referralEntity = ReferralEntityFactory()
      .withCreatedAt(createdAt)
      .produce()

    val statusHistory = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
      .produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
    val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

    testDataGenerator.createReferralWithFields(
      referralEntity,
      listOf(statusHistory, cohortHistory),
    )

    val secondStatus = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.of(2025, 9, 24, 16, 0))
      .produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )

    testDataGenerator.createReferralStatusHistory(secondStatus)

    val groupCode = "AAA111"
    val group = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(group)
    val groupMembership =
      ProgrammeGroupMembershipFactory().withReferral(referralEntity).withProgrammeGroup(group).produce()
    testDataGenerator.createGroupMembership(groupMembership)
    val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
    val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

    nDeliusApiStubs.stubAccessCheck(granted = true, savedReferral.crn)
    nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(savedReferral.crn, savedReferral.eventNumber)

    oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

    val eventType = "interventions.community-referral.imported"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(mapOf("REFERRAL_ID" to savedReferral.id.toString()))
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", savedReferral.crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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

    val result = referralRepository.findByCrn(referralEntity.crn).first()

    // Then
    assertThat(result.id).isEqualTo(savedReferral.id)
    assertThat(result.crn).isEqualTo(savedReferral.crn)
    assertThat(result.interventionName).isEqualTo(savedReferral.interventionName)
    assertThat(result.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
    assertThat(result.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
    assertThat(result.sex).isEqualTo(nDeliusPersonalDetails.sex.description)
  }

  @Test
  fun `should update referral CRN on receipt of probation case merge completed message`() {
    // Given
    val savedReferral = testReferralHelper.createReferral()
    val eventType = "probation-case.merge.completed"
    val newCrn = "X777879"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(
        mapOf(
          "sourceCRN" to savedReferral.crn,
          "targetCRN" to newCrn,
        ),
      )
      .withEventType(eventType)
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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

    val result = referralRepository.findByCrn(newCrn)

    // Then
    assertThat(result).isNotEmpty()
    assertThat(result.size).isEqualTo(1)
  }

  @Test
  fun `should update referral CRN on receipt of probation case unmerge completed message`() {
    // Given
    val savedReferral = testReferralHelper.createReferral()
    val eventType = "probation-case.unmerge.completed"
    val reactivatedCrn = "X776371"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(
        mapOf(
          "unmergedCRN" to savedReferral.crn,
          "reactivatedCRN" to reactivatedCrn,
        ),
      )
      .withEventType(eventType)
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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

    val result = referralRepository.findByCrn(reactivatedCrn)

    // Then
    assertThat(result).isNotEmpty()
    assertThat(result.size).isEqualTo(1)
  }

  @Test
  fun `should update referral personal details on receipt of ACP M&D referral details updated message`() {
    // Given
    val createdAt = LocalDateTime.now()
    val referralEntity = ReferralEntityFactory()
      .withCreatedAt(createdAt)
      .produce()

    val statusHistory = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
      .produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
    val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

    testDataGenerator.createReferralWithFields(
      referralEntity,
      listOf(statusHistory, cohortHistory),
    )

    val secondStatus = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.of(2025, 9, 24, 16, 0))
      .produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription(),
      )

    testDataGenerator.createReferralStatusHistory(secondStatus)

    val groupCode = "AAA111"
    val group = ProgrammeGroupFactory().withCode(groupCode).produce()
    testDataGenerator.createGroup(group)
    val groupMembership =
      ProgrammeGroupMembershipFactory().withReferral(referralEntity).withProgrammeGroup(group).produce()
    testDataGenerator.createGroupMembership(groupMembership)
    val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]
    val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

    nDeliusApiStubs.stubAccessCheck(granted = true, savedReferral.crn)
    nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(savedReferral.crn, savedReferral.eventNumber)

    oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

    val eventType = "accredited-programmes-manage-and-deliver.referral.details-updated"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withAdditionalInformation(mapOf("referralId" to savedReferral.id.toString()))
      .withEventType(eventType)
      .withPersonReference(PersonReference(listOf(PersonReference.Identifier("CRN", savedReferral.crn))))
      .produce()

    // When
    domainEventsQueueConfig.sendDomainEvent(domainEventsMessage)

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
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

    val result = referralRepository.findByCrn(referralEntity.crn).first()

    // Then
    assertThat(result.id).isEqualTo(savedReferral.id)
    assertThat(result.crn).isEqualTo(savedReferral.crn)
    assertThat(result.interventionName).isEqualTo(savedReferral.interventionName)
    assertThat(result.personName).isEqualTo(nDeliusPersonalDetails.name.getNameAsString())
    assertThat(result.dateOfBirth).isEqualTo(nDeliusPersonalDetails.dateOfBirth)
    assertThat(result.sex).isEqualTo(nDeliusPersonalDetails.sex.description)
  }
}
