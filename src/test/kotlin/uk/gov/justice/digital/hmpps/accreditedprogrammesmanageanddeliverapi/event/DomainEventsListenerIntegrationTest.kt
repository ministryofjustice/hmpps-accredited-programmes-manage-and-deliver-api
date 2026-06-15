package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
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

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

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
  fun `should create message history on receipt of community-referral imported message`() {
    // Given
    val eventType = "interventions.community-referral.imported"
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
    await withPollDelay ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      }
    } matches { it == 0 }

    await withPollDelay ofMillis(100) untilCallTo {
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
      it.referralReportingLocation!!.reportingTeam == "TEAM_1"
      it.referralReportingLocation!!.pduName == "PDU_1"
      it.referralReportingLocation!!.regionName == "REGION_1"
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
    await withPollDelay ofMillis(100) untilCallTo {
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
      it.referralReportingLocation!!.reportingTeam == "TEAM_1"
      it.referralReportingLocation!!.pduName == "PDU_1"
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
    await withPollDelay ofMillis(100) untilCallTo {
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
      it.referralReportingLocation!!.reportingTeam == "TEAM_1"
      it.referralReportingLocation!!.pduName == "PDU_1"
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
    await withPollDelay ofMillis(100) untilCallTo {
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
      it.referralReportingLocation!!.reportingTeam == "TEAM_1"
      it.referralReportingLocation!!.pduName == "PDU_1"
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
    await withPollDelay ofMillis(100) untilCallTo {
      try {
        throw IllegalStateException("Unexpected event type received: unknown.event.type.created")
      } catch (e: IllegalStateException) {
        e.message
      }
    } matches { it == "Unexpected event type received: unknown.event.type.created" }
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

    println("savedReferral personName: ${savedReferral.personName}")

    val nDeliusPersonalDetails = NDeliusPersonalDetailsFactory().produce()

    println("nDeliusPersonalDetails name: ${nDeliusPersonalDetails.name}")

    nDeliusApiStubs.stubAccessCheck(granted = true, savedReferral.crn)
    nDeliusApiStubs.stubPersonalDetailsResponse(nDeliusPersonalDetails)
    nDeliusApiStubs.stubSuccessfulSentenceInformationResponse(savedReferral.crn, savedReferral.eventNumber)

    oasysApiStubs.stubSuccessfulPniResponse(referralEntity.crn)

    val username = "AUTH_ADM"
    val accessCheck = LimitedAccessOffenderCheck(
      crn = referralEntity.crn,
      userExcluded = false,
      userRestricted = false,
      exclusionMessage = null,
      restrictionMessage = null,
    )

    wiremock.stubFor(
      post(urlEqualTo("/user/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(referralEntity.crn))))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                LimitedAccessOffenderCheckResponse(listOf(accessCheck)),
              ),
            ),
        ),
    )

    val eventType = "interventions.community-referral.imported"
    val domainEventsMessage = DomainEventsMessageFactory()
      .withDetailUrl("/referral/${savedReferral.id}")
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
