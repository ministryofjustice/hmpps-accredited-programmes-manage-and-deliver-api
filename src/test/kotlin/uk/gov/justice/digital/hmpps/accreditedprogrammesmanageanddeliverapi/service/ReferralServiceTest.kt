package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.listener.ReferralStatusUpdateEvent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.CreateReferralStatusHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusCaseRequirementOrLicenceConditionResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralSentenceReferenceRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusTransitionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UserFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.UPDATE_REFERRAL_SENTENCE_REFERENCE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.UPDATE_REFERRAL_STATUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.VIEW_REFERRAL
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCohortHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.time.LocalDateTime
import java.util.UUID

class ReferralServiceTest {
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient = mockk()
  private val findAndReferInterventionApiClient: FindAndReferInterventionApiClient = mockk()
  private val referralRepository: ReferralRepository = mockk()
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository = mockk()
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository = mockk()
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository = mockk()
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository = mockk()
  private val userService: UserService = mockk()
  private val ldcService: LdcService = mockk()
  private val cohortService: CohortService = mockk()
  private val pniService: PniService = mockk()
  private val referralLdcHistoryRepository: ReferralLdcHistoryRepository = mockk()
  private val referralReportingLocationRepository: ReferralReportingLocationRepository = mockk()
  private val sentenceService: SentenceService = mockk()
  private val referralStatusService: ReferralStatusService = mockk()
  private val programmeGroupMembershipService: ProgrammeGroupMembershipService = mockk()
  private val programmeGroupService: ProgrammeGroupService = mockk()
  private val applicationEventPublisher: ApplicationEventPublisher = mockk()
  private val sessionNameFormatter: SessionNameFormatter = mockk()
  private val referralCohortHistoryRepository: ReferralCohortHistoryRepository = mockk()
  private val referralEventNumberResolverService: ReferralEventNumberResolverService = mockk()
  private val telemetryService: TelemetryService = mockk()
  private val probationAccessControlApiClient: ProbationAccessControlApiClient = mockk()
  private val sessionRepository: SessionRepository = mockk()

  private lateinit var referralService: ReferralService

  @BeforeEach
  fun beforeEach() {
    referralService = ReferralService(
      findAndReferInterventionApiClient = findAndReferInterventionApiClient,
      nDeliusIntegrationApiClient = nDeliusIntegrationApiClient,
      referralRepository = referralRepository,
      referralStatusDescriptionRepository = referralStatusDescriptionRepository,
      referralStatusTransitionRepository = referralStatusTransitionRepository,
      userService = userService,
      cohortService = cohortService,
      pniService = pniService,
      referralStatusHistoryRepository = referralStatusHistoryRepository,
      referralLdcHistoryRepository = referralLdcHistoryRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      ldcService = ldcService,
      referralReportingLocationRepository = referralReportingLocationRepository,
      sentenceService = sentenceService,
      programmeGroupMembershipService = programmeGroupMembershipService,
      programmeGroupService = programmeGroupService,
      sessionNameFormatter = sessionNameFormatter,
      referralStatusService = referralStatusService,
      referralCohortHistoryRepository = referralCohortHistoryRepository,
      telemetryService = telemetryService,
      referralEventNumberResolverService = referralEventNumberResolverService,
      applicationEventPublisher = applicationEventPublisher,
      probationAccessControlApiClient = probationAccessControlApiClient,
      sessionRepository = sessionRepository,
      laoAccessCheckEnabled = true,
    )
  }

  @Test
  fun `getFindAndReferReferralDetails should return referral details when present`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralDetails = FindAndReferReferralDetailsFactory().withReferralId(referralId).produce()

    every { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = referralDetails,
    )

    // When
    val result = referralService.getFindAndReferReferralDetails(referralId)

    // Then
    assertThat(referralDetails).isEqualTo(result)
    verify { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) }
  }

  @Test
  fun `getFindAndReferReferralDetails should throw NotFoundException when referral is not found`() {
    // Given
    val referralId = UUID.randomUUID()
    every { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/referral/$referralId",
      HttpStatusCode.valueOf(404),
      "",
    )

    // When & Then
    assertThrows<NotFoundException> { referralService.getFindAndReferReferralDetails(referralId) }
    verify { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) }
  }

  @Test
  fun `getFindAndReferReferralDetails should not throw NotFoundException when the call fails for a non-404 reason`() {
    // Given
    val referralId = UUID.randomUUID()
    every { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/referral/$referralId",
      HttpStatusCode.valueOf(400),
      "Bad request",
    )

    // When & Then
    assertThrows<BusinessException> { referralService.getFindAndReferReferralDetails(referralId) }
    verify { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) }
  }

  @Test
  fun `getFindAndReferReferralDetails should throw BusinessException when a non-HTTP failure occurs`() {
    val referralId = UUID.randomUUID()
    every { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) } returns ClientResult.Failure.Other(
      HttpMethod.GET,
      "/referral/$referralId",
      RuntimeException("timeout"),
      "find-and-refer",
    )

    assertThrows<BusinessException> { referralService.getFindAndReferReferralDetails(referralId) }
    verify { findAndReferInterventionApiClient.getFindAndReferReferral(referralId) }
  }

  @Test
  fun `getReferralById should return referral when it exists`() {
    // Given
    val referralEntity = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    every { referralRepository.findByIdOrNull(referralEntity.id!!) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When
    val result = referralService.getReferralById(referralEntity.id!!)

    // Then
    assertThat(result).isEqualTo(referralEntity)
    verify { referralRepository.findByIdOrNull(referralEntity.id!!) }
    verify {
      telemetryService.logToAppInsights(
        referralEntity = referralEntity,
        eventName = "Referral.get.success",
        activityType = VIEW_REFERRAL.name,
        toReferralStatusId = null,
        appliedBy = null,
      )
    }
  }

  @Test
  fun `getReferralById should throw NotFoundException when referral does not exist`() {
    // Given
    val referralId = UUID.randomUUID()
    every { referralRepository.findByIdOrNull(referralId) } returns null

    // When & Then
    val exception = assertThrows<NotFoundException> { referralService.getReferralById(referralId) }
    assertThat(exception.message).isEqualTo("No Referral found for id: $referralId")
    verify { referralRepository.findByIdOrNull(referralId) }
    verify(exactly = 0) { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) }
  }

  @Test
  fun `attemptToFindManagerForReferral should return manager when referral exists and is sourced from requirement`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("EVENT123")
      .withCrn("CRN123")
      .produce()

    val managerResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit
    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(referralEntity.crn, referralEntity.eventId!!)
    } returns ClientResult.Success(status = HttpStatusCode.valueOf(200), body = managerResponse)

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(managerResponse.manager)
    verify {
      telemetryService.logToAppInsights(
        "RequirementManagerDetails.get-nDelius.success",
        "GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `attemptToFindManagerForReferral should return null when manager response is null`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("EVENT123")
      .withCrn("CRN123")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit
    every {
      nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(referralEntity.crn, referralEntity.eventId!!)
    } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/case/${referralEntity.crn}/licence-condition/${referralEntity.eventId}",
      status = HttpStatusCode.valueOf(404),
      body = "Not Found",
    )

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isNull()
  }

  @Test
  fun `getReferralAndEnsureSourcedFrom should throw NotFoundException when eventId is null`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .produce()
    referralEntity.eventId = null

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // When & Then
    val exception = assertThrows<NotFoundException> {
      referralService.attemptToFindManagerForReferral(referralId)
    }
    assertThat(exception.message).isEqualTo("Referral with id: $referralId exists, but has no eventId")
  }

  @Test
  fun `getReferralAndEnsureSourcedFrom should return immediately when sourcedFrom is already set`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("12345")
      .produce()

    val managerResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit
    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = managerResponse,
    )

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(managerResponse.manager)
    verify(exactly = 0) { referralRepository.save(any()) }
  }

  @Test
  fun `getReferralAndEnsureSourcedFrom should find as REQUIREMENT when not set`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(null)
      .withEventId("12345")
      .produce()

    val managerResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { referralRepository.save(any()) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit
    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = managerResponse,
    )

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(managerResponse.manager)
    assertThat(referralEntity.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.REQUIREMENT)
    verify(exactly = 1) { referralRepository.save(referralEntity) }
    verify {
      telemetryService.logToAppInsights(
        "${GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.eventName}.success",
        GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.name,
        "success",
      )
    }
  }

  @Test
  fun `getReferralAndEnsureSourcedFrom should find as LICENCE_CONDITION when REQUIREMENT fails`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(null)
      .withEventId("12345")
      .produce()

    val managerResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { referralRepository.save(any()) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/req",
      HttpStatusCode.valueOf(404),
      "",
    )
    every {
      nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = managerResponse,
    )

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(managerResponse.manager)
    assertThat(referralEntity.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
    verify(exactly = 1) { referralRepository.save(referralEntity) }
    verify {
      telemetryService.logToAppInsights(
        "LicenceConditionManagerDetails.get-nDelius.success",
        "GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getReferralAndEnsureSourcedFrom should throw NotFoundException when both fail`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(null)
      .withEventId("12345")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/req",
      HttpStatusCode.valueOf(404),
      "",
    )
    every {
      nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/lic",
      HttpStatusCode.valueOf(404),
      "",
    )

    // When & Then
    val exception = assertThrows<NotFoundException> {
      referralService.attemptToFindManagerForReferral(referralId)
    }
    assertThat(exception.message).isEqualTo("No LicenceCondition or Requirement found with id 12345")

    verify {
      telemetryService.logToAppInsights(
        "LicenceConditionManagerDetails.get-nDelius.failure",
        "GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS",
        "failure",
      )
    }
  }

  @Test
  fun `updateStatus should update referral status and return response`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val createReferralStatusHistory = CreateReferralStatusHistoryFactory().produce()
    val createdBy = "test-user"
    val user = UserFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit

    // Mock the overloaded updateStatus call
    val incomingStatusDescription = ReferralStatusDescriptionEntityFactory().produce()
    every { referralStatusDescriptionRepository.findByIdOrNull(any()) } returns incomingStatusDescription
    val currentStatusHistory = ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referralEntity, ReferralStatusDescriptionEntityFactory().produce())
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentStatusHistory
    val transition = ReferralStatusTransitionEntityFactory().produce()
    every { referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(any(), any()) } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(any()) } returns null
    every { referralStatusHistoryRepository.save(any()) } returns ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referralEntity, incomingStatusDescription)
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    referralService.updateStatus(referralId, createReferralStatusHistory, createdBy)

    // Then
    verify { referralRepository.findByIdOrNull(referralId) }
    verify {
      telemetryService.logToAppInsights(
        referralEntity,
        "Referral.admin-update-status.success",
        UPDATE_REFERRAL_STATUS.name,
        createReferralStatusHistory.referralStatusDescriptionId,
        createdBy,
      )
    }
    verify { userService.getUserByUsernameOrNull(createdBy) }
  }

  @Test
  fun `updateStatus should throw NotFoundException when status description does not exist`() {
    // Given
    val referral = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val statusDescriptionId = UUID.randomUUID()

    every { referralStatusDescriptionRepository.findByIdOrNull(statusDescriptionId) } returns null

    // When & Then
    assertThrows<NotFoundException> {
      referralService.updateStatus(referral, statusDescriptionId, createdBy = "user")
    }
  }

  @Test
  fun `updateStatus should throw BusinessException when current status history does not exist`() {
    // Given
    val referral = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val statusDescription = ReferralStatusDescriptionEntityFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(statusDescription.id) } returns statusDescription
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referral.id!!) } returns null

    // When & Then
    assertThrows<BusinessException> {
      referralService.updateStatus(referral, statusDescription.id, createdBy = "user")
    }
  }

  @Test
  fun `updateStatus should throw BusinessException when transition is invalid`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status A").produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status B").produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns null

    // When & Then
    val exception = assertThrows<BusinessException> {
      referralService.updateStatus(referral, incomingStatus.id, createdBy = "user")
    }
    assertThat(exception.message).contains("Invalid referral status transition")
  }

  @Test
  fun `updateStatus should remove from group when transition is not continuing`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).withPersonName("John Doe").produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status A").produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().withDescription("Withdrawn").produce()
    val transition = ReferralStatusTransitionEntityFactory()
      .withFromStatus(currentStatus)
      .withToStatus(incomingStatus)
      .withIsContinuing(false)
      .produce()
    val membership = ProgrammeGroupMembershipFactory(referral = referral).produce()
    val user = UserFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns membership
    every {
      programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(
        any(),
        any(),
        any(),
      )
    } returns mockk()
    every { referralStatusHistoryRepository.save(any()) } returns ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, incomingStatus)
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    val response = referralService.updateStatus(referral, incomingStatus.id, createdBy = "test-user")

    // Then
    assertThat(response.message).contains("They have been removed from group")
    verify {
      programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(
        referral,
        membership.programmeGroup,
        "test-user",
      )
    }
    verify { applicationEventPublisher.publishEvent(any<ReferralStatusUpdateEvent>()) }
    verify { userService.getUserByUsernameOrNull(any()) }
  }

  @Test
  fun `updateStatus should NOT remove from group when transition is continuing`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().produce()
    val transition = ReferralStatusTransitionEntityFactory()
      .withFromStatus(currentStatus)
      .withToStatus(incomingStatus)
      .withIsContinuing(true)
      .produce()
    val membership = ProgrammeGroupMembershipFactory(referral = referral).produce()
    val user = UserFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns membership
    every { referralStatusHistoryRepository.save(any()) } returns ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, incomingStatus)
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    referralService.updateStatus(referral, incomingStatus.id, createdBy = "test-user")

    // Then
    verify(exactly = 0) {
      programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(
        any(),
        any(),
        any(),
      )
    }
    verify { userService.getUserByUsernameOrNull(any()) }
  }

  @Test
  fun `updateStatus should update referral status, publish event and log telemetry`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status A").produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status B").produce()
    val transition = ReferralStatusTransitionEntityFactory()
      .withFromStatus(currentStatus)
      .withToStatus(incomingStatus)
      .withIsContinuing(true)
      .produce()
    val additionalDetails = "Some details"
    val createdBy = "test-user"
    val user = UserFactory().withUsername(createdBy).produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns null
    val savedHistory = ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, incomingStatus)
    every { referralStatusHistoryRepository.save(any()) } returns savedHistory
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    val response = referralService.updateStatus(referral, incomingStatus.id, additionalDetails, createdBy)

    // Then
    assertThat(response.referralStatusHistory).isEqualTo(savedHistory.toApi())
    assertThat(response.message).isEqualTo("${referral.personName}'s referral status is now Status B.")

    verify {
      referralStatusHistoryRepository.save(
        withArg {
          assertThat(it.referral).isEqualTo(referral)
          assertThat(it.referralStatusDescription).isEqualTo(incomingStatus)
          assertThat(it.additionalDetails).isEqualTo(additionalDetails)
          assertThat(it.createdBy).isEqualTo(createdBy)
        },
      )
    }
    verify { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId!!)) }
    verify {
      telemetryService.logToAppInsights(
        referralEntity = referral,
        eventName = "Referral.update-status.success",
        activityType = UPDATE_REFERRAL_STATUS.name,
        toReferralStatusId = null,
        appliedBy = null,
      )
    }
    verify { userService.getUserByUsernameOrNull(createdBy) }
  }

  @Test
  fun `updateStatus should NOT throw BusinessException when from and to status are the same`() {
    // Given
    val referral = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    val referralId = referral.id!!
    val currentStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status A").produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val user = UserFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(currentStatus.id) } returns currentStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        currentStatus.id,
      )
    } returns null // No self-transition defined
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns null
    val savedHistory = ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, currentStatus)
    every { referralStatusHistoryRepository.save(any()) } returns savedHistory
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When & Then - should not throw
    referralService.updateStatus(referral, currentStatus.id, createdBy = "test-user")
  }

  @Test
  fun `updateStatus should NOT include removed from group in message when status is Programme complete`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).withPersonName("John Doe").produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().withDescription("Status A").produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().withDescription("Programme complete").produce()
    val transition = ReferralStatusTransitionEntityFactory()
      .withFromStatus(currentStatus)
      .withToStatus(incomingStatus)
      .withIsContinuing(false)
      .produce()
    val membership = ProgrammeGroupMembershipFactory(referral = referral).produce()
    val user = UserFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns membership
    every {
      programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(any(), any(), any())
    } returns mockk()
    val savedHistory = ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, incomingStatus)
    every { referralStatusHistoryRepository.save(any()) } returns savedHistory
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { referralStatusService.checkAndPublishCompletionEvent(referralId) } returns true
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    val response = referralService.updateStatus(referral, incomingStatus.id, createdBy = "test-user")

    // Then
    assertThat(response.message).isEqualTo("John Doe's referral status is now Programme complete.")
    verify { programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(any(), any(), any()) }
  }

  @Test
  fun `updateStatus should call referralStatusService when status is Programme complete`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val currentStatus = ReferralStatusDescriptionEntityFactory().produce()
    val currentHistory = ReferralStatusHistoryEntityFactory().produce(referral, currentStatus)
    val incomingStatus = ReferralStatusDescriptionEntityFactory().withDescription("Programme complete").produce()
    val transition = ReferralStatusTransitionEntityFactory()
      .withFromStatus(currentStatus)
      .withToStatus(incomingStatus)
      .produce()
    val user = UserFactory().produce()

    every { referralStatusDescriptionRepository.findByIdOrNull(incomingStatus.id) } returns incomingStatus
    every { referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) } returns currentHistory
    every {
      referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
        currentStatus.id,
        incomingStatus.id,
      )
    } returns transition
    every { programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral) } returns null
    every { referralStatusHistoryRepository.save(any()) } returns ReferralStatusHistoryEntityFactory()
      .withId(UUID.randomUUID())
      .produce(referral, incomingStatus)
    every { applicationEventPublisher.publishEvent(any<Any>()) } returns Unit
    every { referralStatusService.checkAndPublishCompletionEvent(referralId) } returns true
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { userService.getUserByUsernameOrNull(any()) } returns user

    // When
    referralService.updateStatus(referral, incomingStatus.id, createdBy = "test-user")

    // Then
    verify { referralStatusService.checkAndPublishCompletionEvent(referralId) }
    verify { userService.getUserByUsernameOrNull(any()) }
  }

  @Test
  fun `getPersonalDetails should return personal details when successful`() {
    // Given
    val crn = "X123456"
    val personalDetails = NDeliusPersonalDetailsFactory().produce()
    val referralDetails = FindAndReferReferralDetailsFactory().withPersonReference(crn).produce()

    every { findAndReferInterventionApiClient.getFindAndReferReferral(any()) } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = referralDetails,
    )
    every { nDeliusIntegrationApiClient.getPersonalDetails(crn) } returns ClientResult.Success(
      status = HttpStatusCode.valueOf(200),
      body = personalDetails,
    )
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    // We test this via createReferral which calls the private getPersonalDetails
    // Need to mock other things that createReferral calls
    every { referralRepository.findByCrnAndEventIdAndSourcedFrom(any(), any(), any()) } returns null
    every { pniService.getPniCalculation(crn) } returns mockk(relaxed = true)
    every { sentenceService.getSentenceEndDate(any(), any(), any()) } returns null
    every { cohortService.determineOffenceCohort(any()) } returns mockk(relaxed = true)
    val awaitingAssessmentStatusDescription = ReferralStatusDescriptionEntityFactory().produce()
    every { referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription() } returns awaitingAssessmentStatusDescription
    val savedReferral = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    every { referralRepository.save(any()) } returns savedReferral
    every { referralStatusHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralCohortHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralLdcHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralReportingLocationRepository.save(any()) } returns mockk(relaxed = true)

    // When
    referralService.createReferral(referralDetails)

    // Then
    verify { nDeliusIntegrationApiClient.getPersonalDetails(crn) }
    verify {
      telemetryService.logToAppInsights(
        "PersonalDetails.get-nDelius.success",
        "GET_PERSONAL_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getPersonalDetails should return null and log failure when call fails`() {
    // Given
    val crn = "X123456"
    val referralDetails = FindAndReferReferralDetailsFactory().withPersonReference(crn).produce()

    every { nDeliusIntegrationApiClient.getPersonalDetails(crn) } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/person/$crn",
      status = HttpStatusCode.valueOf(404),
      body = "Not Found",
    )
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    // Other mocks for createReferral
    every { referralRepository.findByCrnAndEventIdAndSourcedFrom(any(), any(), any()) } returns null
    every { pniService.getPniCalculation(crn) } returns mockk(relaxed = true)
    every { sentenceService.getSentenceEndDate(any(), any(), any()) } returns null
    every { cohortService.determineOffenceCohort(any()) } returns mockk(relaxed = true)
    val awaitingAssessmentStatusDescription = ReferralStatusDescriptionEntityFactory().produce()
    every { referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription() } returns awaitingAssessmentStatusDescription
    val savedReferral = ReferralEntityFactory().withId(UUID.randomUUID()).produce()
    every { referralRepository.save(any()) } returns savedReferral
    every { referralStatusHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralCohortHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralLdcHistoryRepository.save(any()) } returns mockk(relaxed = true)
    every { referralReportingLocationRepository.save(any()) } returns mockk(relaxed = true)

    // When
    referralService.createReferral(referralDetails)

    // Then
    verify { nDeliusIntegrationApiClient.getPersonalDetails(crn) }
    verify {
      telemetryService.logToAppInsights(
        "PersonalDetails.get-nDelius.failure",
        "GET_PERSONAL_DETAILS_N_DELIUS",
        "failure",
      )
    }
  }

  @Test
  fun `updateReferralSentenceReference should update and return success when validation passes`() {
    // Given
    val referralId = UUID.randomUUID()
    val username = "test-user"
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("OLD_EVENT")
      .produce()

    val request = ReferralSentenceReferenceRequestFactory()
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("NEW_EVENT")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupMembershipService.validateReferralSentenceDataExistsInNDelius(any(), any()) } returns Unit
    every { referralRepository.save(any()) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every {
      telemetryService.logToAppInsights(
        referralEntity = any(),
        eventName = any(),
        activityType = any(),
        fromSourcedFromName = any(),
        fromEventId = any(),
        referralSentenceReferenceRequest = any(),
        appliedBy = any(),
      )
    } returns Unit

    // When
    val result = referralService.updateReferralSentenceReference(referralId, request, username)

    // Then
    assertThat(result.message).contains("now has the sourceFrom: LICENCE_CONDITION and eventId: NEW_EVENT")
    assertThat(referralEntity.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
    assertThat(referralEntity.eventId).isEqualTo("NEW_EVENT")

    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupMembershipService.validateReferralSentenceDataExistsInNDelius(referralEntity, any()) }
    verify { referralRepository.save(referralEntity) }
    verify {
      telemetryService.logToAppInsights(
        referralEntity = referralEntity,
        eventName = "Referral.admin-repoint-sentence-reference.applied.success",
        activityType = UPDATE_REFERRAL_SENTENCE_REFERENCE.name,
        fromSourcedFromName = "REQUIREMENT",
        fromEventId = "OLD_EVENT",
        referralSentenceReferenceRequest = request,
        appliedBy = username,
      )
    }
  }

  @Test
  fun `updateReferralSentenceReference should throw NotFoundException when referral does not exist`() {
    // Given
    val referralId = UUID.randomUUID()
    val request = ReferralSentenceReferenceRequestFactory().produce()
    every { referralRepository.findByIdOrNull(referralId) } returns null

    // When & Then
    assertThrows<NotFoundException> {
      referralService.updateReferralSentenceReference(referralId, request, "username")
    }

    verify { referralRepository.findByIdOrNull(referralId) }
    verify(exactly = 0) { referralRepository.save(any()) }
  }

  @Test
  fun `updateReferralSentenceReference should throw ConflictException when validation fails`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).produce()
    val request = ReferralSentenceReferenceRequestFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every {
      programmeGroupMembershipService.validateReferralSentenceDataExistsInNDelius(any(), any())
    } throws ConflictException("Validation failed")

    // When & Then
    val exception = assertThrows<ConflictException> {
      referralService.updateReferralSentenceReference(referralId, request, "username")
    }
    assertThat(exception.message).isEqualTo("Validation failed")

    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupMembershipService.validateReferralSentenceDataExistsInNDelius(any(), any()) }
    verify(exactly = 0) { referralRepository.save(any()) }
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should return requirement when sourcedFrom is REQUIREMENT`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("12345")
      .produce()

    val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(status = HttpStatusCode.valueOf(200), body = requirementResponse)

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(requirementResponse.manager)
    verify {
      telemetryService.logToAppInsights(
        "RequirementManagerDetails.get-nDelius.success",
        "GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should throw NotFoundException when sourcedFrom is REQUIREMENT and call fails`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("12345")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/req",
      HttpStatusCode.valueOf(404),
      "",
    )

    // When & Then
    val exception = assertThrows<NotFoundException> {
      referralService.attemptToFindManagerForReferral(referralId)
    }
    assertThat(exception.message).isEqualTo("Could not fetch a Requirement with ID ${referralEntity.id}, for Referral with ID: ${referralEntity.id}")

    verify {
      telemetryService.logToAppInsights(
        "RequirementManagerDetails.get-nDelius.failure",
        "GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS",
        "failure",
      )
    }
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should return licence condition when sourcedFrom is LICENCE_CONDITION`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("12345")
      .produce()

    val licenceConditionResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(status = HttpStatusCode.valueOf(200), body = licenceConditionResponse)

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(licenceConditionResponse.manager)
    verify {
      telemetryService.logToAppInsights(
        "LicenceConditionManagerDetails.get-nDelius.success",
        "GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should return null when sourcedFrom is LICENCE_CONDITION and call fails`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("12345")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/lic",
      HttpStatusCode.valueOf(404),
      "",
    )

    // When
    val result = referralService.attemptToFindManagerForReferral(referralId)

    // Then
    assertThat(result).isNull()
    verify {
      telemetryService.logToAppInsights(
        "LicenceConditionManagerDetails.get-nDelius.failure",
        "GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS",
        "failure",
      )
    }
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should return null when sourcedFrom is null`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(null)
      .withEventId("12345")
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit
  }

  @Test
  fun `getRetRequirementOrLicenceCondition should return PDUs when sourcedFrom is REQUIREMENT`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory()
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("12345")
      .produce()

    val pdu = NDeliusCaseRequirementOrLicenceConditionResponseFactory().produce().manager.probationDeliveryUnit
    val pduWithOfficeLocations =
      uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations(
        code = pdu.code,
        description = pdu.description,
        officeLocations = emptyList(),
      )
    val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponseFactory()
      .produce()
      .copy(probationDeliveryUnits = listOf(pduWithOfficeLocations))

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { telemetryService.logToAppInsights(any(), any(), any(), any(), any()) } returns Unit
    every { telemetryService.logToAppInsights(any(), any(), any()) } returns Unit

    every {
      nDeliusIntegrationApiClient.getRequirementManagerDetails(
        referralEntity.crn,
        "12345",
      )
    } returns ClientResult.Success(status = HttpStatusCode.valueOf(200), body = requirementResponse)

    // When
    val result = referralService.attemptToFindNonPrimaryPdusForReferral(referralId)

    // Then
    assertThat(result).isEqualTo(requirementResponse.probationDeliveryUnits)
    verify {
      telemetryService.logToAppInsights(
        "RequirementManagerDetails.get-nDelius.success",
        "GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getAttendanceHistory should return sessions with 'To be confirmed' when no attendance recorded`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val group = ProgrammeGroupFactory().withId(UUID.randomUUID()).produce()

    val membership = ProgrammeGroupMembershipFactory()
      .withReferral(referral)
      .withProgrammeGroup(group)
      .produce()

    val session = SessionFactory(programmeGroup = group)
      .withId(UUID.randomUUID())
      .withStartsAt(LocalDateTime.now().minusHours(2))
      .withEndsAt(LocalDateTime.now().minusHours(1))
      .produce()
    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees = mutableListOf(attendee)
    val futureSession = SessionFactory(programmeGroup = group)
      .withId(UUID.randomUUID())
      .withStartsAt(LocalDateTime.now().plusHours(1))
      .withEndsAt(LocalDateTime.now().plusHours(2))
      .produce()
    futureSession.attendees = mutableListOf(
      AttendeeFactory().withReferral(referral).withSession(futureSession).produce(),
    )

    every { referralRepository.findByIdOrNull(referralId) } returns referral
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns membership
    every { programmeGroupMembershipRepository.findAllByReferralIdWithAttendances(referralId) } returns listOf(membership)
    every { sessionRepository.findAllByProgrammeGroupIdIn(any()) } returns listOf(session, futureSession)
    every { programmeGroupService.getAttendanceTextFromOutcome(null) } returns "To be confirmed"
    every { sessionNameFormatter.format(any(), any()) } returns "Session 1"

    // When
    val result = referralService.getAttendanceHistory(referralId)

    // Then
    assertThat(result.attendanceHistory).hasSize(1)
    assertThat(result.attendanceHistory.single().sessionId).isEqualTo(session.id)
    assertThat(result.attendanceHistory.single().attendanceStatus).isEqualTo("To be confirmed")
  }

  @Test
  fun `getAttendanceHistory should not lose an attendance recorded against an older membership when the referral was re-added to the same group`() {
    // Given
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory().withId(referralId).produce()
    val group = ProgrammeGroupFactory().withId(UUID.randomUUID()).produce()

    val session = SessionFactory(programmeGroup = group)
      .withId(UUID.randomUUID())
      .withStartsAt(LocalDateTime.now().minusHours(2))
      .withEndsAt(LocalDateTime.now().minusHours(1))
      .produce()
    session.attendees = mutableListOf(AttendeeFactory().withReferral(referral).withSession(session).produce())

    val oldMembership = ProgrammeGroupMembershipFactory()
      .withReferral(referral)
      .withProgrammeGroup(group)
      .withCreatedAt(LocalDateTime.now().minusDays(2))
      .produce()
    val attendance = SessionAttendanceEntityFactory(session, oldMembership)
      .withOutcomeType(SessionAttendanceNDeliusOutcomeEntityFactory().produce())
      .withCreatedAt(LocalDateTime.now().minusHours(1))
      .produce()
    oldMembership.attendances = mutableSetOf(attendance)

    // Referral removed and re-added to the same group after the session was attended, so this membership has no attendance recorded.
    val newMembership = ProgrammeGroupMembershipFactory()
      .withReferral(referral)
      .withProgrammeGroup(group)
      .withCreatedAt(LocalDateTime.now())
      .produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referral
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns newMembership
    // Ordered newest first, matching the repository's `ORDER BY pgm.createdAt DESC`.
    every { programmeGroupMembershipRepository.findAllByReferralIdWithAttendances(referralId) } returns listOf(newMembership, oldMembership)
    every { sessionRepository.findAllByProgrammeGroupIdIn(any()) } returns listOf(session)
    every { programmeGroupService.getAttendanceTextFromOutcome(attendance.outcomeType) } returns "Attended"
    every { sessionNameFormatter.format(any(), any()) } returns "Session 1"

    // When
    val result = referralService.getAttendanceHistory(referralId)

    // Then
    assertThat(result.attendanceHistory).hasSize(1)
    assertThat(result.attendanceHistory.single().attendanceStatus).isEqualTo("Attended")
  }
}
