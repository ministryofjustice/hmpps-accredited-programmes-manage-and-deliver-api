package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SessionControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

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
}
