package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.util.UUID

data class FindAndReferReferralDetails(
  val interventionType: String,
  val interventionName: String,
  val personReference: String,
  val personReferenceType: String,
  val referralId: UUID,
  val setting: String,
)

fun FindAndReferReferralDetails.toReferralEntity(
  statusHistories: MutableList<ReferralStatusHistoryEntity>,
  cohort: OffenceCohort,
) = ReferralEntity(
  crn = if (personReferenceType == "CRN") personReference else "UNKNOWN",
  interventionType = interventionType,
  interventionName = interventionName,
  setting = setting,
  personName = "UNKNOWN",
  statusHistories = statusHistories,
  cohort = cohort,
)

