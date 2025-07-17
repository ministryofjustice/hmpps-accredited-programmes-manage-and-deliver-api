package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.util.UUID

data class ReferralCaseListItem(
  val referralId: UUID,
  val crn: String,
  val personName: String,
  val referralStatus: String,
)

fun ReferralCaseListItemViewEntity.toApi() = ReferralCaseListItem(
  referralId = referralId,
  crn = crn,
  personName = personName,
  referralStatus = status,
)
