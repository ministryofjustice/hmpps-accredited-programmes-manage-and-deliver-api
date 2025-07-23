package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class OffenderIdentifiers(
  val crn: String,
  val name: FullName,
  val dateOfBirth: String,
  val age: String,
  val sex: CodeDescription,
  val ethnicity: CodeDescription,
  val probationPractitioner: ProbationPractitioner,
  val probationDeliveryUnit: ProbationDeliveryUnit,
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProbationPractitioner(
  val name: FullName,
  val code: String,
  val email: String,
)

data class ProbationDeliveryUnit(
  val code: String,
  val description: String,
)
