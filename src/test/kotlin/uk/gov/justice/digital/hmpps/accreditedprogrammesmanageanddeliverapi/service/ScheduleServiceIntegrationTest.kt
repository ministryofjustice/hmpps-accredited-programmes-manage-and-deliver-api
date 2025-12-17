package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
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
import java.time.Clock
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
  private lateinit var clock: Clock

  @TestConfiguration
  class FixedClockConfig {

    @Bean
    @Primary
    fun fixedClock(): Clock = Clock.fixed(
      Instant.parse("2025-11-22T12:00:00Z"),
      ZoneId.of("Europe/London"),
    )
  }

  @BeforeEach
  fun setup() {
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

    assertThat(originalSchedule).hasSize(2)
    assertThat(rescheduled).hasSize(24)
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
}
