package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusPersonalDetails(
  val crn: String,
  val name: FullName,
  val dateOfBirth: String,
  val age: String,
  val sex: CodeDescription,
  val ethnicity: CodeDescription?,
  val probationPractitioner: ProbationPractitioner?,
  val probationDeliveryUnit: CodeDescription,
  val team: CodeDescription,
  val region: CodeDescription,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FullName(
  val forename: String,
  val middleNames: String? = null,
  val surname: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProbationPractitioner(
  val name: FullName,
  val code: String,
  val email: String? = null,
)

fun FullName.getNameAsString(): String = listOfNotNull(forename, middleNames, surname).filter { it.isNotBlank() }.joinToString(" ")

fun String.toFullName(): FullName {
  val parts = this.trim().split(Regex("\\s+"))

  return when {
    parts.isEmpty() -> throw IllegalArgumentException("Name cannot be empty")
    parts.size == 1 -> throw IllegalArgumentException("Name must contain at least forename and surname")
    parts.size == 2 -> FullName(
      forename = parts[0],
      surname = parts[1],
    )

    else -> FullName(
      forename = parts.first(),
      middleNames = parts.subList(1, parts.size - 1).joinToString(" "),
      surname = parts.last(),
    )
  }
}
