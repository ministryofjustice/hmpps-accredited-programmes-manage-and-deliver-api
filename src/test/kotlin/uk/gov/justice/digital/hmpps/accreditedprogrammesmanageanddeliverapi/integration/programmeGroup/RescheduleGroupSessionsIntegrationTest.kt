package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.programmeGroup

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupScheduleOverview
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionDateAndTimeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class RescheduleGroupSessionsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var scheduleService: ScheduleService

  private val now: LocalDate = LocalDate.of(2025, 11, 22)

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    whenever(clock.instant()).thenReturn(Instant.parse("2025-11-22T12:00:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    stubAuthTokenEndpoint()
    govUkApiStubs.stubBankHolidaysResponse()
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()
  }

  @Nested
  @DisplayName("When editing a group's slots")
  inner class WhenEditingGroupDaysAndTimes {

    @Test
    fun `does not change sessions in the past and moves future sessions onto the new slot day`() {
      // Given a single past Monday session and three future Monday sessions
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past
          2 to LocalDate.of(2025, 11, 24).atTime(10, 0),
          3 to LocalDate.of(2025, 12, 1).atTime(10, 0),
          4 to LocalDate.of(2025, 12, 8).atTime(10, 0),
        ),
      )
      val pastSession = fixture.sessions[0]

      // When the slots are changed to Wednesday and an automatic reschedule is requested
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(4)

      // Past session is untouched
      assertThat(overview.dateOf(pastSession.id)).isEqualTo(LocalDate.of(2025, 11, 17))

      // The rescheduled future sessions are all on the new Wednesday slot, in the future, and ordered
      val rescheduled = overview.sessions.filter { it.id != pastSession.id }.sortedBy { it.date }
      assertThat(rescheduled).hasSize(3)
      assertThat(rescheduled).allMatch { it.date.dayOfWeek == DayOfWeek.WEDNESDAY }
      assertThat(rescheduled).allMatch { it.date.isAfter(now) }
      assertThat(rescheduled.map { it.date }).isSorted
    }

    @Test
    fun `keeps every rescheduled session after the most recent past session with multiple slots`() {
      // Given two past sessions (Mon 17th, Thu 20th) and two future sessions, across two slots
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(
          DayOfWeek.MONDAY to LocalTime.of(10, 0),
          DayOfWeek.THURSDAY to LocalTime.of(12, 0),
        ),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past
          2 to LocalDate.of(2025, 11, 20).atTime(12, 0), // past (most recent)
          3 to LocalDate.of(2025, 11, 24).atTime(10, 0),
          4 to LocalDate.of(2025, 11, 27).atTime(12, 0),
        ),
      )
      val firstPast = fixture.sessions[0]
      val mostRecentPast = fixture.sessions[1]

      // When the group is changed to a single Wednesday slot
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then both past sessions are untouched
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(4)
      assertThat(overview.dateOf(firstPast.id)).isEqualTo(LocalDate.of(2025, 11, 17))
      assertThat(overview.dateOf(mostRecentPast.id)).isEqualTo(LocalDate.of(2025, 11, 20))

      // And the rescheduled sessions are strictly after the most recent past session and in the future
      val rescheduled = overview.sessions
        .filter { it.id != firstPast.id && it.id != mostRecentPast.id }
        .sortedBy { it.date }
      assertThat(rescheduled).hasSize(2)
      assertThat(rescheduled).allMatch { it.date.dayOfWeek == DayOfWeek.WEDNESDAY }
      assertThat(rescheduled).allMatch { it.date.isAfter(LocalDate.of(2025, 11, 20)) }
      assertThat(rescheduled).allMatch { it.date.isAfter(now) }
    }

    @Test
    fun `does not place regenerated sessions in the past when the last past session ran days ago`() {
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 10),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 10).atTime(10, 0), // past
          2 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past (most recent)
          3 to LocalDate.of(2025, 11, 24).atTime(10, 0),
          4 to LocalDate.of(2025, 12, 1).atTime(10, 0),
        ),
      )
      val pastSessions = listOf(fixture.sessions[0], fixture.sessions[1])

      // When the slots change to Wednesday
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then no regenerated session is placed on the past Wednesday (19 Nov); all are today or later
      val overview = scheduleOverview(fixture.group.id!!)
      val rescheduled = overview.sessions.filterNot { session -> session.id in pastSessions.map { it.id } }
      assertThat(rescheduled).hasSize(2)
      assertThat(rescheduled).allMatch { it.date.isAfter(now) }
      assertThat(rescheduled.minOf { it.date }).isEqualTo(LocalDate.of(2025, 11, 26))
    }

    @Test
    fun `regenerates every uncovered template when the schedule is out of template order so no sessions are dropped`() {
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 24).atTime(10, 0), // future
          2 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past (most recent), but earlier template
          3 to LocalDate.of(2025, 12, 1).atTime(10, 0), // future
          4 to LocalDate.of(2025, 12, 8).atTime(10, 0), // future
        ),
      )
      val pastSession = fixture.sessions[1]

      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then all four templates remain represented (no session dropped): 1 retained + 3 regenerated
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(4)
      assertThat(overview.dateOf(pastSession.id)).isEqualTo(LocalDate.of(2025, 11, 17))

      val rescheduled = overview.sessions.filter { it.id != pastSession.id }
      assertThat(rescheduled).hasSize(3)
      assertThat(rescheduled).allMatch { it.date.dayOfWeek == DayOfWeek.WEDNESDAY }
      assertThat(rescheduled).allMatch { it.date.isAfter(now) }
    }

    @Test
    fun `does not duplicate a template whose retained past session is later in template order`() {
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 10),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past (most recent)
          3 to LocalDate.of(2025, 11, 10).atTime(10, 0), // past, but later in template order
          2 to LocalDate.of(2025, 11, 24).atTime(10, 0), // future
        ),
      )
      val pastTemplate1 = fixture.sessions[0]
      val pastTemplate3 = fixture.sessions[1]

      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then there are exactly three sessions (no duplicate of template 3): 2 retained + 1 regenerated
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(3)
      assertThat(overview.dateOf(pastTemplate1.id)).isEqualTo(LocalDate.of(2025, 11, 17))
      assertThat(overview.dateOf(pastTemplate3.id)).isEqualTo(LocalDate.of(2025, 11, 10))

      val rescheduled = overview.sessions.filterNot { it.id == pastTemplate1.id || it.id == pastTemplate3.id }
      assertThat(rescheduled).hasSize(1)
      assertThat(rescheduled.single().date.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
      assertThat(rescheduled.single().date).isAfter(now)
    }

    @Test
    fun `does not place sessions in the past for a group with members whose start date has passed but no session has run yet`() {
      // Given a group WITH members whose earliest start date is already in the past (10 Nov), but
      // whose sessions are all still in the future, so there is no past session to anchor on. A
      // membership group must never have sessions placed in the past, even when rebuilding from a
      // passed start date.
      val fixture = buildGroup(
        hasMembership = true,
        earliestStartDate = LocalDate.of(2025, 11, 10),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 24).atTime(10, 0), // future
          2 to LocalDate.of(2025, 12, 1).atTime(10, 0), // future
          3 to LocalDate.of(2025, 12, 8).atTime(10, 0), // future
        ),
      )

      // When the slot changes to Wednesday with an automatic reschedule
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then every regenerated session is on the new Wednesday slot in the future, anchored on today
      // rather than the passed start date (which would otherwise place them on past Wednesdays)
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(3)
      assertThat(overview.sessions).allMatch { it.date.dayOfWeek == DayOfWeek.WEDNESDAY }
      assertThat(overview.sessions).allMatch { it.date.isAfter(now) }
      assertThat(overview.sessions.map { it.date }).isSorted
    }
  }

  @Nested
  @DisplayName("When rescheduling a single session with auto reschedule")
  inner class WhenReschedulingAnSingleSessionWithAutoReschedule {

    @Test
    fun `reschedules subsequent future sessions in template order leaving past sessions unchanged`() {
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past
          2 to LocalDate.of(2025, 11, 24).atTime(10, 0),
          3 to LocalDate.of(2025, 12, 1).atTime(10, 0),
          4 to LocalDate.of(2025, 12, 8).atTime(10, 0),
        ),
      )
      val (s1, s2, s3, s4) = fixture.sessions

      // When session 2 is moved to Mon 8 Dec, cascading to subsequent sessions
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/session/${s2.id}/reschedule",
        returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
        body = RescheduleSessionRequest(
          sessionStartDate = LocalDate.of(2025, 12, 8),
          sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
          sessionEndTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
          rescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then the past session is untouched and the subsequent sessions follow in template order
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.dateOf(s1.id)).isEqualTo(LocalDate.of(2025, 11, 17))
      assertThat(overview.dateOf(s2.id)).isEqualTo(LocalDate.of(2025, 12, 8))
      assertThat(overview.dateOf(s3.id)).isEqualTo(LocalDate.of(2025, 12, 15))
      assertThat(overview.dateOf(s4.id)).isEqualTo(LocalDate.of(2025, 12, 22))
    }

    @Test
    fun `does not move a subsequent session that is in the past`() {
      // Session 2 is later in template order than session 1 but, due to a previously broken
      // schedule, currently sits in the past. It must not be moved.
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 24).atTime(10, 0), // future
          2 to LocalDate.of(2025, 11, 17).atTime(10, 0), // past, but later in template order
          3 to LocalDate.of(2025, 12, 1).atTime(10, 0), // future
        ),
      )
      val (s1, s2, s3) = fixture.sessions

      // When session 1 is rescheduled with cascade
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/session/${s1.id}/reschedule",
        returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
        body = RescheduleSessionRequest(
          sessionStartDate = LocalDate.of(2025, 12, 8),
          sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
          sessionEndTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
          rescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then the past session 2 keeps its original date, while the future session 3 is moved
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.dateOf(s2.id)).isEqualTo(LocalDate.of(2025, 11, 17))
      assertThat(overview.dateOf(s1.id)).isEqualTo(LocalDate.of(2025, 12, 8))
      assertThat(overview.dateOf(s3.id)).isEqualTo(LocalDate.of(2025, 12, 15))
    }

    @Test
    fun `keeps subsequent future sessions in the future when a session is moved to the past`() {
      val fixture = buildGroup(
        earliestStartDate = LocalDate.of(2025, 11, 24),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 11, 24).atTime(10, 0), // future
          2 to LocalDate.of(2025, 12, 1).atTime(10, 0), // future
          3 to LocalDate.of(2025, 12, 8).atTime(10, 0), // future
        ),
      )
      val (s1, s2, s3) = fixture.sessions

      // When session 1 is moved back to a past Monday (10 Nov) with cascade
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/session/${s1.id}/reschedule",
        returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
        body = RescheduleSessionRequest(
          sessionStartDate = LocalDate.of(2025, 11, 10),
          sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
          sessionEndTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
          rescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then the later sessions are re-anchored on "now" rather than the past date, so they
      // remain in the future (and are never pushed onto a past Monday such as 17 Nov)
      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.dateOf(s1.id)).isEqualTo(LocalDate.of(2025, 11, 10))
      assertThat(overview.dateOf(s2.id)).isEqualTo(LocalDate.of(2025, 11, 24))
      assertThat(overview.dateOf(s3.id)).isEqualTo(LocalDate.of(2025, 12, 1))
      assertThat(overview.dateOf(s2.id)).isAfter(now)
      assertThat(overview.dateOf(s3.id)).isAfter(now)
    }
  }

  @Nested
  @DisplayName("Realistic Building Choices template (full schedule)")
  inner class RealisticBuildingChoicesSchedule {

    private val postProgrammeModuleName = "Post-programme reviews"

    @Test
    fun `editing slots preserves the past session and reschedules the rest onto the new slot in template order`() {
      val group = buildBuildingChoicesGroup(
        earliestStartDate = LocalDate.now(clock).minusDays(7),
        slotDay = DayOfWeek.MONDAY,
        slotTime = LocalTime.of(9, 30),
      )

      val before = scheduleOverview(group.id!!)
      val pastBefore = before.sessions.filter { !it.date.isAfter(now) }
      assertThat(pastBefore).hasSize(1)

      // When the slots change to Wednesday with an automatic reschedule
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          createGroupSessionSlot = setOf(CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 2, 0, AmOrPm.PM)),
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val overview = scheduleOverview(group.id!!)

      // No sessions dropped or duplicated
      assertThat(overview.sessions).hasSize(before.sessions.size)

      // The single past session (pre-group one-to-one) is untouched and still on its Monday
      val past = overview.sessions.filter { !it.date.isAfter(now) }
      assertThat(past).hasSize(1)
      assertThat(past.single().date).isEqualTo(pastBefore.single().date)
      assertThat(past.single().date.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)

      // Everything else moved onto the new Wednesday slot, in the future
      val rescheduled = overview.sessions.filter { it.date.isAfter(now) }
      assertThat(rescheduled).allMatch { it.date.dayOfWeek == DayOfWeek.WEDNESDAY }

      // Future sessions are placed in module/session template order
      val futureByTemplateOrder = sessionRepository.findByProgrammeGroupId(group.id!!)
        .filter { it.startsAt.toLocalDate().isAfter(now) }
        .filter { (it.sessionType == SessionType.GROUP || it.isPlaceholder) && !it.isCatchup }
        .sortedWith(compareBy({ it.moduleNumber }, { it.sessionNumber }))
      assertThat(futureByTemplateOrder.map { it.startsAt }).isSorted
    }

    @Test
    fun `single session cascade re-applies the six week gap before the post-programme reviews`() {
      // A real group whose whole schedule is in the future, so a cascade updates every later session.
      val group = buildBuildingChoicesGroup(
        earliestStartDate = LocalDate.now(clock).plusDays(3),
        slotDay = DayOfWeek.MONDAY,
        slotTime = LocalTime.of(9, 30),
      )

      val firstGroupSession = sessionRepository.findByProgrammeGroupId(group.id!!)
        .filter { it.sessionType == SessionType.GROUP && !it.isCatchup }
        .minByOrNull { it.startsAt }!!
      val newDate = firstGroupSession.startsAt.toLocalDate().plusWeeks(2)

      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/session/${firstGroupSession.id}/reschedule",
        returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
        body = RescheduleSessionRequest(
          sessionStartDate = newDate,
          sessionStartTime = SessionTime(hour = 9, minutes = 30, amOrPm = AmOrPm.AM),
          sessionEndTime = SessionTime(hour = 12, minutes = 0, amOrPm = AmOrPm.PM),
          rescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val sessions = sessionRepository.findByProgrammeGroupId(group.id!!)
      val firstPostProgramme =
        sessions.filter { it.moduleName == postProgrammeModuleName }.minByOrNull { it.startsAt }!!
      val lastBeforePostProgramme = sessions
        .filter { it.moduleName != postProgrammeModuleName && !it.isCatchup }
        .maxByOrNull { it.startsAt }!!

      assertThat(firstPostProgramme.startsAt.toLocalDate())
        .isAfterOrEqualTo(lastBeforePostProgramme.startsAt.toLocalDate().plusWeeks(6))
      assertThat(firstPostProgramme.startsAt.toLocalDate())
        .isBefore(lastBeforePostProgramme.startsAt.toLocalDate().plusWeeks(8))

      val overview = scheduleOverview(group.id!!)
      assertThat(overview.dateOf(firstGroupSession.id)).isEqualTo(newDate)
    }
  }

  @Nested
  @DisplayName("When cascade rescheduling past sessions in empty groups (in-flight migration)")
  inner class WhenCascadeReschedulingEmptyGroups {

    @Test
    fun `cascade reschedules subsequent past sessions into the future in template order`() {
      // An empty group whose entire schedule is in the past (created with an early start date).
      val fixture = buildGroup(
        hasMembership = false,
        earliestStartDate = LocalDate.of(2025, 9, 1),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 10, 6).atTime(10, 0), // past
          2 to LocalDate.of(2025, 10, 13).atTime(10, 0), // past
          3 to LocalDate.of(2025, 10, 20).atTime(10, 0), // past
          4 to LocalDate.of(2025, 10, 27).atTime(10, 0), // past
        ),
      )
      val (s1, s2, s3, s4) = fixture.sessions

      // Reschedule the (past) second session to a future Monday, cascading the rest
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/session/${s2.id}/reschedule",
        returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
        body = RescheduleSessionRequest(
          sessionStartDate = LocalDate.of(2025, 12, 1),
          sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
          sessionEndTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
          rescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val overview = scheduleOverview(fixture.group.id!!)
      // Sessions earlier in template order than the edited one stay where they are (in the past)
      assertThat(overview.dateOf(s1.id)).isEqualTo(LocalDate.of(2025, 10, 6))
      // The edited session and its (previously past) successors are now in the future, in order
      assertThat(overview.dateOf(s2.id)).isEqualTo(LocalDate.of(2025, 12, 1))
      assertThat(overview.dateOf(s3.id)).isEqualTo(LocalDate.of(2025, 12, 8))
      assertThat(overview.dateOf(s4.id)).isEqualTo(LocalDate.of(2025, 12, 15))
    }

    @Test
    fun `editing the start date regenerates every session including past ones from the new start date`() {
      val fixture = buildGroup(
        hasMembership = false,
        earliestStartDate = LocalDate.of(2025, 9, 1),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(
          1 to LocalDate.of(2025, 10, 6).atTime(10, 0),
          2 to LocalDate.of(2025, 10, 13).atTime(10, 0),
          3 to LocalDate.of(2025, 10, 20).atTime(10, 0),
          4 to LocalDate.of(2025, 10, 27).atTime(10, 0),
        ),
      )

      val newStart = LocalDate.of(2025, 12, 1) // future Monday
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.PUT,
        uri = "/group/${fixture.group.id}",
        returnType = object : ParameterizedTypeReference<UpdateGroupResponse>() {},
        body = UpdateGroupRequest(
          earliestStartDate = newStart,
          automaticallyRescheduleOtherSessions = true,
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      val overview = scheduleOverview(fixture.group.id!!)
      assertThat(overview.sessions).hasSize(4)
      // Every session (including the previously past ones) is regenerated on/after the new start date
      assertThat(overview.sessions).allMatch { !it.date.isBefore(newStart) }
      assertThat(overview.sessions).allMatch { it.date.dayOfWeek == DayOfWeek.MONDAY }
    }

    @Test
    fun `edit session details reports isEmptyGroup correctly`() {
      val emptyFixture = buildGroup(
        hasMembership = false,
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(1 to LocalDate.of(2025, 11, 17).atTime(10, 0)),
      )
      val nonEmptyFixture = buildGroup(
        hasMembership = true,
        earliestStartDate = LocalDate.of(2025, 11, 17),
        slots = listOf(DayOfWeek.MONDAY to LocalTime.of(10, 0)),
        sessionSpecs = listOf(1 to LocalDate.of(2025, 11, 17).atTime(10, 0)),
      )

      val emptyDetails = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/session/${emptyFixture.sessions.first().id}/edit-session-date-and-time",
        returnType = object : ParameterizedTypeReference<EditSessionDetails>() {},
      )
      val nonEmptyDetails = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/session/${nonEmptyFixture.sessions.first().id}/edit-session-date-and-time",
        returnType = object : ParameterizedTypeReference<EditSessionDetails>() {},
      )

      assertThat(emptyDetails.isEmptyGroup).isTrue()
      assertThat(nonEmptyDetails.isEmptyGroup).isFalse()
    }
  }

  private data class GroupToSessionsMap(val group: ProgrammeGroupEntity, val sessions: List<SessionEntity>)

  private fun buildGroup(
    earliestStartDate: LocalDate,
    slots: List<Pair<DayOfWeek, LocalTime>>,
    sessionSpecs: List<Pair<Int, LocalDateTime>>,
    hasMembership: Boolean = true,
  ): GroupToSessionsMap {
    val programmeTemplate =
      testDataGenerator.createAccreditedProgrammeTemplate("Reschedule ${randomAlphanumericString()}")
    val module = testDataGenerator.createModule(programmeTemplate, "Module A", 1)

    val group = ProgrammeGroupFactory()
      .withAccreditedProgrammeTemplate(programmeTemplate)
      .withTreatmentManager(testDataGenerator.createFacilitator(FacilitatorEntityFactory().produce()))
      .withEarliestStartDate(earliestStartDate)
      .withCode(randomAlphanumericString())
      .produce()
    group.programmeGroupSessionSlots = slots.map { (day, time) ->
      ProgrammeGroupSessionSlotEntity(programmeGroup = group, dayOfWeek = day, startTime = time)
    }.toMutableSet()
    testDataGenerator.createGroup(group)

    // A normal (non-empty) group protects past sessions during a reschedule. Empty groups
    // (hasMembership = false) may cascade-reschedule past sessions.
    if (hasMembership) {
      addMembership(group)
    }

    val sessions = sessionSpecs.map { (sessionNumber, startsAt) ->
      val template = testDataGenerator.createModuleSessionTemplate(
        module = module,
        name = "Session $sessionNumber",
        sessionNumber = sessionNumber,
        sessionType = SessionType.GROUP,
        durationMinutes = 60,
      )
      testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(template)
          .withStartsAt(startsAt)
          .withEndsAt(startsAt.plusHours(1))
          .produce(),
      )
    }
    return GroupToSessionsMap(group, sessions)
  }

  private fun buildBuildingChoicesGroup(
    earliestStartDate: LocalDate,
    slotDay: DayOfWeek,
    slotTime: LocalTime,
    hasMembership: Boolean = true,
  ): ProgrammeGroupEntity {
    val template = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val group = ProgrammeGroupFactory()
      .withAccreditedProgrammeTemplate(template)
      .withTreatmentManager(testDataGenerator.createFacilitator(FacilitatorEntityFactory().produce()))
      .withEarliestStartDate(earliestStartDate)
      .withCode(randomAlphanumericString())
      .produce()
    group.programmeGroupSessionSlots = mutableSetOf(
      ProgrammeGroupSessionSlotEntity(programmeGroup = group, dayOfWeek = slotDay, startTime = slotTime),
    )
    testDataGenerator.createGroup(group)
    if (hasMembership) {
      addMembership(group)
    }
    scheduleService.scheduleSessionsForGroup(group.id!!)
    return programmeGroupRepository.findByIdOrNull(group.id!!)!!
  }

  private fun addMembership(group: ProgrammeGroupEntity) {
    testDataGenerator.allocateReferralsToGroup(
      listOf(testDataGenerator.createReferral("Member ${randomAlphanumericString()}", randomAlphanumericString())),
      group,
    )
  }

  private fun scheduleOverview(groupId: UUID): GroupScheduleOverview = performRequestAndExpectOk(
    httpMethod = HttpMethod.GET,
    uri = "/bff/group/$groupId/schedule-overview",
    returnType = object : ParameterizedTypeReference<GroupScheduleOverview>() {},
  )

  private fun GroupScheduleOverview.dateOf(sessionId: UUID?): LocalDate = sessions.first { it.id == sessionId }.date
}
