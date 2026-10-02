package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDate

data class PersonalDetails(
  @field:Schema(
    example = "X933590",
    required = true,
    description = "The crn associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @field:Schema(
    example = "John Smith",
    required = true,
    description = "The full name of the person being referred.",
  )
  @get:JsonProperty("name", required = true)
  @param:JsonProperty("name")
  private val _name: String,

  @field:Schema(
    example = "John",
    required = false,
    description = "The forename of the person being referred.",
  )
  @get:JsonProperty("personForename", required = false)
  val personForename: String? = null,

  @field:Schema(
    example = "Smith",
    required = false,
    description = "The surname of the person being referred.",
  )
  @get:JsonProperty("personSurname", required = false)
  val personSurname: String? = null,

  @field:Schema(
    example = "William",
    required = false,
    description = "The middle names of the person being referred.",
  )
  @get:JsonProperty("personMiddleNames", required = false)
  val personMiddleNames: String? = null,

  @field:Schema(
    example = "15 March 1985",
    required = true,
    description = "The date of birth of the person being referred.",
  )
  @get:JsonProperty("dateOfBirth", required = true)
  @get:JsonFormat(pattern = "d MMMM yyyy")
  val dateOfBirth: LocalDate,

  @field:Schema(
    example = "White",
    required = false,
    description = "The ethnicity of the person being referred.",
  )
  @get:JsonProperty("ethnicity", required = true)
  val ethnicity: String? = null,

  @field:Schema(
    example = "38",
    required = true,
    description = "The age of the person being referred.",
  )
  @get:JsonProperty("age", required = true)
  val age: String,

  @field:Schema(
    example = "Male",
    required = true,
    description = "The gender of the person being referred.",
  )
  @get:JsonProperty("gender", required = true)
  val gender: String,

  @field:Schema(
    example = "Community",
    required = true,
    description = "The setting where the referral will be delivered.",
  )
  @get:JsonProperty("setting", required = true)
  val setting: SettingType,

  @field:Schema(
    example = "North London PDU",
    required = false,
    description = "The probation delivery unit responsible for this referral.",
  )
  @get:JsonProperty("probationDeliveryUnit", required = true)
  val probationDeliveryUnit: String? = null,

  @field:Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("dateRetrieved", required = true)
  @get:JsonFormat(pattern = "d MMMM yyyy")
  val dateRetrieved: LocalDate,
) {
  @get:Schema(
    example = "John Smith",
    required = true,
    description = "The full name of the person being referred.",
  )
  @get:JsonProperty("name", required = true)
  val name: String
    get() = if (personForename != null && personSurname != null) {
      listOfNotNull(personForename, personMiddleNames, personSurname).filter { it.isNotBlank() }.joinToString(" ")
    } else {
      _name
    }
}

fun NDeliusPersonalDetails.toModel(setting: SettingType) = PersonalDetails(
  crn = crn,
  _name = name.getNameAsString(),
  personForename = name.forename,
  personSurname = name.surname,
  personMiddleNames = name.middleNames,
  dateOfBirth = LocalDate.parse(dateOfBirth),
  ethnicity = ethnicity?.description,
  age = age,
  gender = sex.description,
  setting = setting,
  probationDeliveryUnit = probationDeliveryUnit.description,
  dateRetrieved = LocalDate.now(),
)
