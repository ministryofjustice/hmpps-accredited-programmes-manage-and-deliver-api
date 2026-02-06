package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.util.Optional
import java.util.UUID

class SessionServiceTest {
  private val sessionRepository = mockk<SessionRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()
  private val referralRepository = mockk<ReferralRepository>()
  private lateinit var service: SessionService

  @BeforeEach
  fun setup() {
    service = SessionService(
      sessionRepository,
      scheduleService,
      programmeGroupMembershipRepository,
      referralRepository,
    )
  }

  @Test
  fun `should return a delete session caption for a post-programme review session`() {
    // Given
    val groupId = UUID.randomUUID()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Post-programme reviews").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Template 1")
      .withModule(module)
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { sessionRepository.findByIdOrNull(any()) } returns sessionEntity
    every { sessionRepository.delete(any()) } returns Unit
    every { scheduleService.removeNDeliusAppointments(any(), any()) } returns Unit

    // When
    val result = service.deleteSession(groupId)

    // Then
    assertThat(result.caption).isEqualTo("John Smith: post-programme review has been deleted")
    verify { sessionRepository.findByIdOrNull(any()) }
    verify { scheduleService.removeNDeliusAppointments(any(), any()) }
    verify { sessionRepository.delete(any()) }
  }

  @Test
  fun `should return a delete session caption for an one-to-one session`() {
    // Given
    val groupId = UUID.randomUUID()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withModule(module)
      .withName("Getting started one-to-one")
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(false)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { sessionRepository.findByIdOrNull(any()) } returns sessionEntity
    every { sessionRepository.delete(any()) } returns Unit
    every { scheduleService.removeNDeliusAppointments(any(), any()) } returns Unit

    // When
    val result = service.deleteSession(groupId)

    // Then
    assertThat(result.caption).isEqualTo("John Smith: Getting started 1 one-to-one has been deleted.")
    verify { sessionRepository.findByIdOrNull(any()) }
    verify { scheduleService.removeNDeliusAppointments(any(), any()) }
    verify { sessionRepository.delete(any()) }
  }

  @Test
  fun `should return a delete session caption for a group session`() {
    // Given
    val groupId = UUID.randomUUID()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        listOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ) as MutableList<AttendeeEntity>,
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { sessionRepository.findByIdOrNull(any()) } returns sessionEntity
    every { sessionRepository.delete(any()) } returns Unit
    every { scheduleService.removeNDeliusAppointments(any(), any()) } returns Unit

    // When
    val result = service.deleteSession(groupId)

    // Then
    assertThat(result.caption).isEqualTo("Getting started 1 catch-up has been deleted.")
    verify { sessionRepository.findByIdOrNull(any()) }
    verify { scheduleService.removeNDeliusAppointments(any(), any()) }
    verify { sessionRepository.delete(any()) }
  }

  @Test
  fun `should update session attendees`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId1 = UUID.randomUUID()
    val referralId2 = UUID.randomUUID()
    val referralIds = listOf(referralId1, referralId2)

    val programmeGroup = ProgrammeGroupFactory().produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withName("Template 1").produce()
    val session = SessionFactory()
      .withProgrammeGroup(programmeGroup)
      .withModuleSessionTemplate(moduleSessionTemplate)
      .produce()
    val referral1 = ReferralEntityFactory().withId(referralId1).produce()
    val referral2 = ReferralEntityFactory().withId(referralId2).produce()

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)
    every { referralRepository.findById(referralId1) } returns Optional.of(referral1)
    every { referralRepository.findById(referralId2) } returns Optional.of(referral2)
    every { sessionRepository.save(session) } returns session

    // When
    val result = service.updateSessionAttendees(sessionId, referralIds)

    // Then
    assertThat(result).isEqualTo("The date and time have been updated.")
    assertThat(session.attendees).hasSize(2)
    assertThat(session.attendees[0].referral.id).isEqualTo(referralId1)
    assertThat(session.attendees[1].referral.id).isEqualTo(referralId2)

    verify { sessionRepository.findById(sessionId) }
    verify { referralRepository.findById(referralId1) }
    verify { referralRepository.findById(referralId2) }
    verify { sessionRepository.save(session) }
  }
}
