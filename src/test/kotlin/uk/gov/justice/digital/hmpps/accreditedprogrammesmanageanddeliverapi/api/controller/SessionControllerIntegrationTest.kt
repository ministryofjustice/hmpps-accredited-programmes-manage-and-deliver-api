package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeleteSessionCaptionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RescheduleSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.UpdateSessionAttendeesRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.EditSessionAttendeesResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RescheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance.RecordSessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionDateAndTimeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session.EditSessionFacilitatorsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequestCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomAlphanumericString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPduWithTeamFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusRegionWithMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusUserTeamWithMembersFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UpdateAppointmentRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UpdateAppointmentsRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.CreateGroupTeamMemberFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

  @Autowired
  private lateinit var facilitatorRepository: FacilitatorRepository

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
    assertThat(response.sessionName).isEqualTo("Bringing it all together 1")
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
    assertThat(response.sessionName).isEqualTo("Managing people around me 6 catch-up")
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
    assertThat(response.sessionName).isEqualTo("John Doe: Managing myself one-to-one")
  }

  @Test
  fun `getRescheduleSessionDetails returns 200 and reschedule details for post-programme review`() {
    // Given
    val programmeTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Post-programme review")

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
    assertThat(response.sessionName).isEqualTo("Jane Smith: Post-programme review")
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
      sessionStartDate = LocalDate.of(2026, 5, 24),
      sessionStartTime = SessionTime(hour = 10, minutes = 0, amOrPm = AmOrPm.AM),
      null,
      rescheduleOtherSessions = false,
    )

    // When
    val response = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/session/${session.id}/reschedule",
      body = rescheduleRequest,
      returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response.message).isEqualTo("The date and time have been updated.")
    val updatedSession = sessionRepository.findById(session.id!!).get()
    assertThat(updatedSession.startsAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 0))
    assertThat(updatedSession.endsAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 11, 0))
  }

  @Test
  fun `rescheduleSession with rescheduleOtherSessions true updates subsequent group sessions`() {
    // Given
    stubAuthTokenEndpoint()
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
    val treatmentManager = testDataGenerator.createFacilitator(FacilitatorEntityFactory().produce())
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withTreatmentManager(treatmentManager)
        .produce(),
    )

    val referral = testDataGenerator.createReferral("John Smith", "X123456")

    // Session 1: 2026-04-23 10:00 - 11:00
    val session1 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate1)
        .withStartsAt(LocalDateTime.of(2026, 4, 23, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 23, 11, 0))
        .produce(),
    )
    val app1 = testDataGenerator.createNDeliusAppointment(session1, referral)

    // Session 2: 2026-04-24 10:00 - 11:00 (Group)
    val session2 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate2)
        .withStartsAt(LocalDateTime.of(2026, 4, 24, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 24, 11, 0))
        .produce(),
    )
    val app2 = testDataGenerator.createNDeliusAppointment(session2, referral)

    // Session 3: 2026-04-25 10:00 - 11:00 (Individual)
    val session3 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate3)
        .withStartsAt(LocalDateTime.of(2026, 4, 25, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 25, 11, 0))
        .produce(),
    )
    val app3 = testDataGenerator.createNDeliusAppointment(session3, referral)

    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

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
      returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(response.message).isEqualTo("The date and time and schedule have been updated.")
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

    // Verify ndeliusAppointments update request for Session 1
    val expectedUpdateRequest1 = UpdateAppointmentsRequestFactory()
      .withAppointments(
        listOf(
          UpdateAppointmentRequestFactory()
            .withReference(app1.ndeliusAppointmentId)
            .withDate(LocalDate.of(2026, 4, 23))
            .withStartTime(LocalTime.of(11, 0))
            .withEndTime(LocalTime.of(12, 0))
            .withLocation(RequestCode(group.deliveryLocationCode))
            .withStaff(RequestCode(group.treatmentManager!!.ndeliusPersonCode))
            .withTeam(RequestCode(group.treatmentManager!!.ndeliusTeamCode))
            .withNotes(null)
            .produce(),
        ),
      )
      .produce()
    nDeliusApiStubs.verifyPutAppointments(1, expectedUpdateRequest1)

    // Verify ndeliusAppointments update request for Session 2
    val expectedUpdateRequest2 = UpdateAppointmentsRequestFactory()
      .withAppointments(
        listOf(
          UpdateAppointmentRequestFactory()
            .withReference(app2.ndeliusAppointmentId)
            .withDate(LocalDate.of(2026, 4, 24))
            .withStartTime(LocalTime.of(11, 0))
            .withEndTime(LocalTime.of(12, 0))
            .withLocation(RequestCode(group.deliveryLocationCode))
            .withStaff(RequestCode(group.treatmentManager!!.ndeliusPersonCode))
            .withTeam(RequestCode(group.treatmentManager!!.ndeliusTeamCode))
            .withNotes(null)
            .produce(),
        ),
      )
      .produce()
    nDeliusApiStubs.verifyPutAppointments(1, expectedUpdateRequest2)
  }

  @Test
  fun `should reschedule individual session and update its nDelius appointment`() {
    // Given
    stubAuthTokenEndpoint()
    val programmeTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
    val sessionTemplate = moduleSessionTemplateRepository.findByName("Post-programme review")
    val treatmentManager = testDataGenerator.createFacilitator(
      FacilitatorEntityFactory()
        .withId(null)
        .produce(),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .withTreatmentManager(treatmentManager)
        .produce(),
    )

    val referral = testDataGenerator.createReferral("John Doe", "X123457")

    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withStartsAt(LocalDateTime.of(2026, 4, 23, 10, 0))
        .withEndsAt(LocalDateTime.of(2026, 4, 23, 11, 0))
        .produce(),
    )
    val app = testDataGenerator.createNDeliusAppointment(session, referral)

    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse()

    // Reschedule Session to be 1 hour later
    val rescheduleRequest = RescheduleSessionRequest(
      sessionStartDate = LocalDate.of(2026, 4, 23),
      sessionStartTime = SessionTime(hour = 11, minutes = 0, amOrPm = AmOrPm.AM),
      sessionEndTime = SessionTime(hour = 12, minutes = 0, amOrPm = AmOrPm.PM),
      rescheduleOtherSessions = false,
    )

    // When
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/session/${session.id}/reschedule",
      body = rescheduleRequest,
      returnType = object : ParameterizedTypeReference<EditSessionDateAndTimeResponse>() {},
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    val expectedUpdateRequest = UpdateAppointmentsRequestFactory()
      .withAppointments(
        listOf(
          UpdateAppointmentRequestFactory()
            .withReference(app.ndeliusAppointmentId)
            .withDate(LocalDate.of(2026, 4, 23))
            .withStartTime(LocalTime.of(11, 0))
            .withEndTime(LocalTime.of(12, 0))
            .withLocation(RequestCode(group.deliveryLocationCode))
            .withStaff(RequestCode(group.treatmentManager!!.ndeliusPersonCode))
            .withTeam(RequestCode(group.treatmentManager!!.ndeliusTeamCode))
            .withNotes(null)
            .produce(),
        ),
      )
      .produce()
    nDeliusApiStubs.verifyPutAppointments(1, expectedUpdateRequest)
  }

  @Test
  fun `return 200 when the session is deleted`() {
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

    val session =
      sessionRepository.findByProgrammeGroupId(group.id!!).find { it.sessionType == SessionType.GROUP }
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val sessionId = session?.id

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.DELETE,
      "session/$sessionId",
      object : ParameterizedTypeReference<DeleteSessionCaptionResponse>() {},
    )

    // Then
    assertThat(response.caption)
      .isEqualTo("${session?.sessionName} ${session?.sessionNumber} catch-up has been deleted.")

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

  @Test
  fun `should GET session details and return 200`() {
    // Given
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
    val sessionEntity =
      sessionRepository.findByProgrammeGroupId(group.id!!).find { it.sessionType == SessionType.GROUP }
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val sessionId = sessionEntity!!.id!!

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "bff/session/$sessionId",
      object : ParameterizedTypeReference<Session>() {},
    )

    // Then
    assertThat(response.id).isEqualTo(sessionId)
    assertThat(response.type).isEqualTo(sessionEntity.sessionType.value)
    assertThat(response.name).isEqualTo(sessionEntity.moduleSessionTemplate.module.name)
    assertThat(response.number).isEqualTo(sessionEntity.sessionNumber)
    assertThat(response.referrals).isNotEmpty()
    assertThat(response.referrals.size).isEqualTo(1)
    assertThat(response.referrals[0].personName).isEqualTo(sessionEntity.attendees[0].personName)
    assertThat(response.referrals[0].id).isEqualTo(sessionEntity.attendees[0].referral.id)
    assertThat(response.referrals[0].cohort).isNotNull()
    assertThat(response.referrals[0].crn).isEqualTo(sessionEntity.attendees[0].referral.crn)
    assertThat(response.referrals[0].createdAt).isNotNull()
    assertThat(response.referrals[0].status).isNotNull()
    assertThat(response.isCatchup).isEqualTo(sessionEntity.isCatchup)
  }

  @Test
  fun `should return 404 when a session is not found on GET request`() {
    // Given
    val sessionId = UUID.randomUUID()

    // When
    val exception = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.GET,
      uri = "bff/session/$sessionId",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      body = {},
    )

    // Then
    assertThat(exception.userMessage).isEqualTo("Not Found: Session with id $sessionId not found.")
  }

  @Test
  fun `should return 401 when unauthorised on GET session request`() {
    // Given
    val sessionId = UUID.randomUUID()

    // When
    webTestClient
      .method(HttpMethod.GET)
      .uri("bff/session/$sessionId")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }

  @Nested
  @DisplayName("Get session attendees /bff/session/{sessionId}/attendees")
  inner class GetEditSessionAttendees {
    private lateinit var session: SessionEntity
    private lateinit var referral1: ReferralEntity
    private lateinit var referral2: ReferralEntity
    private lateinit var group: ProgrammeGroupEntity

    @BeforeEach
    fun beforeEach() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findByName("Managing myself one-to-one")

      group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate!!)
          .withStartsAt(LocalDateTime.of(2026, 5, 21, 11, 0))
          .withEndsAt(LocalDateTime.of(2026, 5, 21, 13, 30))
          .produce(),
      )
      referral1 = testDataGenerator.createReferral(
        personName = "John Doe",
        crn = "X123456",
      )
      referral2 = testDataGenerator.createReferral(
        personName = "Alex River",
        crn = "X654321",
      )
    }

    @Test
    fun `should return list of attendees for session`() {
      // Given
      // Assign both referrals to a group but only add one attendee on the session
      testDataGenerator.allocateReferralsToGroup(listOf(referral1, referral2), group)
      testDataGenerator.createAttendee(referral1, session)
      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/session/${session.id}/attendees",
        object : ParameterizedTypeReference<EditSessionAttendeesResponse>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.attendees).isNotEmpty
      assertThat(response.attendees).extracting<Boolean> { it.currentlyAttending }.containsOnlyOnce(true)
    }

    @Test
    fun `should return NOT FOUND if session does not exist`() {
      // Given
      val sessionId = UUID.randomUUID()
      // When
      val response = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/session/$sessionId/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
    }

    @Test
    fun `should return NOT FOUND if group has no members `() {
      // Given
      // When
      val response = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/session/${session.id}/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Cannot get attendees as there are currently no members allocated to group with id: ${group.id}")
    }

    @Test
    fun `return 403 when unauthorised on get session attendees`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/session/${UUID.randomUUID()}/attendees")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("Update session attendees")
  inner class UpdateSessionAttendees {
    private lateinit var session: SessionEntity
    private lateinit var referral1: ReferralEntity
    private lateinit var referral2: ReferralEntity
    private lateinit var group: ProgrammeGroupEntity

    @BeforeEach
    fun beforeEach() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findByName("Managing myself one-to-one")

      group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate!!)
          .produce(),
      )
      referral1 = testDataGenerator.createReferral(
        personName = "John Doe",
        crn = "X123456",
      )
      referral2 = testDataGenerator.createReferral(
        personName = "Alex River",
        crn = "X654321",
      )
    }

    @Test
    fun `should update session attendees successfully`() {
      // Given
      val request = UpdateSessionAttendeesRequest(referralIdList = listOf(referral1.id!!, referral2.id!!))

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/${session.id}/attendees",
        object : ParameterizedTypeReference<String>() {},
        request,
        HttpStatus.OK.value(),
      )

      // Then
      assertThat(response).isEqualTo("The date and time have been updated.")
      val updatedSession = sessionRepository.findById(session.id!!).get()
      assertThat(updatedSession.attendees).hasSize(2)
      assertThat(updatedSession.attendees.map { it.referral.id }).containsExactlyInAnyOrder(referral1.id, referral2.id)
    }

    @Test
    fun `should return 404 if session does not exist`() {
      // Given
      val sessionId = UUID.randomUUID()
      val request = UpdateSessionAttendeesRequest(referralIdList = listOf(referral1.id!!))

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/$sessionId/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        request,
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
    }

    @Test
    fun `should return 404 if referral does not exist`() {
      // Given
      val referralId = UUID.randomUUID()
      val request = UpdateSessionAttendeesRequest(referralIdList = listOf(referralId))

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/${session.id}/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        request,
        HttpStatus.NOT_FOUND.value(),
      )

      // Then
      assertThat(response.userMessage).isEqualTo("Not Found: Referral not found with id: $referralId")
    }

    @Test
    fun `should return 403 when unauthorised on update session attendees`() {
      // Given
      val request = UpdateSessionAttendeesRequest(referralIdList = listOf(referral1.id!!))

      // When & Then
      webTestClient
        .method(HttpMethod.PUT)
        .uri("/session/${session.id}/attendees")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `should return 400 if referralIdList is empty`() {
      // Given
      val request = UpdateSessionAttendeesRequest(referralIdList = emptyList())

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/${session.id}/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        request,
        HttpStatus.BAD_REQUEST.value(),
      )

      // Then
      assertThat(response.userMessage).contains("Invalid value for parameter updateAttendeesRequest")
    }

    @Test
    fun `should return 400 if referralIdList is null`() {
      // Given
      val request = mapOf("referralIdList" to null)

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/${session.id}/attendees",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        request,
        HttpStatus.BAD_REQUEST.value(),
      )

      // Then
      assertThat(response.userMessage).contains("Bad request")
    }
  }

  @Nested
  @DisplayName("Get session facilitators /bff/session/{sessionId}/session-facilitators")
  inner class GetSessionFacilitators {
    private lateinit var group: ProgrammeGroupEntity

    @BeforeEach
    fun beforeEach() {
      val facilitators: List<CreateGroupTeamMember> =
        buildList {
          add(CreateGroupTeamMemberFactory().produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.TREATMENT_MANAGER))
          repeat(2) { add(CreateGroupTeamMemberFactory().produceWithRandomValues(teamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR)) }
        }
      group = testGroupHelper.createGroup(teamMembers = facilitators)
      nDeliusApiStubs.stubUserTeamsResponse(
        "AUTH_ADM",
        NDeliusUserTeams(
          teams = listOf(
            NDeliusUserTeam(
              code = "TEAM001",
              description = "Test Team 1",
              pdu = CodeDescription("PDU001", "Test PDU 1"),
              region = CodeDescription("WIREMOCKED_REGION", "WIREMOCKED REGION"),
            ),
          ),
        ),
      )
    }

    @Test
    fun `should return list of facilitators for the session and set currentlyFacilitating to true for facilitators already part of the group`() {
      // Given
      // Stub Ndelius Response with 2 facilitators already assigned to the group and one that is just pulled from the full list from Ndelius
      val groupFacilitators: MutableList<NDeliusRegionWithMembers.NDeliusUserTeamMembers> =
        group.groupFacilitators.map {
          NDeliusUserTeamMembersFactory().produce(
            code = it.facilitatorCode,
            name = it.facilitatorName.toFullName(),
          )
        }.toMutableList()
      groupFacilitators.add(
        NDeliusUserTeamMembersFactory().produce(
          code = randomAlphanumericString(),
          name = randomFullName(),
        ),
      )
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = groupFacilitators))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(
        pdus = listOf(pdu),
        code = "WIREMOCKED_REGION",
      )
      nDeliusApiStubs.stubRegionWithMembersResponse("WIREMOCKED_REGION", regionWithMembers)
      val sessionId = group.sessions.find { it.sessionType == SessionType.GROUP }!!.id!!

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/session/$sessionId/session-facilitators",
        object : ParameterizedTypeReference<EditSessionFacilitatorsResponse>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.facilitators).isNotEmpty
      // Only 1 facilitator that is not in the group currently
      assertThat(response.facilitators).extracting<Boolean> { it.currentlyFacilitating }.containsOnlyOnce(false)
    }

    @Test
    fun `should return page title in the correct format for a pre group session`() {
      // Given
      // Stub Ndelius Response with 2 facilitators already assigned to the group and one that is just pulled from the full list from Ndelius
      val groupFacilitators: MutableList<NDeliusRegionWithMembers.NDeliusUserTeamMembers> =
        group.groupFacilitators.map {
          NDeliusUserTeamMembersFactory().produce(
            code = it.facilitatorCode,
            name = it.facilitatorName.toFullName(),
          )
        }.toMutableList()
      groupFacilitators.add(
        NDeliusUserTeamMembersFactory().produce(
          code = randomAlphanumericString(),
          name = randomFullName(),
        ),
      )
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = groupFacilitators))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(
        pdus = listOf(pdu),
        code = "WIREMOCKED_REGION",
      )
      nDeliusApiStubs.stubRegionWithMembersResponse("WIREMOCKED_REGION", regionWithMembers)
      // Given
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findByName("Pre-group one-to-one")

      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate!!)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = testDataGenerator.createAttendee(referral, session)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/session/${session.id}/session-facilitators",
        object : ParameterizedTypeReference<EditSessionFacilitatorsResponse>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.pageTitle).isEqualTo("Edit ${attendee.personName}: Pre-group one-to-one")
    }

    @Test
    fun `should return page title in the correct format for a post programme review session`() {
      // Given
      // Stub Ndelius Response with 2 facilitators already assigned to the group and one that is just pulled from the full list from Ndelius
      val groupFacilitators: MutableList<NDeliusRegionWithMembers.NDeliusUserTeamMembers> =
        group.groupFacilitators.map {
          NDeliusUserTeamMembersFactory().produce(
            code = it.facilitatorCode,
            name = it.facilitatorName.toFullName(),
          )
        }.toMutableList()
      groupFacilitators.add(
        NDeliusUserTeamMembersFactory().produce(
          code = randomAlphanumericString(),
          name = randomFullName(),
        ),
      )
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = groupFacilitators))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(
        pdus = listOf(pdu),
        code = "WIREMOCKED_REGION",
      )
      nDeliusApiStubs.stubRegionWithMembersResponse("WIREMOCKED_REGION", regionWithMembers)
      // Given
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findByName("Post-programme review")

      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate!!)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = testDataGenerator.createAttendee(referral, session)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/session/${session.id}/session-facilitators",
        object : ParameterizedTypeReference<EditSessionFacilitatorsResponse>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.pageTitle).isEqualTo("Edit ${attendee.personName}: Post-programme review")
    }

    @Test
    fun `should return page title in the correct format for a non pre group or post programme review session`() {
      // Given
      // Stub Ndelius Response with 2 facilitators already assigned to the group and one that is just pulled from the full list from Ndelius
      val groupFacilitators: MutableList<NDeliusRegionWithMembers.NDeliusUserTeamMembers> =
        group.groupFacilitators.map {
          NDeliusUserTeamMembersFactory().produce(
            code = it.facilitatorCode,
            name = it.facilitatorName.toFullName(),
          )
        }.toMutableList()
      groupFacilitators.add(
        NDeliusUserTeamMembersFactory().produce(
          code = randomAlphanumericString(),
          name = randomFullName(),
        ),
      )
      val teams = listOf(NDeliusUserTeamWithMembersFactory().produce(members = groupFacilitators))
      val pdu = NDeliusPduWithTeamFactory().produce(team = teams)
      val regionWithMembers = NDeliusRegionWithMembersFactory().produce(
        pdus = listOf(pdu),
        code = "WIREMOCKED_REGION",
      )
      nDeliusApiStubs.stubRegionWithMembersResponse("WIREMOCKED_REGION", regionWithMembers)
      // Given
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findByName("Getting started one-to-one")

      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate!!)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = testDataGenerator.createAttendee(referral, session)

      // When
      val response = performRequestAndExpectOk(
        HttpMethod.GET,
        "/bff/session/${session.id}/session-facilitators",
        object : ParameterizedTypeReference<EditSessionFacilitatorsResponse>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.pageTitle).isEqualTo("Edit ${attendee.personName}: Getting started one-to-one")
    }

    @Test
    fun `should return NOT FOUND if session does not exist`() {
      // Given
      val sessionId = UUID.randomUUID()
      // When
      val response = performRequestAndExpectStatus(
        HttpMethod.GET,
        "/bff/session/$sessionId/session-facilitators",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
    }

    @Test
    fun `return 403 when unauthorised on get session facilitators`() {
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/session/${UUID.randomUUID()}/session-facilitators")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Nested
  @DisplayName("PUT session facilitators session/{sessionId}/session-facilitators")
  inner class PutSessionFacilitators {

    val facilitatorRequest = List(2) {
      EditSessionFacilitatorRequest(
        facilitatorName = randomFullName().getNameAsString(),
        facilitatorCode = randomAlphanumericString(),
        teamName = randomAlphanumericString(),
        teamCode = randomUppercaseString(),
      )
    }

    @Test
    fun `should update the list of facilitators on the session`() {
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

      assertThat(session.sessionFacilitators).isEmpty()

      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        uri = "session/${session.id}/session-facilitators",
        body = facilitatorRequest,
        returnType = object : ParameterizedTypeReference<String>() {},
        expectedResponseStatus = HttpStatus.OK.value(),
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response).isEqualTo("The people responsible for this session have been updated.")
      val savedSession = sessionRepository.findByIdOrNull(session.id!!)!!
      val sessionFacilitatorCodes = savedSession.sessionFacilitators.map { it.facilitatorCode }

      assertThat(savedSession.sessionFacilitators).hasSize(2)
      assertThat(sessionFacilitatorCodes).containsAll(facilitatorRequest.map { it.facilitatorCode })

      val savedFacilitatorEntities = facilitatorRepository.findAll()
      assertThat(sessionFacilitatorCodes).containsAll(savedFacilitatorEntities.map { it.ndeliusPersonCode })
    }

    @Test
    fun `should return 404 Not Found if session does not exist`() {
      // Given
      val sessionId = UUID.randomUUID()
      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/$sessionId/session-facilitators",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        facilitatorRequest,
        HttpStatus.NOT_FOUND.value(),
      )

      assertThat(response.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
    }

    @Test
    fun `should return 400 Bad Request if body is empty`() {
      // Given
      val sessionId = UUID.randomUUID()
      // When
      val response = performRequestAndExpectStatusWithBody(
        HttpMethod.PUT,
        "/session/$sessionId/session-facilitators",
        object : ParameterizedTypeReference<ErrorResponse>() {},
        listOf<String>(),
        HttpStatus.BAD_REQUEST.value(),
      )

      assertThat(response.userMessage).isEqualTo("Bad request: 400 BAD_REQUEST \"Validation failure\"")
    }

    @Test
    fun `return 403 Forbidden when unauthorised on get session facilitators`() {
      webTestClient
        .method(HttpMethod.PUT)
        .uri("/session/${UUID.randomUUID()}/session-facilitators")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .bodyValue(facilitatorRequest)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }
  }

  @Test
  fun `should POST session attendance request and return 201`() {
    // Given
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
    val sessionEntity =
      sessionRepository.findByProgrammeGroupId(group.id!!).find { it.sessionType == SessionType.GROUP }
    nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
    val sessionId = sessionEntity!!.id!!

    val sessionAttendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = sessionEntity.attendees.first().referralId,
          outcomeCode = "ATTC",
          sessionNotes = "Test session notes",
        ),
      ),
    )

    // When
    val response = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/$sessionId/attendance",
      body = sessionAttendanceRequest,
      returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    // Then
    assertThat(response.responseMessage).isEqualTo("Attendance saved for session $sessionId")
    val updatedSessionEntity = sessionRepository.findById(sessionId)
    assertThat(updatedSessionEntity.isPresent).isTrue()
    assertThat(updatedSessionEntity.get().attendances.size).isEqualTo(1)
    val sessionAttendanceEntity = updatedSessionEntity.get().attendances.first()
    assertThat(sessionAttendanceEntity.attended).isTrue()
    assertThat(sessionAttendanceEntity.recordedByFacilitator?.id)
      .isEqualTo(sessionEntity.sessionFacilitators.find { it.facilitatorType == FacilitatorType.LEAD_FACILITATOR }?.facilitator?.id)
    assertThat(sessionAttendanceEntity.recordedAt?.year).isEqualTo(LocalDate.now().year)
    assertThat(sessionAttendanceEntity.recordedAt?.month?.value).isEqualTo(LocalDate.now().month.value)
    assertThat(sessionAttendanceEntity.recordedAt?.dayOfMonth).isEqualTo(LocalDate.now().dayOfMonth)
    assertThat(sessionAttendanceEntity.notesHistory.first().notes).isEqualTo("Test session notes")
    assertThat(sessionAttendanceEntity.outcomeType.code).isEqualTo("ATTC")

    nDeliusApiStubs.verifyPutAppointments(
      1,
      UpdateAppointmentsRequest(
        appointments = listOf(
          UpdateAppointmentRequestFactory()
            .withReference(sessionEntity.ndeliusAppointments.first().ndeliusAppointmentId)
            .withDate(sessionEntity.startsAt.toLocalDate())
            .withStartTime(sessionEntity.startsAt.toLocalTime())
            .withEndTime(sessionEntity.endsAt.toLocalTime())
            .withOutcome(null)
            .withLocation(RequestCode(group.deliveryLocationCode))
            .withStaff(RequestCode(group.treatmentManager!!.ndeliusPersonCode))
            .withTeam(RequestCode(group.treatmentManager!!.ndeliusTeamCode))
            .withNotes("Test session notes")
            .withSensitive(false)
            .produce(),
        ),
      ),
    )
  }

  @Test
  fun `should return 404 when a session is not found on POST session attendance request`() {
    // Given
    val sessionId = UUID.randomUUID()
    val sessionAttendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = UUID.randomUUID(),
          outcomeCode = "ATTC",
        ),
      ),
    )

    // When
    val exception = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/session/$sessionId/attendance",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      body = sessionAttendanceRequest,
    )

    // Then
    assertThat(exception.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
  }

  @Test
  fun `should return 401 when unauthorised on POST session session attendance request`() {
    // Given
    val sessionId = UUID.randomUUID()
    val sessionAttendanceRequest = SessionAttendance(
      attendees = listOf(
        SessionAttendee(
          referralId = UUID.randomUUID(),
          outcomeCode = "ATTC",
        ),
      ),
    )

    // When
    webTestClient
      .method(HttpMethod.POST)
      .uri("/session/$sessionId/attendance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(sessionAttendanceRequest))
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }

  @Nested
  @DisplayName("Get record attendance")
  inner class GetRecordAttendancePage {
    @Test
    fun `should return 200 and data on GET record attendance by a session ID`() {
      // Given
      // Create group
      val group = testGroupHelper.createGroup()
      nDeliusApiStubs.stubSuccessfulPostAppointmentsResponse()
      // Allocate one referral to a group with 'Awaiting allocation'
      // status to ensure it's not returned as part of our waitlist data
      val referral = testReferralHelper.createReferral()
      programmeGroupMembershipService.allocateReferralToGroup(
        referral.id!!,
        group.id!!,
        "SYSTEM",
        "",
      )
      val session =
        sessionRepository.findByProgrammeGroupId(group.id!!).find { it.sessionType == SessionType.GROUP }
      nDeliusApiStubs.stubSuccessfulDeleteAppointmentsResponse()
      val sessionId = session!!.id!!
      val attendee = session.attendees.first()

      val sessionAttendanceRequest = SessionAttendance(
        attendees = listOf(
          SessionAttendee(
            referralId = attendee.referralId,
            outcomeCode = "ATTC",
          ),
        ),
      )

      // record attendance
      performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.POST,
        uri = "/session/$sessionId/attendance",
        body = sessionAttendanceRequest,
        returnType = object : ParameterizedTypeReference<SessionAttendance>() {},
        expectedResponseStatus = HttpStatus.CREATED.value(),
      )

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/session/${session.id}/record-attendance",
        returnType = object : ParameterizedTypeReference<RecordSessionAttendance>() {},
      )

      // Then
      assertThat(response.sessionTitle).isEqualTo(session.sessionName)
      assertThat(response.groupRegionName).isEqualTo(group.regionName)
      assertThat(response.people).isNotEmpty()
      assertThat(response.people[0].referralId).isEqualTo(attendee.referralId)
      assertThat(response.people[0].name).isEqualTo(attendee.personName)
      assertThat(response.people[0].crn).isEqualTo(attendee.referral.crn)
      assertThat(response.people[0].attendance).isEqualTo("Attended")
    }

    @Test
    fun `return 401 when unauthorised on GET record attendance by a session ID`() {
      // When
      webTestClient
        .method(HttpMethod.GET)
        .uri("/bff/session/${UUID.randomUUID()}/record-attendance")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
        .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
        .returnResult().responseBody!!
    }

    @Test
    fun `return 404 when the session is not found on GET record attendance by a session ID`() {
      // Given
      val sessionId = UUID.randomUUID()

      // When
      val exception = performRequestAndExpectStatusWithBody(
        httpMethod = HttpMethod.GET,
        uri = "/bff/session/$sessionId/record-attendance",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
        body = {},
      )

      // Then
      assertThat(exception.userMessage).isEqualTo("Not Found: Session not found with id: $sessionId")
    }
  }
}
