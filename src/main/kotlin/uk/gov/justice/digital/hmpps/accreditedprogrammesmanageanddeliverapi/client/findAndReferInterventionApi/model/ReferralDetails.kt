package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.util.UUID

data class ReferralDetails(
  val interventionType: String,
  val interventionName: String,
  val personReference: String,
  val personReferenceType: String,
  val referralId: UUID,
  val setting: String,
)

fun ReferralDetails.toReferralEntity() = ReferralEntity(
  crn = if (personReferenceType == "CRN") personReference else "UNKNOWN",
  interventionType = interventionType,
  interventionName = interventionName,
  setting = setting,
  personName = "UNKNOWN",
)
