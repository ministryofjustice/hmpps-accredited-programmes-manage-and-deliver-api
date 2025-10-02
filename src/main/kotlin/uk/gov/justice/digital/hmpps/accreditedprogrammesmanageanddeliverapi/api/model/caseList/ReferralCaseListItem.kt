package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.util.UUID

data class ReferralCaseListItem(
  val referralId: UUID,
  val crn: String,
  val personName: String,
  val referralStatus: String,
  val cohort: OffenceCohort,
  val hasLdc: Boolean,
  val pdu: String,
  val reportingTeam: String,
)

fun ReferralCaseListItemViewEntity.toApi() = ReferralCaseListItem(
  referralId = referralId,
  crn = crn,
  personName = personName,
  referralStatus = status,
  cohort = OffenceCohort.valueOf(cohort),
  hasLdc = hasLdc,
  pdu = pduName,
  reportingTeam = reportingTeam,
)
