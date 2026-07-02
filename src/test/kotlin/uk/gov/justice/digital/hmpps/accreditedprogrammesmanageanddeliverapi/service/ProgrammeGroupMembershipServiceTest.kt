package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.listener.ReferralStatusUpdateEvent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.RemoveFromGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.Clock
import java.util.UUID

class ProgrammeGroupMembershipServiceTest {
  private val programmeGroupRepository = mockk<ProgrammeGroupRepository>()
  private val referralRepository = mockk<ReferralRepository>()
  private val referralStatusDescriptionRepository = mockk<ReferralStatusDescriptionRepository>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val nDeliusIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private val telemetryClient = mockk<TelemetryClient>()
  private val applicationEventPublisher = mockk<ApplicationEventPublisher>()
  private lateinit var service: ProgrammeGroupMembershipService

  @BeforeEach
  fun beforeEach() {
    service = ProgrammeGroupMembershipService(
      programmeGroupRepository = programmeGroupRepository,
      referralRepository = referralRepository,
      referralStatusDescriptionRepository = referralStatusDescriptionRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      scheduleService = scheduleService,
      nDeliusIntegrationApiClient = nDeliusIntegrationApiClient,
      telemetryClient = telemetryClient,
      applicationEventPublisher = applicationEventPublisher,
      clock = Clock.systemDefaultZone(),
    )
  }

  /**
   * Sets up common mocks for allocateReferralToGroup tests.
   * Returns a Triple of (referralId, groupId, referralEntity) for use in assertions.
   */
  private fun setupAllocationMocks(
    referralEntity: ReferralEntity,
    groupId: UUID = UUID.randomUUID(),
  ): Pair<UUID, UUID> {
    val referralId = referralEntity.id!!
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    return referralId to groupId
  }

  @Test
  fun `should allocate referral to group`() {
    // Given
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(UUID.randomUUID())
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .produce()
    val (referralId, groupId) = setupAllocationMocks(referralEntity)

    every { referralRepository.save(referralEntity) } returns referralEntity
    every { nDeliusIntegrationApiClient.getRequirementManagerDetails(any(), any()) } returns ClientResult.Success(
      HttpStatusCode.valueOf(200),
      mockk<NDeliusCaseRequirementOrLicenceConditionResponse>(),
    )
    every { scheduleService.createNdeliusAppointmentsForSessions(any()) } returns Unit
    every { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) } returns Unit

    // When
    val result = service.allocateReferralToGroup(referralId, groupId, "testAdmin", "test additional details")

    // Then
    assertThat(result).isNotNull()
    assertThat(result).isEqualTo(referralEntity)
    verify { nDeliusIntegrationApiClient.getRequirementManagerDetails(referralEntity.crn, referralEntity.eventId!!) }
    verify { referralRepository.save(referralEntity) }
    verify { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) }
  }

  @Test
  fun `should throw ConflictException when referral licence condition does not exist in nDelius`() {
    // Given
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(UUID.randomUUID())
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("1503834986")
      .produce()
    val (referralId, groupId) = setupAllocationMocks(referralEntity)

    every { nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(any(), any()) } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/case/${referralEntity.crn}/licence-conditions/1503834986",
      status = HttpStatusCode.valueOf(404),
      body = "Licence condition not found",
    )

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, "testAdmin", "test additional details")
    }
      .isInstanceOf(ConflictException::class.java)
      .hasMessageContaining("no longer exists in nDelius")
      .hasMessageContaining("licence condition")
      .hasMessageContaining("Please contact your admin")

    verify(exactly = 0) { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify(exactly = 0) { referralRepository.save(any()) }
    verify { telemetryClient.logToAppInsights("Referral.allocate-to-group.ndelius-stale-sentence-data", any()) }
  }

  @Test
  fun `should throw ConflictException when referral requirement does not exist in nDelius`() {
    // Given
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(UUID.randomUUID())
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("1503604735")
      .produce()
    val (referralId, groupId) = setupAllocationMocks(referralEntity)

    every { nDeliusIntegrationApiClient.getRequirementManagerDetails(any(), any()) } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/case/${referralEntity.crn}/requirement/1503604735",
      status = HttpStatusCode.valueOf(404),
      body = "Requirement not found",
    )

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, "testAdmin", "test additional details")
    }
      .isInstanceOf(ConflictException::class.java)
      .hasMessageContaining("no longer exists in nDelius")
      .hasMessageContaining("requirement")
      .hasMessageContaining("Please contact your admin")

    verify(exactly = 0) { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify(exactly = 0) { referralRepository.save(any()) }
    verify { telemetryClient.logToAppInsights("Referral.allocate-to-group.ndelius-stale-sentence-data", any()) }
  }

  @Test
  fun `should throw BusinessException when referral has null sourcedFrom`() {
    // Given
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(UUID.randomUUID())
      .withSourcedFrom(null)
      .produce()
    val (referralId, groupId) = setupAllocationMocks(referralEntity)

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, "testAdmin", "test additional details")
    }
      .isInstanceOf(BusinessException::class.java)
      .hasMessageContaining("sentence source type is not set")

    verify(exactly = 0) { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify(exactly = 0) { referralRepository.save(any()) }
  }

  @Test
  fun `should remove referral from group`() {
    // Given
    val referralId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val removedFromGroupBy = "testAdmin"
    val referralStatusDescriptionId = UUID.randomUUID()
    val removeFromGroupRequest = RemoveFromGroupRequestFactory().withId(referralStatusDescriptionId).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withId(groupId).withTreatmentManager(facilitator).produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").withId(referralId).produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()

    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        referralId,
        groupId,
      )
    } returns programmeGroupMembershipEntity
    every { scheduleService.removeNDeliusAppointments(any(), any()) } returns Unit
    every { programmeGroupRepository.save(programmeGroupEntity) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findByIdOrNull(referralStatusDescriptionId) } returns referralStatusDescriptionEntity
    every { referralRepository.save(referralEntity) } returns referralEntity
    every { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) } returns Unit
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.removeReferralFromGroup(referralId, groupId, removedFromGroupBy, removeFromGroupRequest)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.message).isEqualTo("John Smith was removed from this group. Their referral status is now ${referralStatusDescriptionEntity.description}.")
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referralId, groupId) }
    verify { scheduleService.removeNDeliusAppointments(any(), any()) }
    verify { programmeGroupRepository.save(programmeGroupEntity) }
    verify { referralStatusDescriptionRepository.findByIdOrNull(referralStatusDescriptionId) }
    verify { referralRepository.save(referralEntity) }
    verify { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }
}
