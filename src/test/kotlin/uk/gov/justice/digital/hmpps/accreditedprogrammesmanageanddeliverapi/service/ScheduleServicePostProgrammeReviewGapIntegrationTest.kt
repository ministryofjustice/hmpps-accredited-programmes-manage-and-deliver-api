package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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

class ScheduleServicePostProgrammeReviewGapIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @BeforeEach
  fun setup() {
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
  fun `Schedule sessions should add 6 week gap before Post-programme reviews module`() {
    // Given
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val body = CreateGroupRequestFactory().produce(
      earliestStartDate = LocalDate.now(clock),
      createGroupSessionSlot = setOf(slot1),
    )

    // When
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // Then
    val group = programmeGroupRepository.findByCode(body.groupCode)!!
    val allSessions = group.sessions.sortedBy { it.startsAt }
    val postProgrammeReviewsSession = allSessions.first { it.moduleSessionTemplate.module.name == "Post-programme reviews" }
    val nonPostProgrammeReviewsSessions = allSessions.filter { it.moduleSessionTemplate.module.name != "Post-programme reviews" }

    val lastNonPostProgrammeSession = nonPostProgrammeReviewsSessions.last()
    assertThat(postProgrammeReviewsSession.startsAt).isAfterOrEqualTo(lastNonPostProgrammeSession.startsAt.plusWeeks(6))
  }
}
