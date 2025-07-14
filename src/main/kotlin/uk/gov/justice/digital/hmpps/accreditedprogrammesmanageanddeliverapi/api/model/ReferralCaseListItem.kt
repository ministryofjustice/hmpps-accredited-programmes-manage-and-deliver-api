package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

data class ReferralCaseListItem(
  val crn: String,
  val personName: String,
  val referralStatus: String,
)

fun ReferralCaseListItemViewEntity.toApi() = ReferralCaseListItem(
  crn = crn,
  personName = personName,
  referralStatus = status,
)
