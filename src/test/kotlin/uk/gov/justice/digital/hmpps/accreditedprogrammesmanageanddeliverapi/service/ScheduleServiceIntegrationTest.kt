package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class ScheduleServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var entityManager: EntityManager

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
  fun `Reschedule sessions should not delete already completed sessions`() {
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val slot2 = CreateGroupSessionSlotFactory().produce(DayOfWeek.THURSDAY, 12, 0, AmOrPm.PM)
    val slot3 = CreateGroupSessionSlotFactory().produce(DayOfWeek.FRIDAY, 2, 15, AmOrPm.PM)
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now().minusDays(7),
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
    assertThat(group.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 12, 11, 12, 0, 0))

    // Alter group start date for rescheduling
    group.earliestPossibleStartDate = LocalDate.now().plusYears(2)
    programmeGroupRepository.save(group)

    scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)!!
    assertThat(updatedGroup.sessions).hasSize(26)
    assertThat(updatedGroup.sessions.first().startsAt).isEqualTo(LocalDateTime.of(2025, 12, 11, 12, 0, 0))
    assertThat(updatedGroup.sessions.toList()[3].startsAt).isEqualTo(LocalDateTime.of(2027, 12, 17, 14, 15, 0))
  }
}
