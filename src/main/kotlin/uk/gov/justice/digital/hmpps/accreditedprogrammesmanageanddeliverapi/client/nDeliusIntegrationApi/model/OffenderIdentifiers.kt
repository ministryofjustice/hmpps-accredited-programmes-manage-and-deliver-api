package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class OffenderIdentifiers(
  val crn: String,
  val nomsNumber: String?,
  val name: OffenderName,
  val dateOfBirth: String,
  val ethnicity: String?,
  val gender: String?,
  val probationDeliveryUnit: ProbationDeliveryUnit,
  val setting: String,
)

data class ProbationDeliveryUnit(
  val code: String?,
  val description: String,
)

data class OffenderName(
  val forename: String,
  val surname: String,
)
