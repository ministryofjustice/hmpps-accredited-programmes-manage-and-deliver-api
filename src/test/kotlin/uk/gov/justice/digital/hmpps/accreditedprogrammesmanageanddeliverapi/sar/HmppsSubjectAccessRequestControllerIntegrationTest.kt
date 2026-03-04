package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.UAAB
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceNDeliusOutcomeEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupSessionSlotFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

class HmppsSubjectAccessRequestControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  private lateinit var referrals: List<ReferralEntity>

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
    nDeliusApiStubs.clearAllStubs()
    govUkApiStubs.stubBankHolidaysResponse()
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

  private fun initialiseReferrals() {
    referrals = testReferralHelper.createReferrals(
      referralConfigs =
      listOf(
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
        TestReferralHelper.ReferralConfig(reportingPdu = "PDU 1", reportingTeam = "Team A"),
      ),
    )
    // Update all referrals to 'Awaiting Allocation status'
    val status = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    referrals.forEach {
      referralService.updateStatus(
        it,
        status.id,
        createdBy = "AUTH_USER",
      )
    }
  }

  @Test
  fun `should return 200 on GET subject access request data`() {
    // Given
    initialiseReferrals()
    val referral1 = referrals[0]
    val referral2 = referrals[1]

    // Message history
    val messageHistory = MessageHistoryEntity(
      null,
      "event 1",
      "www.test.com/event_1",
      "test description",
      LocalDateTime.now(UTC),
      "test message",
      LocalDateTime.now(UTC),
      referral1,
    )
    messageHistoryRepository.saveAndFlush(messageHistory)

    // Create group
    val slot1 = CreateGroupSessionSlotFactory().produce(DayOfWeek.MONDAY, 9, 30, AmOrPm.AM)
    val body = CreateGroupRequestFactory().produce(
      createGroupSessionSlot = setOf(slot1),
    )
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/group",
      body = body,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val group = programmeGroupRepository.findByCode(body.groupCode)!!

    // Allocate referrals to group
    listOf(referral1, referral2).forEach { referral ->
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.POST,
        uri = "/group/${group.id}/allocate/${referral.id}",
        body = AllocateToGroupRequest(
          additionalDetails = "Test allocation",
        ),
        expectedResponseStatus = HttpStatus.OK.value(),
      )
    }

    val groupWithAllocation = programmeGroupRepository.findByCode(body.groupCode)!!
    val session = groupWithAllocation.sessions.first { !it.isPlaceholder }

    // Setup attendance and notes for the session
    stubAuthTokenEndpoint()
    val groupMembership1 = groupWithAllocation.programmeGroupMemberships.first { it.referral.id == referral1.id }
    val groupMembership2 = groupWithAllocation.programmeGroupMemberships.first { it.referral.id == referral2.id }

    val attendance1 = SessionAttendanceEntity(
      session = session,
      groupMembership = groupMembership1,
      outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().produce(),
    ).apply {
      notesHistory.add(SessionNotesHistoryEntity(attendance = this, notes = "Notes for referral 1"))
    }

    val attendance2 = SessionAttendanceEntity(
      session = session,
      groupMembership = groupMembership2,
      outcomeType = SessionAttendanceNDeliusOutcomeEntityFactory().withCode(UAAB)
        .withDescription("Unacceptable Absence")
        .withAttendance(false).withCompliant(false).produce(),
    ).apply {
      notesHistory.add(SessionNotesHistoryEntity(attendance = this, notes = "Notes for referral 2 - initial"))
      notesHistory.add(SessionNotesHistoryEntity(attendance = this, notes = "Notes for referral 2 - latest"))
    }

    session.attendances.addAll(listOf(attendance1, attendance2))
    sessionRepository.saveAndFlush(session)

    // When
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/subject-access-request?crn=${referral1.crn}",
      returnType = object : ParameterizedTypeReference<HmppsSubjectAccessRequestContent>() {},
    )

    // Then
    assertThat(response).isNotNull()
    assertThat(response.content).isNotNull()
    val content = response.content as LinkedHashMap<*, *>
    assertThat(content.containsKey("referrals")).isTrue()
    assertThat(content.get("referrals")).isNotNull()
    assertThat(content.containsKey("groupWaitlistItemViews")).isTrue()
    assertThat(content.get("groupWaitlistItemViews")).isNotNull()
    assertThat(content.containsKey("referralCaseListItemViews")).isTrue()
    assertThat(content.get("referralCaseListItemViews")).isNotNull()
  }

  @Test
  fun `should return 401 when unauthorised on GET subject access request data`() {
    // Given
    initialiseReferrals()
    val referral1 = referrals[0]

    // When
    webTestClient
      .method(HttpMethod.GET)
      .uri("/subject-access-request?crn=${referral1.crn}")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(
        setAuthorisation(
          roles = listOf(
            "ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR",
            "ROLE_OTHER",
          ),
        ),
      )
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
