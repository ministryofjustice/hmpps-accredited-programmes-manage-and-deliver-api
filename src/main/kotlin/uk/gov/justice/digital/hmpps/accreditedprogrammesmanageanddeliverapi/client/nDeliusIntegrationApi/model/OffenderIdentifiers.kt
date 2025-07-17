package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class OffenderIdentifiers(
  val crn: String,
  val name: OffenderFullName,
  val dateOfBirth: String,
  val age: String,
  val sex: CodeDescription,
  val ethnicity: CodeDescription,
  val probationPractitioner: ProbationPractitioner,
  val probationDeliveryUnit: ProbationDeliveryUnit,
)

data class OffenderFullName(
  val forename: String,
  val middleNames: String,
  val surname: String,
)

data class CodeDescription(
  val code: String,
  val description: String,
)

data class ProbationPractitioner(
  val name: OffenderFullName,
  val code: String,
  val email: String,
)

data class ProbationDeliveryUnit(
  val code: String,
  val description: String,
)
