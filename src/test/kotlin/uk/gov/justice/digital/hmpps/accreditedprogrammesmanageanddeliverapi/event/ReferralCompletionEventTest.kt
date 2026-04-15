package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.Duration.ofMillis
import java.time.LocalDate

class ReferralCompletionEventTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Test
  fun `publish a completion event and retrieve the details via rest endpoint`() {
    // Given - Create referral and allocate to group
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val group = testGroupHelper.createGroup()
    val referral = testReferralHelper.createReferral()
    val allocatedReferral = testGroupHelper.allocateToGroup(group, referral)

    // Update status to "On programme" first
    val onProgrammeStatusDescriptionId = referralStatusDescriptionRepository.getOnProgrammeStatusDescription().id
    referralService.updateStatus(
      allocatedReferral,
      onProgrammeStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Schedule a post-programme review session
    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val postProgrammeModule = buildingChoicesTemplate.modules.first { it.name == "Post-programme reviews" }
    val postProgrammeSessionTemplate = postProgrammeModule.sessionTemplates.first()

    val scheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = postProgrammeSessionTemplate.id!!,
      referralIds = listOf(referral.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().plusDays(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val session = scheduleService.scheduleIndividualSession(group.id!!, scheduleSessionRequest)

    val attendee = session.attendees.find { it.referralId == referral.id }!!

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
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${session.id}/attendance",
      body = attendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // Clear any existing messages from previous operations
    domainEventsQueueConfig.purgeAllQueues()

    // When - Update status to Programme complete

    val programmeCompleteStatusDescriptionId = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription().id
    referralService.updateStatus(
      referral,
      programmeCompleteStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Then - Wait for completion event to be published
    await withPollDelay ofMillis(100) withPollInterval ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        interventionsQueue.countAllMessagesOnQueue()
      }
    } matches { it == 1 }

    // Find the completion event (there may be multiple events, including status update event)
    var completionEventBody: SQSMessage? = null
    val messageCount = with(domainEventsQueueConfig) { interventionsQueue.countAllMessagesOnQueue() }

    repeat(messageCount!!) {
      val eventBody = objectMapper.readValue<SQSMessage>(
        with(domainEventsQueueConfig) {
          interventionsQueue.receiveMessageOnQueue().body()
        },
      )
      if (eventBody.eventType == HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value) {
        completionEventBody = eventBody
      }
    }

    assertThat(completionEventBody).isNotNull
    assertThat(completionEventBody!!.eventType).isEqualTo(HmppsDomainEventTypes.ACP_COMMUNITY_PROGRAMME_COMPLETE.value)

    val hmppsDomainEvent: DomainEventsMessage = objectMapper.readValue(completionEventBody!!.message)
    assertThat(hmppsDomainEvent.description).isEqualTo("An Accredited Programmes referral in community has been completed.")

    val extractUrlPath = hmppsDomainEvent.detailUrl!!.substringAfter("localhost:8080")
    assertThat(extractUrlPath).isEqualTo("/referral/${referral.id}/completion-data")

    // Make request for completion data details
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = extractUrlPath,
      returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
    )

    assertThat(response).isNotNull
    assertThat(response.requirementId).isEqualTo(referral.eventId)
    assertThat(response.requirementCompletedAt).isNotNull()
  }

  @Test
  fun `completion event should not be published when referral has no valid post-programme review attendance`() {
    // Given - Create referral and allocate to group
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val group = testGroupHelper.createGroup()
    val referral = testReferralHelper.createReferral()
    val allocatedReferral = testGroupHelper.allocateToGroup(group, referral)

    // Update status to "On programme" first
    val onProgrammeStatusDescriptionId = referralStatusDescriptionRepository.getOnProgrammeStatusDescription().id
    referralService.updateStatus(
      allocatedReferral,
      onProgrammeStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Clear any existing messages from previous operations
    domainEventsQueueConfig.purgeAllQueues()

    // When - Update status to Programme complete without post-programme review attendance
    val programmeCompleteStatusDescriptionId = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription().id
    referralService.updateStatus(
      allocatedReferral,
      programmeCompleteStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Then - Wait for status update event
    await withPollDelay ofMillis(100) withPollInterval ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        interventionsQueue.countAllMessagesOnQueue()
      }
    } matches { it == 1 }

    // Verify only status update event was sent, not completion event
    val eventBody = objectMapper.readValue<SQSMessage>(
      with(domainEventsQueueConfig) {
        interventionsQueue.receiveMessageOnQueue().body()
      },
    )
    assertThat(eventBody.eventType).isEqualTo(HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_CREATED.value)
  }

  @Test
  fun `completion event should not be published when referral did not attend post-programme review`() {
    // Given - Create referral and allocate to group
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val group = testGroupHelper.createGroup()
    val referral = testReferralHelper.createReferral()
    val allocatedReferral = testGroupHelper.allocateToGroup(group, referral)

    // Update status to "On programme" first
    val onProgrammeStatusDescriptionId = referralStatusDescriptionRepository.getOnProgrammeStatusDescription().id
    referralService.updateStatus(
      allocatedReferral,
      onProgrammeStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Schedule a post-programme review session
    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val postProgrammeModule = buildingChoicesTemplate.modules.first { it.name == "Post-programme reviews" }
    val postProgrammeSessionTemplate = postProgrammeModule.sessionTemplates.first()

    val scheduleSessionRequest = ScheduleSessionRequest(
      sessionTemplateId = postProgrammeSessionTemplate.id!!,
      referralIds = listOf(referral.id!!),
      facilitators = listOf(
        CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
      ),
      startDate = LocalDate.now().plusDays(1),
      startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
    )
    val session = scheduleService.scheduleIndividualSession(group.id!!, scheduleSessionRequest)

    val attendee = session.attendees.find { it.referralId == referral.id }!!

    // Record attendance as NOT attended
    val attendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = attendee.referralId,
          outcomeCode = SessionAttendanceNDeliusCode.UAAB,
          sessionNotes = "Did not attend post-programme review",
        ),
      ),
    )
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/${session.id}/attendance",
      body = attendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // Clear any existing messages from previous operations
    domainEventsQueueConfig.purgeAllQueues()

    // When - Update status to Programme complete
    // Fetch fresh referral from database to ensure the programmeGroup.sessions contains the newly scheduled session
    val freshReferral = referralRepository.findById(referral.id!!).get()
    val programmeCompleteStatusDescriptionId = referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription().id
    referralService.updateStatus(
      freshReferral,
      programmeCompleteStatusDescriptionId,
      createdBy = "SYSTEM",
    )

    // Then - Wait for status update event
    await withPollDelay ofMillis(100) withPollInterval ofMillis(100) untilCallTo {
      with(domainEventsQueueConfig) {
        interventionsQueue.countAllMessagesOnQueue()
      }
    } matches { it == 1 }

    // Verify only status update event was sent, not completion event
    val eventBody = objectMapper.readValue<SQSMessage>(
      with(domainEventsQueueConfig) {
        interventionsQueue.receiveMessageOnQueue().body()
      },
    )
    assertThat(eventBody.eventType).isEqualTo(HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_CREATED.value)
  }
}

