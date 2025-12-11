package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@WithMockAuthUser("AUTOMATED_TEST_USER")
class ScheduleServiceIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var accreditedProgrammeTemplateRepository: AccreditedProgrammeTemplateRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @BeforeEach
  override fun beforeEach() {
    stubAuthTokenEndpoint()
    nDeliusApiStubs.stubUserTeamsResponse(
      "AUTH_ADM",
      NDeliusUserTeams(
        teams = listOf(
          NDeliusUserTeam(
            code = "TEAM001",
            description = "Test Team 1",
            pdu = CodeDescription("PDU001", "Test PDU 1"),
            region = CodeDescription("REGION001", "WIREMOCKED REGION"),
          ),
        ),
      ),
    )
  }

  @Test
  fun `should generate sessions for a group based on template`() {
    val sessionSlots = CreateGroupSessionSlotFactory().produceUniqueSlots(3)
    val createGroupRequest = CreateGroupRequestFactory().produce(createGroupSessionSlot = sessionSlots)
    val group = programmeGroupService.createGroup(createGroupRequest, "AUTH_ADM")

    scheduleService.scheduleSessionForGroup(group.id!!)

    val sessions = sessionRepository.findAll()

    assertThat(sessions).isNotEmpty
  }
}
