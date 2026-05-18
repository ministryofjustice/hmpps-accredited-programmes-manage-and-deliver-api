package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.MaterializedViewRefresher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReportingGroupSizeTestDataHelper
import java.time.Instant
import java.time.LocalDate
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
  private lateinit var materializedViewRefresher: MaterializedViewRefresher

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should return group size reporting data in csv format with header and filename`() {
    // Given
    whenever(clock.instant()).thenReturn(Instant.parse("2026-05-18T12:30:00Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("Europe/London"))

    val group = ReportingGroupSizeTestDataHelper.createReportingGroup(
      referralRepository = referralRepository,
      facilitatorRepository = facilitatorRepository,
      programmeGroupRepository = programmeGroupRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      groupCode = "GROUP01",
      facilitatorStaffCode = "FAC123",
      createdAt = LocalDateTime.parse("2026-05-10T10:00:00"),
      earliestStartDate = LocalDate.parse("2026-05-20"),
    )

    materializedViewRefresher.refreshReportingGroupSizeView()

    // When & Then
    val csvBody = webTestClient
      .method(HttpMethod.GET)
      .uri("/reporting/group-size.csv?groupStartedSince=2026-05-01T00:00:00")
      .headers(setAuthorisation())
      .accept(MediaType.parseMediaType("text/csv"))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv"))
      .expectHeader().valueEquals(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"2026-05-18-13-30-manage-and-deliver-group-size.csv\"",
      )
      .expectBody<String>()
      .returnResult().responseBody!!

    val lines = csvBody.split("\n")
    assertThat(lines).hasSize(2)

    assertThat(lines.first()).isEqualTo(
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode",
    )
    assertThat(lines[1]).isEqualTo("${group.id},GROUP01,2026-05-10T10:00,MIXED,SEXUAL_OFFENCE,true,2026-05-20,North East,LDS01,Leeds PDU,LOC01,Leeds Office,1,FAC123")
  }

  @Disabled("Will be enabled when reporting role enforcement is added to this endpoint")
  @Test
  fun `should return unauthorized when user does not have reporting role`() {
    webTestClient
      .method(HttpMethod.GET)
      .uri("/reporting/group-size.csv?groupStartedSince=2026-05-01T00:00:00")
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.parseMediaType("text/csv"))
      .exchange()
      .expectStatus().isUnauthorized
  }
}
