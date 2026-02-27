package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository.StagingIapsLicreqnosRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository.StagingReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DataImportRecordEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DataImportRecordRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.Instant

/**
 * THIS CODE WAS WRITTEN FOR A SPIKE ON THE MECHANISMS FOR IMPORTING IM DATA INTO M&D
 * IF THIS HAS MADE IT ONTO MAIN, SOMETHING HAS GONE HORRIBLY WRONG --TJWC 2026-02-17
 *
 * The primary service for importing data from the "staging tables" (i.e. data from IM, transformed
 * to be more in-line with M&D's data model) and creating the relevant entities in our own system.
 *
 * This service runs on start up (assuming the profile is active), but in production will run as a scheduled
 * job, e.g. orchestrated by Spring Batch.
 *
 * The import is idempotent — rows that have already been imported (tracked via data_import_record)
 *  are skipped on subsequent runs.
 *
 * Although it's in the main API service, for now we can quarantine it by its db schema (im_data_import)
 * and profile (im-data-import).
 */
@Service
@Profile("im-data-import")
class StagingDataImportService(
  private val stagingReferralRepository: StagingReferralRepository,
  private val iapsLicreqnosRepository: StagingIapsLicreqnosRepository,
  private val referralRepository: ReferralRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val dataImportRecordRepository: DataImportRecordRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val ENTITY_TYPE_REFERRAL = "REFERRAL"
  }

  @PostConstruct
  @Transactional
  fun importOnStartup() {
    log.info("Staging data import service starting — 'im-data-import' profile is active")
    doImportReferralsFromStagingTables()
  }

  /**
   * Imports Referrals from the staging tables.
   * Can be called multiple times safely due to idempotency checks.
   *
   * @return ImportResult containing counts of created and skipped referrals
   */
  @Transactional
  fun importReferralsFromStagingTables(): ImportResult = doImportReferralsFromStagingTables()

  private fun doImportReferralsFromStagingTables(): ImportResult {
    val unimportedReferrals = stagingReferralRepository.findUnimportedReferralsWithReportingLocations()

    log.info("Found ${unimportedReferrals.size} unimported referrals in staging tables")

    var createdCount = 0
    var skippedCount = 0

    for (stagingReferral in unimportedReferrals) {
      val sourceReferralId = stagingReferral.sourceReferralId

      // Idempotency
      if (dataImportRecordRepository.existsByEntityTypeAndSourceId(ENTITY_TYPE_REFERRAL, sourceReferralId)) {
        log.debug("Skipping already imported referral with source_referral_id: $sourceReferralId")
        skippedCount++
        continue
      }

      // Require a Reporting Location
      val reportingLocation = stagingReferral.reportingLocation
      if (reportingLocation == null) {
        log.warn("No reporting location found for source_referral_id: $sourceReferralId — skipping")
        skippedCount++
        continue
      }

      // Find a Requirement/Licence Number
      val requirementOrLicenceNumbers = iapsLicreqnosRepository.findBySourceReferralId(sourceReferralId)
      if (requirementOrLicenceNumbers.isEmpty()) {
        log.warn("No requirement or licence number found for source_referral_id: $sourceReferralId — skipping")
        skippedCount++
        continue
      } else if (requirementOrLicenceNumbers.size > 1) {
        log.warn("Multiple requirement or licence numbers found for source_referral_id: $sourceReferralId — skipping to avoid ambiguity")
        skippedCount++
        continue
      }

      // Create and persist the referral, reporting location
      val referral = createReferralFromStagingData(stagingReferral, requirementOrLicenceNumbers.first().licreqno)
      val savedReferral = referralRepository.save(referral)
      createReportingLocationFromStagingData(savedReferral, reportingLocation)

      // Create and persist the import record
      val importRecord = DataImportRecordEntity(
        entityType = ENTITY_TYPE_REFERRAL,
        sourceId = sourceReferralId,
        targetId = savedReferral.id!!,
        importedAt = Instant.now(),
      )
      dataImportRecordRepository.save(importRecord)

      log.debug("Created referral with ID ${savedReferral.id} from source_referral_id: $sourceReferralId")
      createdCount++
    }

    log.info("Staging data import complete: $createdCount referrals created, $skippedCount skipped (already imported or missing data)")
    return ImportResult(created = createdCount, skipped = skippedCount)
  }

  private fun createReferralFromStagingData(stagingReferral: StagingReferralEntity, reqlicno: String): ReferralEntity {
    val personName = "${stagingReferral.firstName} ${stagingReferral.lastName}".trim()

    val createdAt = stagingReferral.createdAt.atStartOfDay()

    val statusDescription = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()

    val referral = ReferralEntity(
      crn = stagingReferral.crn,
      personName = personName,
      interventionType = InterventionType.ACP,
      interventionName = "Building Choices",
      setting = SettingType.COMMUNITY,
      // TODO: We need to source this from a PNI calculation.  We should kick off a queued job to do this after referral creation
      cohort = OffenceCohort.GENERAL_OFFENCE,
      createdAt = createdAt,
      sourcedFrom = stagingReferral.sourcedFrom,
      eventId = reqlicno,
      // TODO: We Probably need to check if this is okay, likely need to make some requests to OASys and wait until we se 2xx
      eventNumber = 1,
      sex = stagingReferral.sex,
      dateOfBirth = stagingReferral.dateOfBirth,
    )

    // Add initial status history
    val statusHistory = ReferralStatusHistoryEntity(
      referral = referral,
      referralStatusDescription = statusDescription,
      createdBy = "INTERVENTIONS_MANAGER",
      createdAt = createdAt,
      startDate = createdAt,
    )
    referral.statusHistories.add(statusHistory)

    return referral
  }

  private fun createReportingLocationFromStagingData(
    referral: ReferralEntity,
    stagingReportingLocation: StagingReportingLocationEntity,
  ): ReferralReportingLocationEntity {
    val reportingLocation = ReferralReportingLocationEntity(
      referral = referral,
      regionName = stagingReportingLocation.regionName,
      pduName = stagingReportingLocation.pduName,
      reportingTeam = stagingReportingLocation.reportingTeamName,
    )

    return referralReportingLocationRepository.save(reportingLocation)
  }

  data class ImportResult(
    val created: Int,
    val skipped: Int,
  )
}
