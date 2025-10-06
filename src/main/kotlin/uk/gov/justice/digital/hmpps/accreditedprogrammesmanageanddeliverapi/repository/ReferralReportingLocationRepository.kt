package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.PduReportingLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import java.util.UUID

interface ReferralReportingLocationRepository : JpaRepository<ReferralReportingLocationEntity, ReferralEntity> {
  fun findByReferralId(referralId: UUID?): ReferralReportingLocationEntity?
  fun findFirstByReferralId(referralId: UUID): ReferralReportingLocationEntity?

  @Query("SELECT r.pduName, r.reportingTeam FROM ReferralReportingLocationEntity r GROUP BY r.pduName, r.reportingTeam")
  fun getPdusAndReportingTeams(): List<PduReportingLocation>
}
