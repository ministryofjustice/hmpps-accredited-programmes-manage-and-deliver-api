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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralCompletionData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.Duration.ofMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class ReferralCompleteEventTest(
  @Autowired private val sessionRepository: SessionRepository,
  @Autowired private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  @Autowired private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  @Autowired private val scheduleService: ScheduleService,
) : IntegrationTestBase() {

  @Test
  fun `publish a referral completed event and retrieve details via rest endpoint`() {
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

    // Go through referral journey update status -> assign to group
    val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val onProgrammeStatus = referralStatusDescriptionRepository.getOnProgrammeStatusDescription()
    val programmeCompleteStatus = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
    var referral = testReferralHelper.createReferral()
    referral = testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus)

    val programmeGroup = testGroupHelper.createGroup(earliestStartDate = LocalDate.now())
    referral = testGroupHelper.allocateToGroup(programmeGroup, referral)
    referral = testReferralHelper.updateReferralStatus(referral, onProgrammeStatus)

    val postProgrammeReviewTemplate =
      moduleSessionTemplateRepository.findByModuleId(UUID.fromString("ac581f6c-1d81-45a2-af1e-7e3a041ae756")).first()

    val scheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = postProgrammeReviewTemplate.id!!,
      referralIds = listOf(referral.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().minusWeeks(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val postProgrammeSession = scheduleService.scheduleIndividualSession(programmeGroup.id!!, scheduleSessionRequest)
    postProgrammeSession.startsAt = LocalDateTime.now().minusHours(2)
    sessionRepository.save(postProgrammeSession)
    val freshSession = sessionRepository.findByIdOrNull(postProgrammeSession.id!!)!!
    freshSession.startsAt = LocalDateTime.now().minusHours(2)
    sessionRepository.save(freshSession)
    val attendee = freshSession.attendees.find { it.referralId == referral.id }!!

    // Record attendance as attended and complied
    val attendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = attendee.referralId,
          outcomeCode = SessionAttendanceNDeliusCode.ATTC,
          sessionNotes = "Post-programme review completed successfully",
        ),
      ),
    )
    val attendanceResult = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${freshSession.id}/attendance",
      body = attendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(attendanceResult.attendees).isNotEmpty

    domainEventsQueueConfig.purgeAllQueues()

    referral = testReferralHelper.updateReferralStatus(referral, programmeCompleteStatus)

    // Wait for message to be processed
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { interventionsQueue.countAllMessagesOnQueue() } } matches { it == 2 }
    val messages = (1..2).map {
      with(domainEventsQueueConfig) {
        objectMapper.readValue<SQSMessage>(interventionsQueue.receiveMessageOnQueue().body())
      }
    }
    val secondEvent = messages.first {
      it.eventType.equals(HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value)
    }
    assertThat(secondEvent).isNotNull
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral.id}/completion-data",
      returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
    )

    assertThat(response).isNotNull
    assertThat(response.licReqId).isEqualTo(referral.eventId)
    assertThat(response.licReqCompletedAt).isCloseTo(postProgrammeSession.startsAt, within(1, ChronoUnit.SECONDS))
    assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
  }

  @Test
  fun `publish a referral completed event and retrieve details via rest endpoint when there are multiple post programme reviews in group scheduled`() {
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

    // Go through referral journey update status -> assign to group
    val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val onProgrammeStatus = referralStatusDescriptionRepository.getOnProgrammeStatusDescription()
    val programmeCompleteStatus = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
    var referral1 = testReferralHelper.createReferral()
    referral1 = testReferralHelper.updateReferralStatus(referral1, awaitingAllocationStatus)
    var referral2 = testReferralHelper.createReferral()
    referral2 = testReferralHelper.updateReferralStatus(referral2, awaitingAllocationStatus)

    val programmeGroup = testGroupHelper.createGroup(earliestStartDate = LocalDate.now())
    referral1 = testGroupHelper.allocateToGroup(programmeGroup, referral1)
    referral2 = testGroupHelper.allocateToGroup(programmeGroup, referral2)
    referral1 = testReferralHelper.updateReferralStatus(referral1, onProgrammeStatus)
    referral2 = testReferralHelper.updateReferralStatus(referral2, onProgrammeStatus)

    val postProgrammeReviewTemplate =
      moduleSessionTemplateRepository.findByModuleId(UUID.fromString("ac581f6c-1d81-45a2-af1e-7e3a041ae756")).first()

    // Schedule and mark attendance for referral2 Post programme review session
    val scheduleSessionRequest2 = ScheduleSessionRequest(
      sessionTemplateId = postProgrammeReviewTemplate.id!!,
      referralIds = listOf(referral2.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().minusWeeks(2),
      startTime = SessionTime(hour = 9, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 10, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val postProgrammeSession2 = scheduleService.scheduleIndividualSession(programmeGroup.id!!, scheduleSessionRequest2)
    postProgrammeSession2.startsAt = LocalDateTime.now().minusHours(4)
    sessionRepository.save(postProgrammeSession2)
    val freshSession2 = sessionRepository.findByIdOrNull(postProgrammeSession2.id!!)!!
    freshSession2.startsAt = LocalDateTime.now().minusHours(2)
    sessionRepository.save(freshSession2)
    val attendee2 = freshSession2.attendees.find { it.referralId == referral2.id }!!

    // Record attendance as attended and complied
    val attendanceRequest2 = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = attendee2.referralId,
          outcomeCode = SessionAttendanceNDeliusCode.ATTC,
          sessionNotes = "Post-programme review completed successfully",
        ),
      ),
    )
    val attendanceResult2 = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${freshSession2.id}/attendance",
      body = attendanceRequest2,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    val scheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = postProgrammeReviewTemplate.id!!,
      referralIds = listOf(referral1.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().minusWeeks(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val postProgrammeSession = scheduleService.scheduleIndividualSession(programmeGroup.id!!, scheduleSessionRequest)
    postProgrammeSession.startsAt = LocalDateTime.now().minusHours(2)
    sessionRepository.save(postProgrammeSession)
    val freshSession = sessionRepository.findByIdOrNull(postProgrammeSession.id!!)!!
    freshSession.startsAt = LocalDateTime.now().minusHours(2)
    sessionRepository.save(freshSession)
    val attendee = freshSession.attendees.find { it.referralId == referral1.id }!!

    // Record attendance as attended and complied
    val attendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = attendee.referralId,
          outcomeCode = SessionAttendanceNDeliusCode.ATTC,
          sessionNotes = "Post-programme review completed successfully",
        ),
      ),
    )
    val attendanceResult = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${freshSession.id}/attendance",
      body = attendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(attendanceResult.attendees).isNotEmpty

    domainEventsQueueConfig.purgeAllQueues()

    referral1 = testReferralHelper.updateReferralStatus(referral1, programmeCompleteStatus)

    // Wait for message to be processed
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { interventionsQueue.countAllMessagesOnQueue() } } matches { it == 2 }
    val messages = (1..2).map {
      with(domainEventsQueueConfig) {
        objectMapper.readValue<SQSMessage>(interventionsQueue.receiveMessageOnQueue().body())
      }
    }
    val secondEvent = messages.first {
      it.eventType.equals(HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value)
    }
    assertThat(secondEvent).isNotNull
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral1.id}/completion-data",
      returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
    )

    assertThat(response).isNotNull
    assertThat(response.licReqId).isEqualTo(referral1.eventId)
    assertThat(response.licReqCompletedAt).isCloseTo(postProgrammeSession.startsAt, within(1, ChronoUnit.SECONDS))
    assertThat(response.sourcedFromEntityType).isEqualTo(referral1.sourcedFrom)
  }
}
