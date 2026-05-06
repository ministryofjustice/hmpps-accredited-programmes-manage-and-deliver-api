package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity

data class SubjectAccessRequestReferralReportingLocation(
  val regionName: String,
  val pduName: String,
  val reportingTeam: String,
)

fun ReferralReportingLocationEntity.toApi() = SubjectAccessRequestReferralReportingLocation(
  regionName = regionName,
  pduName = pduName,
  reportingTeam = reportingTeam,
)
