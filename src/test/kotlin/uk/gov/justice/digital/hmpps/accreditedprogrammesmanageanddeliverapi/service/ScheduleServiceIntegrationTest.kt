package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ScheduleServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @BeforeEach
  fun setup() {
    whenever(clock.instant())
      .thenReturn(Instant.parse("2025-11-22T12:00:00Z"))
    whenever(clock.zone)
      .thenReturn(ZoneId.of("Europe/London"))
    testDataCleaner.cleanAllTables()

    nDeliusApiStubs.clearAllStubs()
    govUkApiStubs.stubBankHolidaysResponse()

    stubAuthTokenEndpoint()
    nDeliusApiStubs.stubUserTeamsResponse(
      "AUTH_ADM",
      NDeliusUserTeams(
        listOf(
          NDeliusUserTeam(
            code = "the_code",
            "The Team Description",
            pdu = CodeDescription("PDU_CODE", "PDU Description"),
            region = CodeDescription("REGION_CODE", "Region Description"),
          ),
        ),
      ),
    )
  }

  @Test
  fun `Schedule sessions should add 3 week buffer`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val slot2 = CreateGroupSessionSlotFactory().produce(DayOfWeek.THURSDAY, 12, 0, AmOrPm.PM)

    // Fixed clock so 2025-11-22 minus 4 days = 2025-11-18
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock).minusDays(4),
      createGroupSessionSlot = setOf(slot1, slot2),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    assertThat(group.sessions).hasSize(27)
    val (preGroupSessions, restOfSessions) = group.sessions.partition { it.moduleSessionTemplate.name == "Pre-group" }
    assertThat(restOfSessions.first().startsAt).isAfterOrEqualTo(preGroupSessions.first().startsAt.plusWeeks(3))
  }

  @Test
  fun `Schedule sessions should skip bank holidays`() {
    // Friday before bank holiday, so Monday = holiday
    whenever(clock.instant())
      .thenReturn(Instant.parse("2026-04-03T12:00:00Z"))
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val slot2 = CreateGroupSessionSlotFactory().produce(DayOfWeek.THURSDAY, 12, 0, AmOrPm.PM)

    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock),
      createGroupSessionSlot = setOf(slot1, slot2),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    assertThat(group.sessions).hasSize(27)
    assertThat(group.sessions.find { it.startsAt.toLocalDate() == LocalDate.of(2026, 3, 6) }).isNull()
  }

  @Test
  fun `Reschedule sessions should not delete already completed sessions when changing earliestStartDate`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val slot2 = CreateGroupSessionSlotFactory().produce(DayOfWeek.THURSDAY, 12, 0, AmOrPm.PM)
    val slot3 = CreateGroupSessionSlotFactory().produce(DayOfWeek.FRIDAY, 2, 15, AmOrPm.PM)

    // Fixed clock so 2025-11-22 minus 4 days = 2025-11-18
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock).minusDays(4),
      createGroupSessionSlot = setOf(slot1, slot2, slot3),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    assertThat(group.sessions).hasSize(27)
    // First scheduled session is THURSDAY slot @ 12pm
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 20, 12, 0, 0))

    // Alter group start date for rescheduling
    group.earliestPossibleStartDate = LocalDate.now(clock).plusYears(2)
    programmeGroupRepository.save(group)

    scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(27)
    // After reschedule first slot should stay same THURSDAY slot @ 12pm
    assertThat(updatedGroup.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 20, 12, 0, 0))
    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.year == 2025 }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(26)
    // >= because slots will overlap year
    assertThat(rescheduled).allMatch { it.startsAt.year >= 2027 }
  }

  @Test
  fun `Reschedule sessions should not delete already completed sessions when changing sessionSlots`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    // Earliest start date minus 7 days 2025-11-15 so we will have 1 slot completed
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock).minusDays(7),
      createGroupSessionSlot = setOf(slot1),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    assertThat(group.sessions).hasSize(27)
    // First scheduled session is Monday 17th  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))

    // Alter group start date for rescheduling
    group.programmeGroupSessionSlots = mutableSetOf(
      ProgrammeGroupSessionSlotEntity(
        programmeGroup = group,
        dayOfWeek = DayOfWeek.WEDNESDAY,
        startTime = LocalTime.of(15, 0),
      ),
    )
    programmeGroupRepository.save(group)

    scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(27)
    // First slot should be same as original after reschedule Monday 17th  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.dayOfWeek == DayOfWeek.MONDAY }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(26)
    assertThat(rescheduled).allMatch {
      it.startsAt.dayOfWeek == DayOfWeek.WEDNESDAY && it.startsAt.toLocalTime() == LocalTime.of(15, 0)
    }
  }

  @Test
  fun `Reschedule sessions should remove all attendees and reschedule when group has not started yet`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    // Earliest start date plus 7 days 2025-11-29 so we will have 0 slot completed
    val group = testGroupHelper.createGroup(
      earliestStartDate = LocalDate.now(clock).plusDays(7),
      createGroupSessionSlots = setOf(slot1),
    )

    val referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(),
        TestReferralHelper.ReferralConfig(),
      ),
    )
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    // Allocate all our referrals to a group
    referrals.forEach {
      programmeGroupMembershipService.allocateReferralToGroup(
        it.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )
    }

    val updatedGroupBeforeReschedule = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroupBeforeReschedule.sessions).hasSize(27)
    // First scheduled session is Monday 1st December  @ 9.30am
    assertThat(updatedGroupBeforeReschedule.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 12, 1, 9, 30, 0))
    assertThat(updatedGroupBeforeReschedule.sessions.map { it.attendees }).isNotEmpty

    // Only GROUP sessions get appointments during allocateReferralToGroup
    val groupSessionsWithAppointments = updatedGroupBeforeReschedule.sessions.filter { it.sessionType == SessionType.GROUP }
    assertThat(groupSessionsWithAppointments).isNotEmpty
    groupSessionsWithAppointments.forEach {
      assertThat(it.ndeliusAppointments).hasSize(2)
    }

    // Alter group start date for rescheduling
    updatedGroupBeforeReschedule.earliestPossibleStartDate = LocalDate.now(clock).plusYears(2)
    programmeGroupRepository.save(updatedGroupBeforeReschedule)

    scheduleService.rescheduleSessionsForGroup(updatedGroupBeforeReschedule.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(updatedGroupBeforeReschedule.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(27)
    val (originalSessions, rescheduleSessions) = updatedGroup.sessions.partition { it.startsAt.year <= 2026 }
    assertThat(rescheduleSessions).allMatch { it.startsAt.year >= 2027 }

    assertThat(originalSessions).hasSize(0)
    assertThat(rescheduleSessions).hasSize(27)

    // Verify ndeliusAppointments are removed from old sessions
    assertThat(rescheduleSessions.flatMap { it.ndeliusAppointments }).isEmpty()
  }

  @Test
  fun `Reschedule sessions should remove old attendances and keep ones related to sessions that have ran`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    // Earliest start date minus 7 days 2025-11-15 so we will have 1 slot completed
    val group = testGroupHelper.createGroup(
      earliestStartDate = LocalDate.now(clock).minusDays(7),
      createGroupSessionSlots = setOf(slot1),
    )

    val referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team C"),
      ),
    )
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    // Allocate all our referrals to a group
    referrals.forEach {
      programmeGroupMembershipService.allocateReferralToGroup(
        it.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )
    }

    val updatedGroupBeforeReschedule = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroupBeforeReschedule.sessions).hasSize(27)
    // First scheduled session is Monday 17th  @ 9.30am
    assertThat(updatedGroupBeforeReschedule.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    assertThat(updatedGroupBeforeReschedule.sessions.map { it.attendees }).isNotEmpty

    // Only GROUP sessions get appointments during allocateReferralToGroup
    val groupSessionsWithAppointments = updatedGroupBeforeReschedule.sessions.filter { it.sessionType == SessionType.GROUP }
    assertThat(groupSessionsWithAppointments).isNotEmpty
    groupSessionsWithAppointments.forEach {
      assertThat(it.ndeliusAppointments).hasSize(2)
    }

    // Alter group start date for rescheduling
    updatedGroupBeforeReschedule.programmeGroupSessionSlots = mutableSetOf(
      ProgrammeGroupSessionSlotEntity(
        programmeGroup = updatedGroupBeforeReschedule,
        dayOfWeek = DayOfWeek.WEDNESDAY,
        startTime = LocalTime.of(15, 0),
      ),
    )
    programmeGroupRepository.save(updatedGroupBeforeReschedule)

    scheduleService.rescheduleSessionsForGroup(updatedGroupBeforeReschedule.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(updatedGroupBeforeReschedule.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(27)

    // First session should be same as original after reschedule Monday 17th  @ 9.30am
    val firstSession = updatedGroup.sessions.first()
    assertThat(firstSession.startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    // It should still have its appointments IF it was a GROUP session
    if (firstSession.sessionType == SessionType.GROUP) {
      assertThat(firstSession.ndeliusAppointments).hasSize(2)
    }

    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.dayOfWeek == DayOfWeek.MONDAY }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(26)
    assertThat(rescheduled).allMatch {
      it.startsAt.dayOfWeek == DayOfWeek.WEDNESDAY && it.startsAt.toLocalTime() == LocalTime.of(15, 0)
    }

    // Verify ndeliusAppointments are removed from old sessions
    assertThat(rescheduled.flatMap { it.ndeliusAppointments }).isEmpty()
  }
}
