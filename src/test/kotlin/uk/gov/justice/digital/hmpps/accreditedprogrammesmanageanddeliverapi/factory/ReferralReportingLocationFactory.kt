package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity

class ReferralReportingLocationFactory(referral: ReferralEntity? = null) {

  private var referral: ReferralEntity = referral ?: ReferralEntityFactory().produce()
  private var pduName: String = randomSentence(1..2)
  private var regionName: String = randomSentence(1..2)
  private var reportingTeam: String = randomSentence(1..2)

  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withPduName(pduName: String) = apply { this.pduName = pduName }
  fun withRegionName(regionName: String) = apply { this.regionName = regionName }
  fun withReportingTeam(reportingTeam: String) = apply { this.reportingTeam = reportingTeam }

  fun produce(): ReferralReportingLocationEntity = ReferralReportingLocationEntity(
    referral = referral,
    pduName = pduName,
    reportingTeam = reportingTeam,
    regionName = regionName,
  )
}
