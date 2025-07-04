package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model

import java.util.UUID

data class ReferralDetails(
  val interventionType: String,
  val interventionName: String,
  val personReference: String,
  val personReferenceType: String,
  val referralId: UUID,
  val setting: String,
)
