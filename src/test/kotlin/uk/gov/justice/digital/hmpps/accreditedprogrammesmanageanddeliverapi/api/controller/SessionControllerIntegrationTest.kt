package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.EditSessionDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.util.UUID

class SessionControllerIntegrationTest : IntegrationTestBase() {

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
}
