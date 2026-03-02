package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.UUID

class GroupWaitlistItemViewEntityFactory {
  private var referralId: UUID = UUID.randomUUID()
  private var crn: String = "X123456"
  private var personName: String = "John Smith"
  private var sentenceEndDate: LocalDate = LocalDate.now(UTC).plusYears(1)
  private var sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION
  private var cohort: String = "SEXUAL_OFFENCE"
  private var hasLdc: Boolean = true
  private var dateOfBirth: LocalDate = LocalDate.now(UTC).minusYears(20)
  private var sex: String = "Male"
  private var status: String = "Active"
  private var statusColour: String = "Red"
  private var pduName: String = "PDU1"
  private var reportingTeam: String = "TEAM_1"
  private var regionName: String = "REGION_1"
  private var activeProgrammeGroupId: UUID = UUID.randomUUID()

  fun withReferralId(referralId: UUID?) = apply { this.referralId }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withPersonName(personName: String) = apply { this.personName = personName }
  fun withSentenceEndDate(sentenceEndDate: LocalDate) = apply { this.sentenceEndDate = sentenceEndDate }
  fun withSourcedFrom(sourcedFrom: ReferralEntitySourcedFrom) = apply { this.sourcedFrom = sourcedFrom }
  fun withCohort(cohort: String) = apply { this.cohort = cohort }
  fun withHasLdc(hasLdc: Boolean) = apply { this.hasLdc = hasLdc }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }
  fun withSex(sex: String) = apply { this.sex = sex }
  fun withStatus(status: String) = apply { this.status = status }
  fun withStatusColour(statusColour: String) = apply { this.statusColour = statusColour }
  fun withPduName(pduName: String) = apply { this.pduName = pduName }
  fun withReportingTeam(reportingTeam: String) = apply { this.reportingTeam = reportingTeam }
  fun withRegionName(regionName: String) = apply { this.regionName = regionName }
  fun withActiveProgrammeGroupId(activeProgrammeGroupId: UUID) = apply { this.activeProgrammeGroupId = activeProgrammeGroupId }
  fun produce() = GroupWaitlistItemViewEntity(
    referralId = this.referralId,
    crn = this.crn,
    personName = this.personName,
    sentenceEndDate = this.sentenceEndDate,
    sourcedFrom = this.sourcedFrom,
    cohort = this.cohort,
    hasLdc = this.hasLdc,
    dateOfBirth = this.dateOfBirth,
    sex = this.sex,
    status = this.status,
    statusColour = this.statusColour,
    pduName = this.pduName,
    reportingTeam = this.reportingTeam,
    regionName = this.regionName,
    activeProgrammeGroupId = this.activeProgrammeGroupId,
  )
}
