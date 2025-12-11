package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository

class ScheduleServiceIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Autowired
  private lateinit var programmeGroupService: ProgrammeGroupService

  @Autowired
  private lateinit var accreditedProgrammeTemplateRepository: AccreditedProgrammeTemplateRepository

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
    val createGroupRequest = CreateGroupRequestFactory().produce()
    val group = programmeGroupService.createGroup(createGroupRequest, "AUTH_ADM")

    scheduleService.scheduleSessionForGroup(group.id!!)
  }
}
