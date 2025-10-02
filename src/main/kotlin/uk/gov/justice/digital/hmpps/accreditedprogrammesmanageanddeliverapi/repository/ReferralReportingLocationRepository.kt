package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import java.util.UUID

interface ReferralReportingLocationRepository : JpaRepository<ReferralReportingLocationEntity, ReferralEntity> {
  fun findFirstByReferralId(referralId: UUID): ReferralReportingLocationEntity?
}
