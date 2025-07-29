package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class NDeliusPersonalDetails(
  val crn: String,
  val name: FullName,
  val dateOfBirth: String,
  val age: String,
  val sex: CodeDescription,
  val ethnicity: CodeDescription,
  val probationPractitioner: ProbationPractitioner,
  val probationDeliveryUnit: CodeDescription,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FullName(
  val forename: String,
  val middleNames: String? = null,
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
