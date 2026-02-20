package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReferralEntity

interface StagingReferralRepository : JpaRepository<StagingReferralEntity, String> {
  /**
   * Fetches all staging referrals with their reporting locations, iaps requirement/licence numbers
   * eagerly loaded.
   */
  @Query(
    """
    SELECT sr FROM StagingReferralEntity sr
    LEFT JOIN FETCH sr.reportingLocation
    LEFT JOIN FETCH sr.iapsReqLicNos
    LEFT JOIN DataImportRecordEntity dir 
      ON dir.entityType = 'REFERRAL' AND dir.sourceId = sr.sourceReferralId
    WHERE dir.id IS NULL
    """,
  )
  fun findUnimportedReferralsWithReportingLocations(): List<StagingReferralEntity>
}
