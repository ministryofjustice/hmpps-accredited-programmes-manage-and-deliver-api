package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SessionControllerIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @Autowired
  private lateinit var programmeGroupMembershipService: ProgrammeGroupMembershipService

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Test
  fun `retrieveSessionDetailsToEdit returns 200 and session details`() {
    // Given
    val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
    val module = testDataGenerator.createModule(programmeTemplate, "Test Module", 1)
    val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 2,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Test Session Template",
        durationMinutes = 120,
      ),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withCode("GROUPCODE")
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2026, 4, 23, 13, 30))
        .withEndsAt(LocalDateTime.of(2026, 4, 23, 14, 30))
        .produce(),
    )

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/bff/session/${session.id}/edit-session-date-and-time",
      object : ParameterizedTypeReference<EditSessionDetails>() {},
    )

    // Then
    assertThat(response.sessionId).isEqualTo(session.id)
    assertThat(response.groupCode).isEqualTo("GROUPCODE")
    assertThat(response.sessionName).isEqualTo("Test Module 2")
    assertThat(response.sessionDate).isEqualTo("23/4/2026")
    assertThat(response.sessionStartTime.hour).isEqualTo(1)
    assertThat(response.sessionStartTime.minutes).isEqualTo(30)
    assertThat(response.sessionStartTime.amOrPm).isEqualTo(AmOrPm.PM)
    assertThat(response.sessionEndTime.hour).isEqualTo(2)
    assertThat(response.sessionEndTime.minutes).isEqualTo(30)
    assertThat(response.sessionEndTime.amOrPm).isEqualTo(AmOrPm.PM)
  }

  @Test
  fun `getRescheduleSessionDetails returns 200 and reschedule details for group session`() {
    // Given
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Future me plan")

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withStartsAt(LocalDateTime.of(2026, 5, 21, 11, 0))
        .withEndsAt(LocalDateTime.of(2026, 5, 21, 13, 30))
        .produce(),
    )

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/bff/session/${session.id}/edit-session-date-and-time/reschedule",
      object : ParameterizedTypeReference<RescheduleSessionDetails>() {},
    )

    // Then
    assertThat(response.sessionId).isEqualTo(session.id)
    assertThat(response.sessionName).isEqualTo("Future me plan")
    assertThat(response.previousSessionDateAndTime).isEqualTo("Thursday 21 May 2026, 11am to 1:30pm")
  }

  @Test
  fun `getRescheduleSessionDetails returns 200 and reschedule details for group catch-up session`() {
    // Given
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Module skills practice")

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withIsCatchup(true)
        .withStartsAt(LocalDateTime.of(2026, 5, 21, 11, 0))
        .withEndsAt(LocalDateTime.of(2026, 5, 21, 13, 30))
        .produce(),
    )

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/bff/session/${session.id}/edit-session-date-and-time/reschedule",
      object : ParameterizedTypeReference<RescheduleSessionDetails>() {},
    )

    // Then
    assertThat(response.sessionName).isEqualTo("Module skills practice catch-up")
  }

  @Test
  fun `getRescheduleSessionDetails returns 200 and reschedule details for one-to-one session`() {
    // Given
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Managing myself one-to-one")

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withStartsAt(LocalDateTime.of(2026, 5, 21, 11, 0))
        .withEndsAt(LocalDateTime.of(2026, 5, 21, 13, 30))
        .produce(),
    )
    val referral = testDataGenerator.createReferral(
      personName = "John Doe",
      crn = "X123456",
    )
    testDataGenerator.createAttendee(referral, session)

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/bff/session/${session.id}/edit-session-date-and-time/reschedule",
      object : ParameterizedTypeReference<RescheduleSessionDetails>() {},
    )

    // Then
    assertThat(response.sessionName).isEqualTo("Managing myself one-to-one")
  }

  @Test
  fun `getRescheduleSessionDetails returns 200 and reschedule details for post-programme review`() {
    // Given
    val programmeTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Post programme review")

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withCode("RESCHR")
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withStartsAt(LocalDateTime.of(2026, 5, 21, 11, 0))
        .withEndsAt(LocalDateTime.of(2026, 5, 21, 13, 30))
        .produce(),
    )
    val referral = testDataGenerator.createReferral(
      personName = "Jane Smith",
      crn = "Y654321",
    )
    testDataGenerator.createAttendee(referral, session)

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/bff/session/${session.id}/edit-session-date-and-time/reschedule",
      object : ParameterizedTypeReference<RescheduleSessionDetails>() {},
    )

    // Then
    assertThat(response.sessionName).isEqualTo("Post programme review")
  }

  @Test
  fun `retrieveSessionDetailsToEdit returns 404 when session does not exist`() {
    // Given
    val nonExistentSessionId = UUID.randomUUID()
    // When & Then
    performRequestAndExpectStatusNoBody(
      HttpMethod.GET,
      "/bff/session/$nonExistentSessionId/edit-session-date-and-time",
      HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `retrieveSessionDetailsToEdit returns 403 when unauthorized`() {
    // Given
    val sessionId = UUID.randomUUID()
    // When & Then
    performRequestAndExpectStatusNoBody(
      HttpMethod.GET,
      "/bff/session/$sessionId/edit-session-date-and-time",
      HttpStatus.FORBIDDEN.value(),
      roles = listOf("ROLE_OTHER"),
    )
  }

  @Test
  fun `rescheduleSession returns 200 and updates session without rescheduling other sessions`() {
    // Given
    val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
    val module = testDataGenerator.createModule(programmeTemplate, "Test Module", 1)
    val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 2,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Test Session Template",
        durationMinutes = 120,
      ),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withCode("RESCHED")
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2026, 4, 23, 13, 30))
        .withEndsAt(LocalDateTime.of(2026, 4, 23, 14, 30))
        .produce(),
    )

    val rescheduleRequest = RescheduleSessionRequest(
      sessionStartDate = java.time.LocalDate.of(2026, 5, 24),
      sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      null,
      rescheduleOtherSessions = false,
    )

    // When
    val response = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/session/${session.id}/reschedule",
      body = rescheduleRequest,
      returnType = object : ParameterizedTypeReference<String>() {},
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response).isEqualTo("The date and time have been updated.")
    val updatedSession = sessionRepository.findById(session.id!!).get()
    assertThat(updatedSession.startsAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 0))
    assertThat(updatedSession.endsAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 11, 0))
  }

  @Test
  fun `rescheduleSession with rescheduleOtherSessions true updates subsequent group sessions`() {
    // Given
    val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
    val module = testDataGenerator.createModule(programmeTemplate, "Test Module", 1)
    val sessionTemplate1 = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 1,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Session 1",
        durationMinutes = 60,
      ),
    )
    val sessionTemplate2 = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 2,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Session 2",
        durationMinutes = 60,
      ),
    )
    val sessionTemplate3 = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 3,
        sessionType = SessionType.ONE_TO_ONE,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Session 3",
        durationMinutes = 60,
      ),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withCode("OTHERS")
        .produce(),
    )

    // Session 1: 2026-04-23 10:00 - 11:00
    val session1 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate1)
        .withStartsAt(LocalDateTime.of(2026, 4, 23, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 23, 11, 0))
        .produce(),
    )

    // Session 2: 2026-04-24 10:00 - 11:00 (Group)
    val session2 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate2)
        .withStartsAt(LocalDateTime.of(2026, 4, 24, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 24, 11, 0))
        .produce(),
    )

    // Session 3: 2026-04-25 10:00 - 11:00 (Individual)
    val session3 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate3)
        .withStartsAt(LocalDateTime.of(2026, 4, 25, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 25, 11, 0))
        .produce(),
    )

    // Reschedule Session 1 to be 1 hour later: 2026-04-23 11:00
    val rescheduleRequest = RescheduleSessionRequest(
      sessionStartDate = LocalDate.of(2026, 4, 23),
      sessionStartTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
      sessionEndTime = SessionTime(hour = 12, minutes = 0, amOrPm = AmOrPm.PM),
      rescheduleOtherSessions = true,
    )

    // When
    val response = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/session/${session1.id}/reschedule",
      body = rescheduleRequest,
      returnType = object : ParameterizedTypeReference<String>() {},
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response).isEqualTo("The date and time and schedule have been updated.")
    val updatedSession1 = sessionRepository.findById(session1.id!!).get()
    assertThat(updatedSession1.startsAt).isEqualTo(LocalDateTime.of(2026, 4, 23, 11, 0))
    assertThat(updatedSession1.endsAt).isEqualTo(LocalDateTime.of(2026, 4, 23, 12, 0))

    // Session 2 should be moved 1 hour later: 2026-04-24 11:00
    val updatedSession2 = sessionRepository.findById(session2.id!!).get()
    assertThat(updatedSession2.startsAt).isEqualTo(LocalDateTime.of(2026, 4, 24, 11, 0))
    assertThat(updatedSession2.endsAt).isEqualTo(LocalDateTime.of(2026, 4, 24, 12, 0))

    // Session 3 should NOT be moved as it is INDIVIDUAL
    val updatedSession3 = sessionRepository.findById(session3.id!!).get()
    assertThat(updatedSession3.startsAt).isEqualTo(LocalDateTime.of(2026, 4, 25, 10, 0))
    assertThat(updatedSession3.endsAt).isEqualTo(LocalDateTime.of(2026, 4, 25, 11, 0))
  }

  @Test
  fun `return 204 when the session is deleted`() {
    // Create group
    val group = testGroupHelper.createGroup()
    nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()

    // Allocate one referral to a group with 'Awaiting allocation' status to ensure it's not returned as part of our waitlist data
    val referral = testReferralHelper.createReferral()
    programmeGroupMembershipService.allocateReferralToGroup(
      referral.id!!,
      group.id!!,
      "SYSTEM",
      "",
    )

    val sessionId =
      sessionRepository.findByProgrammeGroupId(group.id!!).find { it.sessionType == SessionType.GROUP }?.id
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()

    performRequestAndExpectStatusNoBody(
      httpMethod = HttpMethod.DELETE,
      uri = "/session/$sessionId",
      expectedResponseStatus = HttpStatus.NO_CONTENT.value(),
    )

    val savedGroup = programmeGroupRepository.findByIdOrNull(group.id!!)
    assertThat(savedGroup!!.sessions.find { it.id == sessionId }).isNull()
  }

  @Test
  fun `return 404 when the session is not found on delete`() {
    val sessionId = UUID.randomUUID()

    val exception = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.DELETE,
      uri = "/session/$sessionId",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      body = {},
    )
    assertThat(exception.userMessage).isEqualTo("Not Found: Session with id $sessionId not found.")
  }

  @Test
  fun `return 401 when unauthorised on delete session`() {
    val sessionId = UUID.randomUUID()

    webTestClient
      .method(HttpMethod.DELETE)
      .uri("/session/$sessionId")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
