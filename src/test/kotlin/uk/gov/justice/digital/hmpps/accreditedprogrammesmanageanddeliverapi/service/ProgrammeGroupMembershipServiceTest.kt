package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.springframework.data.repository.findByIdOrNull
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
  private val programmeGroupRepositoryImpl = mockk<ProgrammeGroupRepository>()
  private val referralRepository = mockk<ReferralRepository>()
  private val referralStatusDescriptionRepository = mockk<ReferralStatusDescriptionRepository>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val domainEventService = mockk<DomainEventService>()
  private lateinit var service: ProgrammeGroupMembershipService

  @BeforeEach
  fun beforeEach() {
    service = ProgrammeGroupMembershipService(
      programmeGroupRepositoryImpl = programmeGroupRepositoryImpl,
      referralRepository = referralRepository,
      referralStatusDescriptionRepository = referralStatusDescriptionRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      scheduleService = scheduleService,
      domainEventService = domainEventService,
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
    every { programmeGroupRepositoryImpl.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) } returns referralStatusDescriptionEntity
    every { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) } returns null andThen programmeGroupMembershipEntity
    every { referralStatusDescriptionRepository.getScheduledStatusDescription() } returns referralStatusDescriptionEntity
    every { referralRepository.save(referralEntity) } returns referralEntity
    every { scheduleService.createNdeliusAppointmentsForSessions(anyList()) } returns Unit
    every { domainEventService.publishReferralStatusUpdatedEvent(referralEntity) } returns Unit

    // When
    val result = service.allocateReferralToGroup(referralId, groupId, allocatedToGroupBy, additionalDetails)

    // Then
    assertThat(result).isNotNull()
    assertThat(result).isEqualTo(referralEntity)
    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupRepositoryImpl.findByIdOrNull(groupId) }
    verify { referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId) }
    verify { programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId) }
    verify { referralStatusDescriptionRepository.getScheduledStatusDescription() }
    verify { referralRepository.save(referralEntity) }
    verify { scheduleService.createNdeliusAppointmentsForSessions(any()) }
    verify { domainEventService.publishReferralStatusUpdatedEvent(referralEntity) }
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

    every { programmeGroupRepositoryImpl.findByIdOrNull(groupId) } returns programmeGroupEntity
    every { referralRepository.findByIdOrNull(referralId) } returns referralEntity
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        referralId,
        groupId,
      )
    } returns programmeGroupMembershipEntity
    every { scheduleService.removeNDeliusAppointments(anyList(), anyList()) } returns Unit
    every { programmeGroupRepositoryImpl.save(programmeGroupEntity) } returns programmeGroupEntity
    every { referralStatusDescriptionRepository.findByIdOrNull(referralStatusDescriptionId) } returns referralStatusDescriptionEntity
    every { referralRepository.save(referralEntity) } returns referralEntity
    every { domainEventService.publishReferralStatusUpdatedEvent(referralEntity) } returns Unit

    // When
    val result = service.removeReferralFromGroup(referralId, groupId, removedFromGroupBy, removeFromGroupRequest)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.message).isEqualTo("John Smith was removed from this group. Their referral status is now ${referralStatusDescriptionEntity.description}")
    verify { programmeGroupRepositoryImpl.findByIdOrNull(groupId) }
    verify { referralRepository.findByIdOrNull(referralId) }
    verify { programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referralId, groupId) }
    verify { scheduleService.removeNDeliusAppointments(anyList(), anyList()) }
    verify { programmeGroupRepositoryImpl.save(programmeGroupEntity) }
    verify { referralStatusDescriptionRepository.findByIdOrNull(referralStatusDescriptionId) }
    verify { referralRepository.save(referralEntity) }
    verify { domainEventService.publishReferralStatusUpdatedEvent(referralEntity) }
  }
}
