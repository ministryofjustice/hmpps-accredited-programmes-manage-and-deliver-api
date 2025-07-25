package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class PersonalDetails(
  val crn: String,
  val name: FullName,
  val dateOfBirth: String,
  val age: String,
  val sex: CodeDescription,
  val ethnicity: CodeDescription,
  val probationPractitioner: ProbationPractitioner,
  val probationDeliveryUnit: CodeDescription,
)

data class FullName(
  val forename: String,
  val middleNames: String,
  val surname: String,
)

data class CodeDescription(
  val code: String,
  val description: String,
)

data class ProbationPractitioner(
  val name: FullName,
  val code: String,
  val email: String,
)

fun FullName.getNameAsString(): String = listOfNotNull(forename, middleNames, surname).filter { it.isNotBlank() }.joinToString(" ")
