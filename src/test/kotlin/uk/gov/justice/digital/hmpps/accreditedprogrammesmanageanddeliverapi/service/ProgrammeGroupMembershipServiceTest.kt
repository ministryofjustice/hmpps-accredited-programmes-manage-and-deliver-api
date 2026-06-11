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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
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
    )
  }

  @Test
  fun `should allocate referral to group`() {
    // Given
    val referralId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val allocatedToGroupBy = "testAdmin"
    val additionalDetails = "test additional details"
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity
    every { referralRepository.save(referralEntity) } returns referralEntity
    every { nDeliusIntegrationApiClient.getRequirementManagerDetails(any(), any()) } returns ClientResult.Success(
      HttpStatusCode.valueOf(200),
      mockk<NDeliusCaseRequirementOrLicenceConditionResponse>(),
    )
    every { scheduleService.createNdeliusAppointmentsForSessions(any()) } returns Unit
    every { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) } returns Unit
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When
    val result = service.allocateReferralToGroup(referralId, groupId, allocatedToGroupBy, additionalDetails)

    // Then
    assertThat(result).isNotNull()
    assertThat(result).isEqualTo(referralEntity)
    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupRepository.findByIdOrNull(groupId) }
    verify { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) }
    verify { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) }
    verify { referralStatusDescriptionRepository.getScheduledStatusDescription() }
    verify { nDeliusIntegrationApiClient.getRequirementManagerDetails(referralEntity.crn, referralEntity.eventId!!) }
    verify { referralRepository.save(referralEntity) }
    verify { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
  }

  @Test
  fun `should throw BusinessException when referral licence condition does not exist in nDelius`() {
    // Given
    val referralId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val allocatedToGroupBy = "testAdmin"
    val additionalDetails = "test additional details"
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      .withEventId("1503834986")
      .produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity
    every { nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(any(), any()) } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/case/${referralEntity.crn}/licence-conditions/1503834986",
      status = HttpStatusCode.valueOf(404),
      body = "Licence condition not found",
    )
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, allocatedToGroupBy, additionalDetails)
    }
      .isInstanceOf(BusinessException::class.java)
      .hasMessageContaining("no longer exists in nDelius")
      .hasMessageContaining("licence condition")
      .hasMessageContaining("1503834986")

    // nDelius appointments should NOT have been attempted
    verify(exactly = 0) { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    // Referral should NOT have been saved
    verify(exactly = 0) { referralRepository.save(any()) }
    // Telemetry for validation failure should have been emitted
    verify { telemetryClient.logToAppInsights("Referral.allocate-to-group.ndelius-validation-failure", any()) }
  }

  @Test
  fun `should throw BusinessException when referral requirement does not exist in nDelius`() {
    // Given
    val referralId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val allocatedToGroupBy = "testAdmin"
    val additionalDetails = "test additional details"
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(referralId)
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("1503604735")
      .produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity
    every { nDeliusIntegrationApiClient.getRequirementManagerDetails(any(), any()) } returns ClientResult.Failure.StatusCode(
      method = HttpMethod.GET,
      path = "/case/${referralEntity.crn}/requirement/1503604735",
      status = HttpStatusCode.valueOf(404),
      body = "Requirement not found",
    )
    every { telemetryClient.logToAppInsights(any(), any()) } returns Unit

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, allocatedToGroupBy, additionalDetails)
    }
      .isInstanceOf(BusinessException::class.java)
      .hasMessageContaining("no longer exists in nDelius")
      .hasMessageContaining("requirement")
      .hasMessageContaining("1503604735")

    verify(exactly = 0) { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify(exactly = 0) { referralRepository.save(any()) }
    verify { telemetryClient.logToAppInsights("Referral.allocate-to-group.ndelius-validation-failure", any()) }
  }

  @Test
  fun `should throw BusinessException when referral has null sourcedFrom`() {
    // Given
    val referralId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val allocatedToGroupBy = "testAdmin"
    val additionalDetails = "test additional details"
    val referralEntity = ReferralEntityFactory()
      .withPersonName("John Smith")
      .withId(referralId)
      .withSourcedFrom(null)
      .produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val referralStatusDescriptionEntity = ReferralStatusDescriptionEntityFactory().produce()
    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every { programmeGroupRepository.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity

    // When / Then
    assertThatThrownBy {
      service.allocateReferralToGroup(referralId, groupId, allocatedToGroupBy, additionalDetails)
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
    assertThat(result.message).isEqualTo("John Smith was removed from this group. Their referral status is now ${referralStatusDescriptionEntity.description}")
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
