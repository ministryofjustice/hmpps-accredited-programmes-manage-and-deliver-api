package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Session
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import java.time.LocalDateTime
import java.util.UUID

class SessionControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

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
        .withCode("AAAB1")
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
    assertThat(response.groupCode).isEqualTo("AAAB1")
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
    webTestClient
      .method(HttpMethod.GET)
      .uri("/bff/session/$sessionId/edit-session-date-and-time")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
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
}
