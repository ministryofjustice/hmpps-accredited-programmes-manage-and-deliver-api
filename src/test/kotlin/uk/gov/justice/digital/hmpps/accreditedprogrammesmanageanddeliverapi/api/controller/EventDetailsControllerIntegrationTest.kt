package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralCompletionData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralStartData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ALLOCATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.LocalDate
import java.util.UUID

class EventDetailsControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var scheduleService: ScheduleService

  @Nested
  inner class GetStatusChangeDetails {
    @Test
    fun `should return 200 response and ReferralStatusInfo`() {
      // Given
      // Creates referral and moves to awaiting allocation status
      val awaitingAllocationStatus =
        referralStatusDescriptionRepository.findByDescription(AWAITING_ALLOCATION.description)
      val referral = testReferralHelper.createReferral()
      testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus!!, "TEST ADDITIONAL DETAILS")

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/status-change-details",
        returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.newStatus).isEqualTo(ReferralStatusInfo.Status.AWAITING_ALLOCATION)
      assertThat(response.sourcedFromEntityId).isEqualTo(referral.eventId!!.toLong())
      assertThat(response.eventNumber).isEqualTo(referral.eventNumber)
      assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
      assertThat(response.notes).isEqualTo("TEST ADDITIONAL DETAILS")
      assertThat(response.description).isEqualTo("The person is ready to be allocated to a programme group.")
      assertThat(response.username).isEqualTo("AUTH_USER")
    }

    @Test
    fun `should return the probation practitioner as the username when the status was set by SYSTEM`() {
      // Given
      val referral = testReferralHelper.createReferral()
      stubPersonalDetailsWithPractitioner(
        referral.crn,
        ProbationPractitioner(name = FullName(forename = "Jane", surname = "Practitioner"), code = "PP123"),
      )

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/status-change-details",
        returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
      )

      // Then
      assertThat(response.newStatus).isEqualTo(ReferralStatusInfo.Status.AWAITING_ASSESSMENT)
      assertThat(response.username).isEqualTo("PP123")
    }

    @Test
    fun `should fall back to SYSTEM when nDelius has no probation practitioner for the person`() {
      // Given
      val referral = testReferralHelper.createReferral()
      stubPersonalDetailsWithPractitioner(referral.crn, probationPractitioner = null)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/status-change-details",
        returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
      )

      // Then
      assertThat(response.username).isEqualTo("SYSTEM")
    }

    @Test
    fun `should fall back to SYSTEM when the nDelius personal details call fails`() {
      // Given
      val referral = testReferralHelper.createReferral()
      nDeliusApiStubs.stubNotFoundPersonalDetailsResponse(referral.crn)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/status-change-details",
        returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
      )

      // Then
      assertThat(response.username).isEqualTo("SYSTEM")
    }

    private fun stubPersonalDetailsWithPractitioner(crn: String, probationPractitioner: ProbationPractitioner?) {
      nDeliusApiStubs.stubPersonalDetailsResponseForCrn(
        crn,
        NDeliusPersonalDetailsFactory()
          .withCrn(crn)
          .withProbationPractitioner(probationPractitioner)
          .produce(),
      )
    }

    @Test
    fun `should return 401 when not authorized`() {
      // Given
      // Creates referral and moves to awaiting allocation status
      val awaitingAllocationStatus =
        referralStatusDescriptionRepository.findByDescription(AWAITING_ALLOCATION.description)
      val referral = testReferralHelper.createReferralAndUpdateStatus(awaitingAllocationStatus)

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/${referral.id}/status-change-details")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  inner class GetCompletionData {

    @BeforeEach
    fun beforeEach() {
      nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()
    }

    @Test
    fun `should return completion data when referral attended and complied with post-programme review`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val session = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

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

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
      )

      assertThat(response).isNotNull
      assertThat(response.licReqId).isEqualTo(referral.eventId)
      assertThat(response.licReqCompletedAt).isNotNull()
      assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
    }

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${UUID.randomUUID()}/completion-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when referral has no group membership`() {
      val referral = testReferralHelper.createReferral()

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when no post-programme review session exists`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 400 when referral did not attend post-programme review`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val session = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

      val attendee = session.attendees.find { it.referralId == referral.id }!!

      // Record attendance as not attended
      val attendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.UAAB,
            sessionNotes = "Did not attend",
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

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
      )
    }

    @Test
    fun `should return most recent attendance when multiple attendances exist`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val firstSession = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

      // Record attendance on the first session as "not attended"
      val firstAttendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = firstSession.attendees.find { it.referralId == referral.id }!!.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.UAAB,
            sessionNotes = "Did not attend first time",
          ),
        ),
      )
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${firstSession.id}/attendance",
        body = firstAttendanceRequest,
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Schedule a second post-programme review session and record attended and complied
      val secondSession = scheduleService.scheduleSession(
        group.id!!,
        scheduleSessionRequest.copy(startDate = LocalDate.now().plusDays(2)),
      )
      val secondAttendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = secondSession.attendees.find { it.referralId == referral.id }!!.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.ATTC,
            sessionNotes = "Attended and complied second time",
          ),
        ),
      )
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${secondSession.id}/attendance",
        body = secondAttendanceRequest,
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
      )

      assertThat(response).isNotNull
      assertThat(response.licReqId).isEqualTo(referral.eventId)
      assertThat(response.licReqCompletedAt).isNotNull()
      assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
    }
  }

  @Nested
  inner class GetStartData {

    @BeforeEach
    fun beforeEach() {
      nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()
    }

    @Test
    fun `should return start data when referral attended their pre-group one-to-one`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val session = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

      val attendee = session.attendees.find { it.referralId == referral.id }!!

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
        uri = "/session/${session.id}/attendance",
        body = attendanceRequest,
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
      )

      assertThat(response).isNotNull
      assertThat(response.licReqId).isEqualTo(referral.eventId)
      assertThat(response.licReqStartedAt).isNotNull()
      assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
    }

    @Test
    fun `should return 404 when referral is not found`() {
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${UUID.randomUUID()}/start-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when no actual start date has been recorded`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when referral did not attend their only pre-group one-to-one`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val session = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

      val attendee = session.attendees.find { it.referralId == referral.id }!!

      // Record attendance as not attended
      val attendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.UAAB,
            sessionNotes = "Did not attend",
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

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should return 404 when referral attended their only pre-group one-to-one but failed to comply`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val session = scheduleService.scheduleSession(group.id!!, scheduleSessionRequest)

      val attendee = session.attendees.find { it.referralId == referral.id }!!

      // Record attendance as attended but failed to comply - this must not set an actual start date
      val attendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.AFTC,
            sessionNotes = "Attended but failed to comply",
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

      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }

    @Test
    fun `should use the first complied pre-group one-to-one and ignore an earlier attended but failed to comply one`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

      val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      val preGroupModule = buildingChoicesTemplate.modules.first { it.name == "Pre-group one-to-ones" }
      val preGroupSessionTemplate = preGroupModule.sessionTemplates.first()

      val failedToComplyRequest = ScheduleSessionRequest(
        sessionTemplateId = preGroupSessionTemplate.id!!,
        referralIds = listOf(referral.id!!),
        facilitators = listOf(
          CreateGroupTeamMemberFactory().withTeamMemberType(CreateGroupTeamMemberType.REGULAR_FACILITATOR).produce(),
        ),
        startDate = LocalDate.now().plusDays(1),
        startTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
        endTime = SessionTime(hour = 11, minutes = 30, amOrPm = AmOrPm.AM),
      )
      val failedToComplySession = scheduleService.scheduleSession(group.id!!, failedToComplyRequest)
      val failedToComplyAttendee = failedToComplySession.attendees.find { it.referralId == referral.id }!!

      // Earlier session: attended but failed to comply - should be ignored
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${failedToComplySession.id}/attendance",
        body = SessionAttendance(
          attendees = listOf(
            SessionAttendee(
              referralId = failedToComplyAttendee.referralId,
              outcomeCode = SessionAttendanceNDeliusCode.AFTC,
              sessionNotes = "Attended but failed to comply",
            ),
          ),
        ),
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Later session: attended and complied - this is the one that should set the actual start date
      val compliedRequest = failedToComplyRequest.copy(startDate = LocalDate.now().plusDays(2))
      val compliedSession = scheduleService.scheduleSession(group.id!!, compliedRequest)
      val compliedAttendee = compliedSession.attendees.find { it.referralId == referral.id }!!

      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${compliedSession.id}/attendance",
        body = SessionAttendance(
          attendees = listOf(
            SessionAttendee(
              referralId = compliedAttendee.referralId,
              outcomeCode = SessionAttendanceNDeliusCode.ATTC,
              sessionNotes = "Attended and complied",
            ),
          ),
        ),
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
      )

      assertThat(response.licReqStartedAt.toLocalDate()).isEqualTo(compliedSession.startsAt.toLocalDate())
      assertThat(response.licReqStartedAt.toLocalDate()).isNotEqualTo(failedToComplySession.startsAt.toLocalDate())
    }

    @Test
    fun `should not overwrite actual start date when a later pre-group one-to-one is also attended`() {
      val group = testGroupHelper.createGroup()
      val referral = testReferralHelper.createReferral()
      testGroupHelper.allocateToGroup(group, referral)

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
      val firstSession = scheduleService.scheduleSession(group.id!!, firstScheduleSessionRequest)
      val firstAttendee = firstSession.attendees.find { it.referralId == referral.id }!!

      // Record attendance as attended on the first pre-group one-to-one
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

      // Schedule and attend a second, later, pre-group one-to-one for the same referral
      val secondScheduleSessionRequest = firstScheduleSessionRequest.copy(startDate = LocalDate.now().plusDays(2))
      val secondSession = scheduleService.scheduleSession(group.id!!, secondScheduleSessionRequest)
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

      val secondResponse = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/start-data",
        returnType = object : ParameterizedTypeReference<ReferralStartData>() {},
      )

      assertThat(secondResponse.licReqStartedAt).isEqualTo(firstResponse.licReqStartedAt)
    }
  }
}
