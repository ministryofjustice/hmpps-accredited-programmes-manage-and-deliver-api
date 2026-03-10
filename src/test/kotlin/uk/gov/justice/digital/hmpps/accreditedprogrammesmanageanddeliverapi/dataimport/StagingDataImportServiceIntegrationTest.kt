package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingIapsLicreqnosEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository.StagingIapsLicreqnosRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository.StagingReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository.StagingReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.StagingIapsLicreqnoEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.StagingReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.StagingReportingLocationEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DataImportRecordRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate

/**
 * Integration test for the staging table-based referral import functionality.
 * Tests that:
 * 1. Referrals are correctly created from staging table data
 * 2. DataImportRecords are created to track the imports
 * 3. Re-running the import is idempotent (no duplicate referrals created)
 * 4. Incremental imports correctly process only new rows
 */
@ActiveProfiles("test", "im-data-import")
class StagingDataImportServiceIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var stagingDataImportService: StagingDataImportService

  @Autowired
  private lateinit var stagingReferralRepository: StagingReferralRepository

  @Autowired
  private lateinit var stagingReportingLocationRepository: StagingReportingLocationRepository

  @Autowired
  private lateinit var stagingIapsLicreqnoRepository: StagingIapsLicreqnosRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var dataImportRecordRepository: DataImportRecordRepository

  @Autowired
  private lateinit var referralReportingLocationRepository: ReferralReportingLocationRepository
  companion object {
    private const val ENTITY_TYPE_REFERRAL = "REFERRAL"
  }

  @BeforeEach
  fun setUp() {
    testDataCleaner.cleanAllTables()
  }

  private fun insertStagingReferral(
    sourceReferralId: String,
    crn: String = "A123456B",
    firstName: String = "James",
    lastName: String = "Mitchell",
    createdAt: LocalDate = LocalDate.of(2024, 3, 15),
    sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.REQUIREMENT,
    sourcedFromId: String = "REQ-001",
    sex: String = "M",
    dateOfBirth: LocalDate = LocalDate.of(1985, 7, 22),
  ): StagingReferralEntity {
    val referral = StagingReferralEntityFactory()
      .withSourceReferralId(sourceReferralId)
      .withCrn(crn)
      .withFirstName(firstName)
      .withLastName(lastName)
      .withCreatedAt(createdAt)
      .withSourcedFrom(sourcedFrom)
      .withSourcedFromId(sourcedFromId)
      .withSex(sex)
      .withDateOfBirth(dateOfBirth)
      .produce()
    return stagingReferralRepository.save(referral)
  }

  private fun insertStagingReportingLocation(
    sourceReferralId: String,
    regionName: String = "London",
    pduName: String = "North London PDU",
    reportingTeamName: String = "Islington Probation Team",
  ): StagingReportingLocationEntity {
    val reportingLocation = StagingReportingLocationEntityFactory()
      .withSourceReferralId(sourceReferralId)
      .withRegionName(regionName)
      .withPduName(pduName)
      .withReportingTeamName(reportingTeamName)
      .produce()
    return stagingReportingLocationRepository.save(reportingLocation)
  }

  private fun insertIapsReqlicno(
    sourceReferralId: String,
    licenceConditionId: String = "LIC-001",
  ): StagingIapsLicreqnosEntity {
    val entity = StagingIapsLicreqnoEntityFactory()
      .withSourceReferralId(sourceReferralId)
      .withLicreqno(licenceConditionId)
      .produce()
      .also { stagingIapsLicreqnoRepository.save(it) }

    return entity
  }

  @Nested
  @DisplayName("When importing referrals from staging tables")
  inner class ImportReferralsFromStagingTables {
    @Test
    fun `should create referrals from staging data`() {
      // Given
      insertStagingReferral("IM-REF-001")
      insertStagingReportingLocation("IM-REF-001")
      insertIapsReqlicno("IM-REF-001")

      insertStagingReferral("IM-REF-002", crn = "B234567C", firstName = "Sarah", lastName = "Jones")
      insertStagingReportingLocation("IM-REF-002", regionName = "Manchester", pduName = "South Manchester PDU")
      insertIapsReqlicno("IM-REF-002")

      // When
      val result = stagingDataImportService.importReferralsFromStagingTables()

      // Then
      assertThat(result.skipped).isEqualTo(0)
      assertThat(result.created).isEqualTo(2)
      assertThat(referralRepository.count()).isEqualTo(2)
    }

    @Test
    fun `should create DataImportRecords for each imported referral`() {
      // Given
      insertStagingReferral("IM-REF-001")
      insertStagingReportingLocation("IM-REF-001")
      insertIapsReqlicno("IM-REF-001")

      insertStagingReferral("IM-REF-002", crn = "B234567C")
      insertStagingReportingLocation("IM-REF-002")
      insertIapsReqlicno("IM-REF-002")

      // When
      stagingDataImportService.importReferralsFromStagingTables()

      // Then
      val importRecords = dataImportRecordRepository.findAll()
      assertThat(importRecords).hasSize(2)
      assertThat(importRecords).allMatch { it.entityType == ENTITY_TYPE_REFERRAL }

      // Verify each import record has a valid source and target ID
      importRecords.forEach { record ->
        assertThat(record.sourceId).startsWith("IM-REF-")
        assertThat(record.targetId).isNotNull()
        assertThat(record.importedAt).isNotNull()

        val referral = referralRepository.findById(record.targetId)
        assertThat(referral).isPresent
        assertThat(referral.get().eventId).isEqualTo("LIC-001")
      }
    }

    @Test
    fun `should create referrals with correct data from staging tables`() {
      // Given
      insertStagingReferral(
        sourceReferralId = "IM-REF-001",
        crn = "A123456B",
        firstName = "James",
        lastName = "Mitchell",
        createdAt = LocalDate.of(2024, 3, 15),
        sourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION,
        sourcedFromId = "REQ-001",
        sex = "M",
        dateOfBirth = LocalDate.of(1985, 7, 22),
      )
      insertStagingReportingLocation("IM-REF-001")
      insertIapsReqlicno("IM-REF-001", "THE-REQLICNO")

      // When
      stagingDataImportService.importReferralsFromStagingTables()

      // Then
      val importRecord = dataImportRecordRepository.findByEntityTypeAndSourceId(ENTITY_TYPE_REFERRAL, "IM-REF-001")
      assertThat(importRecord).isNotNull

      val referral = referralRepository.findById(importRecord!!.targetId).get()
      assertThat(referral.crn).isEqualTo("A123456B")
      assertThat(referral.personName).isEqualTo("James Mitchell")
      assertThat(referral.sex).isEqualTo("M")
      assertThat(referral.dateOfBirth).isEqualTo(LocalDate.of(1985, 7, 22))
      assertThat(referral.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
      assertThat(referral.eventId).isEqualTo("THE-REQLICNO")
    }

    @Test
    fun `should create reporting locations for each referral`() {
      // Given
      insertStagingReferral("IM-REFERRAL-WITH-REPORTING-LOCATION")
      insertStagingReportingLocation(
        sourceReferralId = "IM-REFERRAL-WITH-REPORTING-LOCATION",
        regionName = "THE_REGION_NAME",
        pduName = "THE_PDU_NAME",
        reportingTeamName = "THE_REPORTING_TEAM_NAME",
      )
      insertIapsReqlicno("IM-REFERRAL-WITH-REPORTING-LOCATION")

      // When
      stagingDataImportService.importReferralsFromStagingTables()

      // Then
      val reportingLocations = referralReportingLocationRepository.findAll()
      assertThat(reportingLocations).hasSize(1)

      val importRecord = dataImportRecordRepository.findByEntityTypeAndSourceId(ENTITY_TYPE_REFERRAL, "IM-REFERRAL-WITH-REPORTING-LOCATION")
      val referral = referralRepository.findById(importRecord!!.targetId).get()
      val reportingLocation = reportingLocations.find { it.referral.id == referral.id }
      assertThat(reportingLocation).isNotNull
      assertThat(reportingLocation!!.regionName).isEqualTo("THE_REGION_NAME")
      assertThat(reportingLocation.pduName).isEqualTo("THE_PDU_NAME")
      assertThat(reportingLocation.reportingTeam).isEqualTo("THE_REPORTING_TEAM_NAME")
    }

    @Test
    fun `should skip referrals without reporting locations`() {
      // Given - referral without a reporting location
      insertStagingReferral("IM-REFERRAL-NO-REPORTING-LOCATION")
      insertIapsReqlicno("IM-REFERRAL-NO-REPORTING-LOCATION")

      // When
      val result = stagingDataImportService.importReferralsFromStagingTables()

      // Then
      assertThat(result.created).isEqualTo(0)
      assertThat(result.skipped).isEqualTo(1)
      assertThat(referralRepository.count()).isEqualTo(0)
    }

    @Test
    fun `should skip referrals without IAPS Requirement or Licence`() {
      // Given - referral without a reporting location
      insertStagingReferral("IM-REFERRAL-NO-REQ-LIC")
      insertStagingReportingLocation("IM-REFERRAL-NO-REQ-LIC")

      // When
      val result = stagingDataImportService.importReferralsFromStagingTables()

      // Then
      assertThat(result.created).isEqualTo(0)
      assertThat(result.skipped).isEqualTo(1)
      assertThat(referralRepository.count()).isEqualTo(0)
    }
  }

  @Nested
  @DisplayName("Idempotency and Incremental Imports")
  inner class IncrementalImport {
    @Test
    fun `should import only new rows when additional data is added`() {
      // Given (first run)
      insertStagingReferral("IM-REF-001", crn = "FIRST-REFERRAL-CRN")
      insertStagingReportingLocation("IM-REF-001")
      insertIapsReqlicno("IM-REF-001", "FIRST-REFERRAL-LIC-OR-REQ-NO")

      // When (first run)
      val firstResult = stagingDataImportService.importReferralsFromStagingTables()
      assertThat(firstResult.created).isEqualTo(1)
      assertThat(referralRepository.count()).isEqualTo(1)

      // Given (second run - add a new Referral)
      insertStagingReferral("IM-REF-002", crn = "SECOND-REFERRAL-CRN", firstName = "Sarah", lastName = "Jones")
      insertStagingReportingLocation("IM-REF-002", regionName = "Manchester")
      insertIapsReqlicno("IM-REF-002", "SECOND-REFERRAL-LIC-OR-REQ-NO")

      // When (second run)
      val secondResult = stagingDataImportService.importReferralsFromStagingTables()
      assertThat(secondResult.created).isEqualTo(1)
      assertThat(referralRepository.count()).isEqualTo(2)

      // Then (basic checks on the Referrals)
      val firstRunImportRecord = dataImportRecordRepository.findByEntityTypeAndSourceId(ENTITY_TYPE_REFERRAL, "IM-REF-001")
      assertThat(firstRunImportRecord).isNotNull
      val firstRunReferral = referralRepository.findById(firstRunImportRecord!!.targetId).get()
      assertThat(firstRunReferral.crn).isEqualTo("FIRST-REFERRAL-CRN")
      assertThat(firstRunReferral.eventId).isEqualTo("FIRST-REFERRAL-LIC-OR-REQ-NO")

      val secondRunImportRecord = dataImportRecordRepository.findByEntityTypeAndSourceId(ENTITY_TYPE_REFERRAL, "IM-REF-002")
      assertThat(secondRunImportRecord).isNotNull
      val secondRunReferral = referralRepository.findById(secondRunImportRecord!!.targetId).get()
      assertThat(secondRunReferral.crn).isEqualTo("SECOND-REFERRAL-CRN")
      assertThat(secondRunReferral.eventId).isEqualTo("SECOND-REFERRAL-LIC-OR-REQ-NO")
    }

    @Test
    fun `should handle multiple incremental imports correctly`() {
      // Given - first import
      insertStagingReferral("IM-REF-001")
      insertStagingReportingLocation("IM-REF-001")
      insertIapsReqlicno("IM-REF-001")
      stagingDataImportService.importReferralsFromStagingTables()

      // When - second import with one new row
      insertStagingReferral("IM-REF-002", crn = "B234567C")
      insertStagingReportingLocation("IM-REF-002")
      insertIapsReqlicno("IM-REF-002")
      val secondRun = stagingDataImportService.importReferralsFromStagingTables()
      assertThat(secondRun.created).isEqualTo(1)

      // When - third import with two more rows
      insertStagingReferral("IM-REF-003", crn = "C345678D")
      insertStagingReportingLocation("IM-REF-003")
      insertIapsReqlicno("IM-REF-003")

      insertStagingReferral("IM-REF-004", crn = "D456789E")
      insertStagingReportingLocation("IM-REF-004")
      insertIapsReqlicno("IM-REF-004")
      val thirdRun = stagingDataImportService.importReferralsFromStagingTables()

      // Then
      assertThat(thirdRun.created).isEqualTo(2)
      assertThat(referralRepository.count()).isEqualTo(4)
      assertThat(dataImportRecordRepository.count()).isEqualTo(4)
    }
  }
}
