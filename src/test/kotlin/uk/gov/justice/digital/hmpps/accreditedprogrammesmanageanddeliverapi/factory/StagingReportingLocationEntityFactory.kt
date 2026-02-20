package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReportingLocationEntity

class StagingReportingLocationEntityFactory {
  private var sourceReferralId: String = "LS-REF-001"
  private var regionName: String = "London"
  private var pduName: String = "North London PDU"
  private var reportingTeamName: String = "Islington Probation Team"

  fun withSourceReferralId(sourceReferralId: String) = apply { this.sourceReferralId = sourceReferralId }
  fun withRegionName(regionName: String) = apply { this.regionName = regionName }
  fun withPduName(pduName: String) = apply { this.pduName = pduName }
  fun withReportingTeamName(reportingTeamName: String) = apply { this.reportingTeamName = reportingTeamName }

  fun produce() = StagingReportingLocationEntity(
    sourceReferralId = this.sourceReferralId,
    regionName = this.regionName,
    pduName = this.pduName,
    reportingTeamName = this.reportingTeamName,
  )
}
