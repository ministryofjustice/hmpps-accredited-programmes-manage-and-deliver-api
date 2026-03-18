package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.UUID

class ReferralCaseListItemViewEntityFactory {
  private var referralId: UUID = UUID.randomUUID()
  private var crn: String = "X123456"
  private var personName: String = "John Smith"
  private var status: String = "Active"
  private var cohort: String = "SEXUAL_OFFENCE"
  private var hasLdc: Boolean = true
  private var pduName: String = "PDU1"
  private var reportingTeam: String = "TEAM_1"
  private var regionName: String = "REGION_1"
  private var sentenceEndDate: LocalDate = LocalDate.now(UTC).plusYears(1)
  private var sentenceEndDateSource: String = "LICENCE"

  fun withReferralId(referralId: UUID) = apply { this.referralId }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withPersonName(personName: String) = apply { this.personName = personName }
  fun withCohort(cohort: String) = apply { this.cohort = cohort }
  fun withStatus(status: String) = apply { this.status = status }
  fun withHasLdc(hasLdc: Boolean) = apply { this.hasLdc = hasLdc }
  fun withPduName(pduName: String) = apply { this.pduName = pduName }
  fun withReportingTeam(reportingTeam: String) = apply { this.reportingTeam = reportingTeam }
  fun withRegionName(regionName: String) = apply { this.regionName = regionName }
  fun withSentenceEndDate(sentenceEndDate: LocalDate) = apply { this.sentenceEndDate = sentenceEndDate }
  fun produce() = ReferralCaseListItemViewEntity(
    referralId = this.referralId,
    crn = this.crn,
    personName = this.personName,
    cohort = this.cohort,
    status = this.status,
    hasLdc = this.hasLdc,
    pduName = this.pduName,
    reportingTeam = this.reportingTeam,
    regionName = this.regionName,
    sentenceEndDate = this.sentenceEndDate,
    sentenceEndDateSource = this.sentenceEndDateSource,
  )
}
