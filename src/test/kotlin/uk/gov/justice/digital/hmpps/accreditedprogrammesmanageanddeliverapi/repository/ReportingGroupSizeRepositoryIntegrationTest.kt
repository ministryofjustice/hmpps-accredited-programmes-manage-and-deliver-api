package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.MaterializedViewRefresher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalDateTime

class ReportingGroupSizeRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repository: ReportingGroupSizeRepository

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

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should create reporting group size materialized view`() {
    val exists = jdbcTemplate.queryForObject(
      "SELECT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname = 'reporting_group_size')",
      Boolean::class.java,
    )

    assertThat(exists).isTrue()
  }

  @Test
  fun `should populate reporting group size view after refresh`() {
    val facilitator = facilitatorRepository.save(
      FacilitatorEntityFactory()
        .withNdeliusPersonCode("FAC123")
        .produce(),
    )

    val group = programmeGroupRepository.save(
      ProgrammeGroupFactory()
        .withCode("GROUP01")
        .withCreatedAt(LocalDateTime.now().minusDays(2))
        .withSex(ProgrammeGroupSexEnum.MIXED)
        .withCohort(OffenceCohort.SEXUAL_OFFENCE)
        .withIsLdc(true)
        .withRegionName("North East")
        .withProbationDeliveryUnit("Leeds PDU", "LDS01")
        .withDeliveryLocation("Leeds Office", "LOC01")
        .withEarliestStartDate(LocalDate.now().plusDays(14))
        .withTreatmentManager(facilitator)
        .produce(),
    )

    val referral = referralRepository.save(ReferralEntityFactory().produce())
    programmeGroupMembershipRepository.save(ProgrammeGroupMembershipFactory(referral, group).produce())

    materializedViewRefresher.refreshReportingGroupSizeView()

    val rows = repository.findAll()
    assertThat(rows).hasSize(1)

    val row = rows.single()
    assertThat(row.code).isEqualTo("GROUP01")
    assertThat(row.sex).isEqualTo(ProgrammeGroupSexEnum.MIXED)
    assertThat(row.cohort).isEqualTo(OffenceCohort.SEXUAL_OFFENCE)
    assertThat(row.isLdc).isTrue()
    assertThat(row.regionName).isEqualTo("North East")
    assertThat(row.pduCode).isEqualTo("LDS01")
    assertThat(row.pduName).isEqualTo("Leeds PDU")
    assertThat(row.locationCode).isEqualTo("LOC01")
    assertThat(row.locationName).isEqualTo("Leeds Office")
    assertThat(row.groupSize).isEqualTo(1)
    assertThat(row.facilitatorStaffCode).isEqualTo("FAC123")
  }
}
