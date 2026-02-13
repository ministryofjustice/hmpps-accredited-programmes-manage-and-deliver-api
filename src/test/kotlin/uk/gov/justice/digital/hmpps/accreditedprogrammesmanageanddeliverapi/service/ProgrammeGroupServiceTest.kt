package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.util.Optional
import java.util.UUID

class ProgrammeGroupServiceTest {
  private val programmeGroupRepository = mockk<ProgrammeGroupRepository>()
  private val groupWaitlistItemViewRepository = mockk<GroupWaitlistItemViewRepository>()
  private val referralReportingLocationRepository = mockk<ReferralReportingLocationRepository>()
  private val userService = mockk<UserService>()
  private val accreditedProgrammeTemplateRepository = mockk<AccreditedProgrammeTemplateRepository>()
  private val scheduleService = mockk<ScheduleService>()
  private val sessionRepository = mockk<SessionRepository>()
  private val facilitatorService = mockk<FacilitatorService>()
  private lateinit var service: ProgrammeGroupService

  @BeforeEach
  fun setup() {
    service = ProgrammeGroupService(
      programmeGroupRepository,
      groupWaitlistItemViewRepository,
      referralReportingLocationRepository,
      userService,
      accreditedProgrammeTemplateRepository,
      scheduleService,
      sessionRepository,
      facilitatorService,
    )
  }

  @Test
  fun `should return a record attendance with attended and complied attendance`() {
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
    var referralId = UUID.randomUUID()
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
        .withDidNotEngage(false)
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.sessionTitle).isEqualTo("Template 1")
    assertThat(result.groupRegionName).isEqualTo("TEST REGION")
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].referralId).isEqualTo(referralId.toString())
    assertThat(result.people[0].crn).isEqualTo(referralEntity.crn)
    assertThat(result.people[0].name).isEqualTo(referralEntity.personName)
    assertThat(result.people[0].attendance).isEqualTo("Attended")

    verify { programmeGroupRepository.findById(any()) }
    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with attended and did not comply attendance`() {
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
    var referralId = UUID.randomUUID()
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
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isEqualTo("Attended but failed to comply")

    verify { programmeGroupRepository.findById(any()) }
    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with did not attend attendance`() {
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
    var referralId = UUID.randomUUID()
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
        .withAttended(false)
        .withGroupMembership(ProgrammeGroupMembershipFactory().withReferral(referralEntity).produce()).produce(),
    )

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isEqualTo("Did not attend")

    verify { programmeGroupRepository.findById(any()) }
    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should return a record attendance with no attendance`() {
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
    var referralId = UUID.randomUUID()
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

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every { sessionRepository.findById(any()) } returns Optional.of(sessionEntity)

    // When
    val result = service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)

    // Then
    assertThat(result).isNotNull()
    assertThat(result.people).hasSize(1)
    assertThat(result.people[0].attendance).isNull()

    verify { programmeGroupRepository.findById(any()) }
    verify { sessionRepository.findById(any()) }
  }

  @Test
  fun `should throw a not found exception when group id doesn't exist on return a record attendance`() {
    // Given
    val groupId = UUID.randomUUID()
    val sessionId = UUID.randomUUID()

    every { programmeGroupRepository.findById(any()) } returns Optional.empty()

    // When
    val exception = assertThrows<NotFoundException> {
      service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)
    }

    // Then
    assertTrue(
      exception.message!!.contains("Programme Group not found with id: $groupId"),
    )
    verify { programmeGroupRepository.findById(any()) }
  }

  @Test
  fun `should throw a not found exception when session id doesn't exist on return a record attendance`() {
    // Given
    val groupId = UUID.randomUUID()
    val facilitator = FacilitatorEntityFactory().produce()
    val programmeGroupEntity = ProgrammeGroupFactory().withTreatmentManager(facilitator).produce()
    val sessionId = UUID.randomUUID()

    every { programmeGroupRepository.findById(any()) } returns Optional.of(programmeGroupEntity)
    every { sessionRepository.findById(any()) } returns Optional.empty()

    // When
    val exception = assertThrows<NotFoundException> {
      service.getRecordAttendanceByGroupIdAndSessionId(groupId, sessionId)
    }

    // Then
    assertTrue(
      exception.message!!.contains("Session not found with id: $sessionId"),
    )
    verify { programmeGroupRepository.findById(any()) }
    verify { sessionRepository.findById(any()) }
  }
}
