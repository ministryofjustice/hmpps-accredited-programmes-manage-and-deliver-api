package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.SessionAttendanceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReportingControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var facilitatorRepository: FacilitatorRepository

  @Autowired
  private lateinit var programmeGroupRepository: ProgrammeGroupRepository

  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var sessionAttendanceRepository: SessionAttendanceRepository

  @Autowired
  private lateinit var sessionAttendanceOutcomeTypeRepository: SessionAttendanceOutcomeTypeRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-21T08:42:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["group-size", "facilitator-continuity", "session-rate", "dosage"])
  fun `should return the report as a csv file named after the report`(reportName: String) {
    createGroup("GROUP01", regionName = "North East Region")

    val csv = getReport("/reporting/$reportName.csv?startDate=2026-05-01&endDate=2026-05-31")
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
      .expectHeader().valueEquals(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"$reportName-2026-05-21-09-42.csv\"",
      )
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThat(csv).contains("GROUP01")
  }

  @Test
  fun `should encode reports so that Excel can read apostrophes in session names`() {
    val bytes = getReport("/reporting/dosage.csv?startDate=2026-05-01&endDate=2026-05-31")
      .expectStatus().isOk
      .expectBody()
      .returnResult().responseBody!!

    assertThat(bytes.take(3)).containsExactly(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    assertThat(bytes.toString(Charsets.UTF_8)).contains("M4 S2 Exploring life’s problems")
  }

  @ParameterizedTest
  @ValueSource(strings = ["group-size", "facilitator-continuity", "session-rate", "dosage"])
  fun `should only return groups in the region when one is given`(reportName: String) {
    createGroup("NORTH_EAST", regionName = "North East Region")
    createGroup("WALES", regionName = "Wales")

    val csv = getReport("/reporting/$reportName.csv?startDate=2026-05-01&endDate=2026-05-31&region=North East Region")
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThat(csv).contains("NORTH_EAST").doesNotContain("WALES")
  }

  @ParameterizedTest
  @ValueSource(strings = ["group-size", "facilitator-continuity", "session-rate", "dosage"])
  fun `should return bad request when a date is missing`(reportName: String) {
    listOf("", "?startDate=2026-05-01", "?endDate=2026-05-31", "?region=Wales").forEach { query ->
      expectBadRequest("/reporting/$reportName.csv$query", "startDate and endDate must be provided")
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["group-size", "facilitator-continuity", "session-rate", "dosage"])
  fun `should return bad request when the end date is before the start date`(reportName: String) {
    expectBadRequest(
      "/reporting/$reportName.csv?startDate=2026-05-31&endDate=2026-05-01",
      "endDate must be on or after startDate",
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["group-size", "facilitator-continuity", "session-rate", "dosage"])
  fun `should return bad request when the region is not recognised`(reportName: String) {
    expectBadRequest(
      "/reporting/$reportName.csv?startDate=2026-05-01&endDate=2026-05-31&region=Narnia",
      "region must be one of: East Midlands Region, East of England",
    )
  }

  @Test
  fun `should treat a blank region as national`() {
    createGroup("NORTH_EAST", regionName = "North East Region")
    createGroup("WALES", regionName = "Wales")

    val csv = getReport("/reporting/group-size.csv?startDate=2026-05-01&endDate=2026-05-31&region=")
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThat(csv).contains("NORTH_EAST", "WALES")
  }

  @Disabled("Will be enabled when reporting role enforcement is added to this endpoint")
  @Test
  fun `should return unauthorized when user does not have reporting role`() {
    webTestClient.get()
      .uri("/reporting/group-size.csv?startDate=2026-05-01&endDate=2026-05-31")
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.parseMediaType("text/csv"))
      .exchange()
      .expectStatus().isUnauthorized
  }

  private fun getReport(uri: String): WebTestClient.ResponseSpec = webTestClient.get()
    .uri(uri)
    .headers(setAuthorisation())
    .accept(MediaType.parseMediaType("text/csv"))
    .exchange()

  private fun expectBadRequest(uri: String, message: String) {
    getReport(uri)
      .expectStatus().isBadRequest
      .expectBody<String>().value { body -> assertThat(body).contains(message) }
  }

  private fun createGroup(code: String, regionName: String) {
    val facilitator = facilitatorRepository.save(FacilitatorEntityFactory().produce())
    val group = programmeGroupRepository.save(
      ProgrammeGroupFactory().withCode(code).withRegionName(regionName).withTreatmentManager(facilitator).produce(),
    )
    val referral = referralRepository.save(ReferralEntityFactory().withInterventionName("Building Choices").produce())
    val membership = programmeGroupMembershipRepository.save(
      ProgrammeGroupMembershipFactory(referral, group).withCreatedAt(LocalDateTime.parse("2026-05-01T09:00:00")).produce(),
    )
    val session = sessionRepository.save(
      SessionFactory(group, moduleSessionTemplateRepository.findByName("Introduction to Building Choices"))
        .withStartsAt(LocalDateTime.parse("2026-05-11T10:00:00"))
        .withEndsAt(LocalDateTime.parse("2026-05-11T12:30:00"))
        .produce(),
    )
    testDataGenerator.createAttendee(referral, session)
    sessionAttendanceRepository.save(
      SessionAttendanceEntityFactory(session, membership)
        .withRecordedByFacilitator(facilitator)
        .withOutcomeType(sessionAttendanceOutcomeTypeRepository.findByCode(SessionAttendanceNDeliusCode.ATTC)!!)
        .produce(),
    )
  }
}
