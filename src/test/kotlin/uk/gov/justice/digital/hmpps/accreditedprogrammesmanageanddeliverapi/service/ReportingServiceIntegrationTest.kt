package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.MaterializedViewRefresher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReportingGroupSizeTestDataHelper
import java.time.LocalDate
import java.time.LocalDateTime

class ReportingServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var reportingService: ReportingService

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
  fun `should return group size report csv for groups with earliest start after provided datetime`() {
    ReportingGroupSizeTestDataHelper.createReportingGroup(
      referralRepository = referralRepository,
      facilitatorRepository = facilitatorRepository,
      programmeGroupRepository = programmeGroupRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      groupCode = "GROUP_BEFORE",
      facilitatorStaffCode = "FAC001",
      createdAt = LocalDateTime.parse("2026-05-05T09:00:00"),
      earliestStartDate = LocalDate.parse("2026-05-01"),
    )

    ReportingGroupSizeTestDataHelper.createReportingGroup(
      referralRepository = referralRepository,
      facilitatorRepository = facilitatorRepository,
      programmeGroupRepository = programmeGroupRepository,
      programmeGroupMembershipRepository = programmeGroupMembershipRepository,
      groupCode = "GROUP_AFTER",
      facilitatorStaffCode = "FAC002",
      createdAt = LocalDateTime.parse("2026-05-12T09:00:00"),
      earliestStartDate = LocalDate.parse("2026-05-20"),
    )

    materializedViewRefresher.refreshReportingGroupSizeView()

    val firstSessionAfter = LocalDateTime.parse("2026-05-10T00:00:00")

    val csv = reportingService.getGroupSizeReportCsv(firstSessionAfter)

    val lines = csv.split("\n")
    assertThat(lines).hasSize(2)
    assertThat(lines.first()).isEqualTo(
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode",
    )
    assertThat(lines[1]).contains("GROUP_AFTER")
    assertThat(lines[1]).contains("FAC002")
    assertThat(lines[1]).doesNotContain("GROUP_BEFORE")
  }
}
