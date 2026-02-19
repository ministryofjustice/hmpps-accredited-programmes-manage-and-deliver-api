package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingIapsLicreqnosEntity

interface StagingIapsLicreqnosRepository : JpaRepository<StagingIapsLicreqnosEntity, Long> {
  fun findBySourceReferralId(sourceReferralId: String): List<StagingIapsLicreqnosEntity>
}
