package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
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
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").withId(referralId).produce()
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
    verify { referralRepository.save(referralEntity) }
    verify { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify { applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId)) }
    verify { telemetryClient.logToAppInsights(any(), any()) }
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
