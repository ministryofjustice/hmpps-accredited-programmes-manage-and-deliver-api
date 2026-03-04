package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity

data class SubjectAccessRequestReferralReportingLocation(
  val reportingTeam: String,
)

fun ReferralReportingLocationEntity.toApi() = SubjectAccessRequestReferralReportingLocation(
  reportingTeam = reportingTeam,
)
