package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import java.time.LocalDate
import java.util.UUID

class StatusChangeDetailsControllerIntegrationTest : IntegrationTestBase() {

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
      val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
      val referral = testReferralHelper.createReferral()
      testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus, "TEST ADDITIONAL DETAILS")

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
      assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
      assertThat(response.notes).isEqualTo("TEST ADDITIONAL DETAILS")
      assertThat(response.description).isEqualTo("The person is ready to be allocated to a programme group.")
    }

    @Test
    fun `should return 401 when not authorized`() {
      // Given
      // Creates referral and moves to awaiting allocation status
      val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
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

      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral/${referral.id}/completion-data",
        returnType = object : ParameterizedTypeReference<ReferralCompletionData>() {},
      )

      assertThat(response).isNotNull
      assertThat(response.requirementId).isEqualTo(referral.eventId)
      assertThat(response.requirementCompletedAt).isNotNull()
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
      val session = scheduleService.scheduleIndividualSession(group.id!!, scheduleSessionRequest)

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
      val session = scheduleService.scheduleIndividualSession(group.id!!, scheduleSessionRequest)

      val attendee = session.attendees.find { it.referralId == referral.id }!!

      // Record first attendance as not attended
      val firstAttendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.UAAB,
            sessionNotes = "Did not attend first time",
          ),
        ),
      )
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${session.id}/attendance",
        body = firstAttendanceRequest,
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // Record second attendance as attended and complied
      val secondAttendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = SessionAttendanceNDeliusCode.ATTC,
            sessionNotes = "Attended and complied second time",
          ),
        ),
      )
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/${session.id}/attendance",
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
      assertThat(response.requirementId).isEqualTo(referral.eventId)
      assertThat(response.requirementCompletedAt).isNotNull()
    }
  }
}
