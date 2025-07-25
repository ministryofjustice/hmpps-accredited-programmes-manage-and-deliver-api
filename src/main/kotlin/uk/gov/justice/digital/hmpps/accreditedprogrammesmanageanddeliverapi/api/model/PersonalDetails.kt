package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import java.time.LocalDate

data class PersonalDetails(
  val crn: String,
  val name: String,

  @JsonFormat(pattern = "d MMMM yyyy")
  val dateOfBirth: LocalDate,
  val ethnicity: String,
  val age: String,
  val gender: String,
  val setting: String,
  val probationDeliveryUnit: String,
)

fun NDeliusPersonalDetails.toModel(setting: String) = PersonalDetails(
  crn = crn,
  name = name.getNameAsString(),
  dateOfBirth = LocalDate.parse(dateOfBirth),
  ethnicity = ethnicity.description,
  age = age,
  gender = sex.description,
  setting = setting,
  probationDeliveryUnit = probationDeliveryUnit.description,
)
