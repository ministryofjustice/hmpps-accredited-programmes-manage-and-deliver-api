package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.LdcNeedsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.util.UUID

data class ReferralCaseListItem(
  val referralId: UUID,
  val crn: String,
  val personName: String,
  val referralStatus: String,
  val cohort: OffenceCohort,
  val hasLdcNeeds: Boolean? = null,
  val ldcNeedsOverridden: Boolean? = null,
)

fun ReferralCaseListItemViewEntity.toApi(ldcNeeds: LdcNeedsEntity? = null) = ReferralCaseListItem(
  referralId = referralId,
  crn = crn,
  personName = personName,
  referralStatus = status,
  cohort = OffenceCohort.valueOf(cohort),
  hasLdcNeeds = ldcNeeds?.hasLdcNeeds,
  ldcNeedsOverridden = ldcNeeds?.overridden,
)
