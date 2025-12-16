package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

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
  fun `Default availability config is returned when referral does not exist`() {
//    val createGroupTeamMemberFactory = CreateGroupTeamMemberFactory()
    val createGroupRequestFactory = CreateGroupRequestFactory()
    val referralId = UUID.randomUUID()

    val body = CreateGroupRequestFactory().produce(
      "TEST_GROUP",
      ProgrammeGroupCohort.GENERAL,
      ProgrammeGroupSexEnum.MALE,
      LocalDate.now().minusDays(7),
      setOf(
        CreateGroupSessionSlot(DayOfWeek.MONDAY, 1, 1, AmOrPm.AM),
        CreateGroupSessionSlot(DayOfWeek.TUESDAY, 6, 2, AmOrPm.PM),
        CreateGroupSessionSlot(DayOfWeek.WEDNESDAY, 3, 3, AmOrPm.AM),
      ),
    )
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)

    // module 1 session 1 : Last Tuesday
    // module 2 session 1 : Last Wednesday
    // module 2 session 2 : Monday

//    -- compare against now (everything after today -> move to in 2y time)

    group!!.earliestPossibleStartDate = LocalDate.now().plusYears(2)
    programmeGroupRepository.save(group)

    val result = scheduleService.rescheduleSessionsForGroup(group.id!!)

    val updatedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)
    assertThat(updatedGroup!!.sessions).hasSize(26)
  }
}
