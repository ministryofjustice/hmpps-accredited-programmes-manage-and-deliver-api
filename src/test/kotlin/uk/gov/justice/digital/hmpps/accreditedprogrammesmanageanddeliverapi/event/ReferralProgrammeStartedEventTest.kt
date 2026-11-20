package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralStartData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ALLOCATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.Duration.ofMillis
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReferralProgrammeStartedEventTest(
  @Autowired private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  @Autowired private val scheduleService: ScheduleService,
) : IntegrationTestBase() {

  @Test
  fun `publish a programme started event when the first pre-group one-to-one is attended`() {
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

    val awaitingAllocationStatus = referralStatusDescriptionRepository.findByReferralStatusType(AWAITING_ALLOCATION)
    var referral = testReferralHelper.createReferral()
    referral = testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus!!)

    val programmeGroup = testGroupHelper.createGroup(earliestStartDate = LocalDate.now())
    referral = testGroupHelper.allocateToGroup(programmeGroup, referral)

    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val preGroupModule = buildingChoicesTemplate.modules.first { it.name == "Pre-group one-to-ones" }
    val preGroupSessionTemplate = preGroupModule.sessionTemplates.first()

    val scheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = preGroupSessionTemplate.id!!,
      referralIds = listOf(referral.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().plusDays(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val preGroupSession = scheduleService.scheduleSession(programmeGroup.id!!, scheduleSessionRequest)
    val attendee = preGroupSession.attendees.find { it.referralId == referral.id }!!

    domainEventsQueueConfig.purgeAllQueues()

    // Record attendance as attended
    val attendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = attendee.referralId,
          outcomeCode = SessionAttendanceNDeliusCode.ATTC,
          sessionNotes = "Attended pre-group one-to-one",
        ),
      ),
    )
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${preGroupSession.id}/attendance",
      body = attendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // Wait for message to be processed
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { interventionsQueue.countAllMessagesOnQueue() } } matches { it!! >= 1 }
    val message = with(domainEventsQueueConfig) {
      objectMapper.readValue<SQSMessage>(interventionsQueue.receiveMessageOnQueue().body())
    }

    assertThat(message.eventType).isEqualTo(HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_STARTED.value)

    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral.id}/start-data",
      returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
    )

    assertThat(response).isNotNull
    assertThat(response.licReqId).isEqualTo(referral.eventId)
    assertThat(response.licReqStartedAt).isCloseTo(preGroupSession.startsAt, within(1, ChronoUnit.SECONDS))
    assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
  }

  @Test
  fun `does not publish a second programme started event when a later pre-group one-to-one is also attended`() {
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

    val awaitingAllocationStatus = referralStatusDescriptionRepository.findByReferralStatusType(AWAITING_ALLOCATION)
    var referral = testReferralHelper.createReferral()
    referral = testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus!!)

    val programmeGroup = testGroupHelper.createGroup(earliestStartDate = LocalDate.now())
    referral = testGroupHelper.allocateToGroup(programmeGroup, referral)

    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val preGroupModule = buildingChoicesTemplate.modules.first { it.name == "Pre-group one-to-ones" }
    val preGroupSessionTemplate = preGroupModule.sessionTemplates.first()

    val firstScheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = preGroupSessionTemplate.id!!,
      referralIds = listOf(referral.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().plusDays(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val firstSession = scheduleService.scheduleSession(programmeGroup.id!!, firstScheduleSessionRequest)
    val firstAttendee = firstSession.attendees.find { it.referralId == referral.id }!!

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${firstSession.id}/attendance",
      body = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = firstAttendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.ATTC,
            sessionNotes = "Attended first pre-group one-to-one",
          ),
        ),
      ),
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val firstResponse = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral.id}/start-data",
      returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
    )

    domainEventsQueueConfig.purgeAllQueues()

    val secondSession = scheduleService.scheduleSession(
      programmeGroup.id!!,
      firstScheduleSessionRequest.copy(startDate = LocalDate.now().plusDays(2)),
    )
    val secondAttendee = secondSession.attendees.find { it.referralId == referral.id }!!

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${secondSession.id}/attendance",
      body = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = secondAttendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.ATTC,
            sessionNotes = "Attended second pre-group one-to-one",
          ),
        ),
      ),
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // No programme started event should be published for the second attendance
    with(domainEventsQueueConfig) {
      await withPollDelay ofMillis(200) untilCallTo { interventionsQueue.countAllMessagesOnQueue() } matches { it == 0 }
    }

    val secondResponse = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral.id}/start-data",
      returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
    )

    assertThat(secondResponse.licReqStartedAt).isEqualTo(firstResponse.licReqStartedAt)
  }
}
