package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType.LEAD_FACILITATOR
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class SessionServiceTest {
  private val sessionRepository = mockk<SessionRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val programmeGroupMembershipRepository = mockk<ProgrammeGroupMembershipRepository>()
  private val referralRepository = mockk<ReferralRepository>()
  private val facilitatorService = mockk<FacilitatorService>()
  private val sessionAttendanceOutcomeTypeRepository = mockk<SessionAttendanceOutcomeTypeRepository>()
  private val nDeliusIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private lateinit var service: SessionService

  @BeforeEach
  fun setup() {
    service = SessionService(
      sessionRepository,
      scheduleService,
      programmeGroupMembershipRepository,
      facilitatorService,
      referralRepository,
      nDeliusIntegrationApiClient,
      sessionAttendanceOutcomeTypeRepository,
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
      .withName("Post-programme review")
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
    assertThat(result).isNotNull
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
    assertThat(result).isNotNull
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
    val module = ModuleEntityFactory().withName("Getting started").produce()
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
    assertThat(result).isNotNull
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

  @Test
  fun `rescheduleSessions should update nDelius appointments for single session`() {
    // Given
    val sessionId = UUID.randomUUID()
    val request = RescheduleSessionRequest(
      sessionStartDate = LocalDate.now().plusDays(1),
      sessionStartTime = SessionTime(10, 0, AmOrPm.AM),
      rescheduleOtherSessions = false,
    )

    val facilitator = FacilitatorEntityFactory().produce()
    val group = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val moduleSessionTemplate = ModuleSessionTemplateEntityFactory().withName("session 1").produce()
    val session = SessionFactory()
      .withProgrammeGroup(group)
      .withModuleSessionTemplate(moduleSessionTemplate)
      .withStartsAt(LocalDateTime.now().plusDays(1))
      .produce()

    val appointment = NDeliusAppointmentEntity(
      ndeliusAppointmentId = UUID.randomUUID(),
      session = session,
      referral = ReferralEntityFactory().produce(),
    )
    session.ndeliusAppointments.add(appointment)

    every { sessionRepository.findById(sessionId) } returns Optional.of(session)
    every { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) } returns ClientResult.Success(
      status = HttpStatus.OK,
      body = Unit,
    )

    // When
    service.rescheduleSessions(sessionId, request)

    // Then
    verify { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) }
  }

  @Test
  fun `rescheduleSessions should update nDelius appointments for all shifted sessions`() {
    // Given
    val sessionId = UUID.randomUUID()
    val request = RescheduleSessionRequest(
      sessionStartDate = LocalDate.now().plusDays(1),
      sessionStartTime = SessionTime(10, 0, AmOrPm.AM),
      rescheduleOtherSessions = true,
    )

    val facilitator = FacilitatorEntityFactory().produce()
    val group = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val template1 = ModuleSessionTemplateEntityFactory()
      .withName("session 1")
      .withSessionNumber(1)
      .withSessionType(GROUP)
      .produce()
    val template2 = ModuleSessionTemplateEntityFactory()
      .withName("session 2")
      .withSessionNumber(2)
      .withSessionType(GROUP)
      .produce()

    val session1 = SessionFactory()
      .withProgrammeGroup(group)
      .withModuleSessionTemplate(template1)
      .withStartsAt(LocalDateTime.now().plusDays(1))
      .produce()

    val session2 = SessionFactory()
      .withProgrammeGroup(group)
      .withModuleSessionTemplate(template2)
      .withStartsAt(LocalDateTime.now().plusDays(2))
      .produce()

    group.sessions.add(session1)
    group.sessions.add(session2)

    val appointment1 = NDeliusAppointmentEntity(
      ndeliusAppointmentId = UUID.randomUUID(),
      session = session1,
      referral = ReferralEntityFactory().produce(),
    )
    session1.ndeliusAppointments.add(appointment1)

    val appointment2 = NDeliusAppointmentEntity(
      ndeliusAppointmentId = UUID.randomUUID(),
      session = session2,
      referral = ReferralEntityFactory().produce(),
    )
    session2.ndeliusAppointments.add(appointment2)

    every { sessionRepository.findById(sessionId) } returns Optional.of(session1)
    every { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) } returns ClientResult.Success(
      status = HttpStatus.OK,
      body = Unit,
    )

    // When
    service.rescheduleSessions(sessionId, request)

    // Then
    verify(exactly = 2) { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) }
  }

  @Test
  fun `should save a new session attendance`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val sessionAttendee = SessionAttendeeFactory().withReferralId(referralId).produce()
    val sessionAttendance = SessionAttendanceFactory().withAttendees(listOf(sessionAttendee)).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory()
      .withId(UUID.randomUUID())
      .withTreatmentManager(facilitator)
      .produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        mutableListOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ),
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    sessionEntity.sessionFacilitators.add(
      SessionFacilitatorEntity(facilitator, sessionEntity, LEAD_FACILITATOR),
    )

    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        any(),
        any(),
      )
    } returns programmeGroupMembershipEntity
    every { sessionAttendanceOutcomeTypeRepository.findByCode(any()) } returns SessionAttendanceOutcomeTypeEntity(
      "ATTC",
      "Attended - Complied",
      true,
      true,
    )
    every { sessionRepository.save(any()) } returns sessionEntity

    // When
    val result = service.saveSessionAttendance(sessionId, sessionAttendance)

    // Then
    assertThat(result.responseMessage).isEqualTo("Attendance saved for session $sessionId")
    verify { sessionRepository.findById(any()) }
    verify { programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(any(), any()) }
    verify { sessionRepository.save(any()) }
  }

  @Test
  fun `should save a new session attendance and update ndelius when notes are present`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val sessionNotes = "Some session notes"
    val sessionAttendee = SessionAttendeeFactory().withReferralId(referralId).withSessionNotes(sessionNotes).produce()
    val sessionAttendance = SessionAttendanceFactory().withAttendees(listOf(sessionAttendee)).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory()
      .withId(UUID.randomUUID())
      .withTreatmentManager(facilitator)
      .produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val ndeliusAppointmentId = UUID.randomUUID()
    val sessionEntity = SessionFactory()
      .withAttendees(
        mutableListOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ),
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    sessionEntity.sessionFacilitators.add(
      SessionFacilitatorEntity(facilitator, sessionEntity, LEAD_FACILITATOR),
    )

    sessionEntity.ndeliusAppointments.add(
      NDeliusAppointmentEntity(
        ndeliusAppointmentId = ndeliusAppointmentId,
        session = sessionEntity,
        referral = referralEntity,
      ),
    )

    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        any(),
        any(),
      )
    } returns programmeGroupMembershipEntity
    every { sessionAttendanceOutcomeTypeRepository.findByCode(any()) } returns SessionAttendanceOutcomeTypeEntity(
      "ATTC",
      "Attended - Complied",
      true,
      true,
    )
    every { sessionRepository.save(any()) } returns sessionEntity
    every { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) } returns ClientResult.Success(
      HttpStatus.NO_CONTENT,
      Unit,
    )

    // When
    service.saveSessionAttendance(sessionId, sessionAttendance)

    // Then
    verify { nDeliusIntegrationApiClient.updateAppointmentsInDelius(match { it.appointments.any { app -> app.notes == sessionNotes && app.reference == ndeliusAppointmentId } }) }
  }

  @Test
  fun `should save a new session attendance and NOT update ndelius when notes are blank`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val sessionAttendee = SessionAttendeeFactory().withReferralId(referralId).withSessionNotes(" ").produce()
    val sessionAttendance = SessionAttendanceFactory().withAttendees(listOf(sessionAttendee)).produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory()
      .withId(UUID.randomUUID())
      .withTreatmentManager(facilitator)
      .produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        mutableListOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ),
      )
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    sessionEntity.sessionFacilitators.add(
      SessionFacilitatorEntity(facilitator, sessionEntity, LEAD_FACILITATOR),
    )

    val programmeGroupMembershipEntity = ProgrammeGroupMembershipFactory().produce()

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        any(),
        any(),
      )
    } returns programmeGroupMembershipEntity
    every { sessionAttendanceOutcomeTypeRepository.findByCode(any()) } returns SessionAttendanceOutcomeTypeEntity(
      "ATTC",
      "Attended - Complied",
      true,
      true,
    )
    every { sessionRepository.save(any()) } returns sessionEntity

    // When
    service.saveSessionAttendance(sessionId, sessionAttendance)

    // Then
    verify(exactly = 0) { nDeliusIntegrationApiClient.updateAppointmentsInDelius(any()) }
  }

  @Test
  fun `should throw a session not found exception on save a new session attendance`() {
    // Given
    val sessionId = UUID.randomUUID()
    val sessionAttendance = SessionAttendanceFactory().produce()

    every { sessionRepository.findById(any()) } returns Optional.empty<SessionEntity>()

    // When
    val exception = assertThrows<NotFoundException> {
      service.saveSessionAttendance(sessionId, sessionAttendance)
    }

    // Then
    assertTrue(exception.message!!.contains("Session not found with id: $sessionId"))
    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should throw a programme group membership not found exception on save a new session attendance`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val sessionAttendance = SessionAttendanceFactory()
      .withAttendees(listOf(SessionAttendeeFactory().withReferralId(referralId).produce()))
      .produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupId = UUID.randomUUID()
    val programmeGroupEntity = ProgrammeGroupFactory()
      .withId(programmeGroupId)
      .withTreatmentManager(facilitator)
      .produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        mutableListOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ),
      )
      .withIsCatchup(true)
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    sessionEntity.sessionFacilitators.add(
      SessionFacilitatorEntity(facilitator, sessionEntity, LEAD_FACILITATOR),
    )

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)
    every {
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(
        any(),
        any(),
      )
    } returns null

    // When
    val exception = assertThrows<NotFoundException> {
      service.saveSessionAttendance(sessionId, sessionAttendance)
    }

    // Then
    assertTrue(
      exception.message!!
        .contains("Programme group membership not found with referralId: $referralId and programmeGroupId: $programmeGroupId"),
    )
    verify { sessionRepository.findById(any()) }
    verify { programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(any(), any()) }
  }

  @Test
  fun `should throw a lead facilitator not found exception on save a new session attendance`() {
    // Given
    val sessionId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val sessionAttendance = SessionAttendanceFactory()
      .withAttendees(listOf(SessionAttendeeFactory().withReferralId(referralId).produce()))
      .produce()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory()
      .withId(UUID.randomUUID())
      .withTreatmentManager(facilitator)
      .produce()
    val module = ModuleEntityFactory().withName("Module 1").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(GROUP)
      .withModule(module)
      .withName("Getting started")
      .produce()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionEntity = SessionFactory()
      .withAttendees(
        mutableListOf(
          AttendeeFactory().withReferral(referralEntity)
            .withSession(
              SessionFactory().withProgrammeGroup(programmeGroupEntity)
                .withModuleSessionTemplate(moduleSessionTemplateEntity).produce(),
            ).produce(),
        ),
      )
      .withProgrammeGroup(programmeGroupEntity)
      .withModuleSessionTemplate(moduleSessionTemplateEntity)
      .produce()

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val exception =
      assertThrows<uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException> {
        service.saveSessionAttendance(sessionId, sessionAttendance)
      }

    // Then
    assertTrue(exception.message!!.contains("Lead facilitator not found for session: ${sessionEntity.id}"))
  }

  @Test
  fun `should return a record attendance with attended and complied attendance`() {
    // Given
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Post-programme reviews").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Template 1")
      .withModule(module)
      .produce()
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionId = UUID.randomUUID()
    val sessionEntity = SessionFactory()
      .withId(sessionId)
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

    sessionEntity.attendances = mutableSetOf(
      SessionAttendanceEntityFactory()
        .withSession(sessionEntity)
        .withOutcomeType(
          SessionAttendanceOutcomeTypeEntity(
            "ATTC",
            "Attended - Complied",
            true,
            true,
          ),
        )
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceBySessionId(sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.sessionTitle).isEqualTo("Template 1")
    assertThat(result.groupRegionName).isEqualTo("TEST REGION")
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].referralId).isEqualTo(referralId)
    assertThat(result.people[0].crn).isEqualTo(referralEntity.crn)
    assertThat(result.people[0].name).isEqualTo(referralEntity.personName)
    assertThat(result.people[0].attendance).isEqualTo("Attended")

    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with attended and did not comply attendance`() {
    // Given
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Post-programme reviews").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Template 1")
      .withModule(module)
      .produce()
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionId = UUID.randomUUID()
    val sessionEntity = SessionFactory()
      .withId(sessionId)
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

    sessionEntity.attendances = mutableSetOf(
      SessionAttendanceEntityFactory()
        .withSession(sessionEntity)
        .withOutcomeType(SessionAttendanceOutcomeTypeEntity("AFTC", "Attended - Failed to Comply", true, false))
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceBySessionId(sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isEqualTo("Attended but failed to comply")

    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with did not attend attendance`() {
    // Given
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Post-programme reviews").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Template 1")
      .withModule(module)
      .produce()
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionId = UUID.randomUUID()
    val sessionEntity = SessionFactory()
      .withId(sessionId)
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

    sessionEntity.attendances = mutableSetOf(
      SessionAttendanceEntityFactory()
        .withSession(sessionEntity)
        .withOutcomeType(SessionAttendanceOutcomeTypeEntity("UAAB", "Unacceptable Absence", false, false))
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceBySessionId(sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isEqualTo("Did not attend")

    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with no attendance`() {
    // Given
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val module = ModuleEntityFactory().withName("Post-programme reviews").produce()
    val moduleSessionTemplateEntity = ModuleSessionTemplateEntityFactory()
      .withSessionType(ONE_TO_ONE)
      .withName("Template 1")
      .withModule(module)
      .produce()
    val referralId = UUID.randomUUID()
    val referralEntity = ReferralEntityFactory().withId(referralId).withPersonName("John Smith").produce()
    val sessionId = UUID.randomUUID()
    val sessionEntity = SessionFactory()
      .withId(sessionId)
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

    sessionEntity.attendances = mutableSetOf()

    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceBySessionId(sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isNull()

    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should throw a not found exception when session id doesn't exist on return a record attendance`() {
    // Given
    val sessionId = UUID.randomUUID()
    every { sessionRepository.findById(any()) } returns Optional.empty()

    // When
    val exception = assertThrows<NotFoundException> {
      service.getRecordAttendanceBySessionId(sessionId)
    }

    // Then
    assertTrue(
      exception.message!!.contains("Session not found with id: $sessionId"),
    )
    verify { sessionRepository.findById(any()) }
  }
}
