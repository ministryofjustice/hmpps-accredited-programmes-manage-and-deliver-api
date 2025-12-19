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

    assertThat(group.sessions).hasSize(26)
    val (preGroupSessions, restOfSessions) = group.sessions.partition { it.moduleSessionTemplate.name == "Pre-Group" }
    assertThat(restOfSessions.first().startsAt).isAfterOrEqualTo(preGroupSessions.first().startsAt.plusWeeks(3))
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

    assertThat(group.sessions).hasSize(26)
    // First scheduled session is THURSDAY slot @ 12pm
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 20, 12, 0, 0))

    // Alter group start date for rescheduling
    group.earliestPossibleStartDate = LocalDate.now(clock).plusYears(2)
    programmeGroupRepository.save(group)

    scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(26)
    // After reschedule first slot should stay same THURSDAY slot @ 12pm
    assertThat(updatedGroup.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 20, 12, 0, 0))
    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.year == 2025 }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(25)
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

    assertThat(group.sessions).hasSize(26)
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
    assertThat(updatedGroup.sessions).hasSize(26)
    // First slot should be same as original after reschedule Monday 17th  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.dayOfWeek == DayOfWeek.MONDAY }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(25)
    assertThat(rescheduled).allMatch {
      it.startsAt.dayOfWeek == DayOfWeek.WEDNESDAY && it.startsAt.toLocalTime() == LocalTime.of(15, 0)
    }
  }

  @Test
  fun `Reschedule sessions should remove all attendances and reschedule when group has not started yet`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    // Earliest start date plus 7 days 2025-11-29 so we will have 0 slot completed
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock).plusDays(7),
      createGroupSessionSlot = setOf(slot1),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    val referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(),
        TestReferralHelper.ReferralConfig(),
      ),
    )
    // Allocate all our referrals to a group
    referrals.forEach {
      programmeGroupMembershipService.allocateReferralToGroup(
        it.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )
    }

    assertThat(group.sessions).hasSize(26)
    // First scheduled session is Monday 1st December  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 12, 1, 9, 30, 0))
    assertThat(group.sessions.sumOf { it.attendances.size }).isEqualTo(52)

    // Alter group start date for rescheduling
    group.earliestPossibleStartDate = LocalDate.now(clock).plusYears(2)
    programmeGroupRepository.save(group)

    scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(26)
    assertThat(updatedGroup.sessions.sumOf { it.attendances.size }).isEqualTo(52)
    val (originalAttendance, rescheduledAttendance) = updatedGroup.sessions.flatMap { it.attendances }
      .partition { it.session.startsAt.year <= 2026 }
    assertThat(rescheduledAttendance).allMatch { it.session.startsAt.year >= 2027 }

    assertThat(originalAttendance).hasSize(0)
    assertThat(rescheduledAttendance).hasSize(52)

    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.year <= 2026 }

    assertThat(originalSchedule).hasSize(0)
    assertThat(rescheduled).hasSize(26)
    assertThat(rescheduled).allMatch { it.startsAt.year >= 2027 }
  }

  @Test
  fun `Reschedule sessions should remove old attendances and keep ones related to sessions that have ran`() {
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

    val referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team C"),
      ),
    )
    // Allocate all our referrals to a group
    referrals.forEach {
      programmeGroupMembershipService.allocateReferralToGroup(
        it.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )
    }

    assertThat(group.sessions).hasSize(26)
    // First scheduled session is Monday 17th  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    assertThat(group.sessions.sumOf { it.attendances.size }).isEqualTo(52)

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
    assertThat(updatedGroup.sessions).hasSize(26)
    assertThat(updatedGroup.sessions.sumOf { it.attendances.size }).isEqualTo(52)
    val (originalAttendance, rescheduledAttendance) = updatedGroup.sessions.flatMap { it.attendances }
      .partition { it.session.startsAt.dayOfWeek == DayOfWeek.MONDAY }
    assertThat(rescheduledAttendance).allMatch { it.session.startsAt.dayOfWeek == DayOfWeek.WEDNESDAY }

    assertThat(originalAttendance).hasSize(2)
    assertThat(rescheduledAttendance).hasSize(50)

    // First slot should be same as original after reschedule Monday 17th  @ 9.30am
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 11, 17, 9, 30, 0))
    val (originalSchedule, rescheduled) = updatedGroup.sessions.partition { it.startsAt.dayOfWeek == DayOfWeek.MONDAY }

    assertThat(originalSchedule).hasSize(1)
    assertThat(rescheduled).hasSize(25)
    assertThat(rescheduled).allMatch {
      it.startsAt.dayOfWeek == DayOfWeek.WEDNESDAY && it.startsAt.toLocalTime() == LocalTime.of(15, 0)
    }
  }
}
