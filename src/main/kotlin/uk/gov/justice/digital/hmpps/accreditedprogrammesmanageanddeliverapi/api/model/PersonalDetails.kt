package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDate

data class PersonalDetails(
  @Schema(
    example = "X933590",
    required = true,
    description = "The crn associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @Schema(
    example = "John Smith",
    required = true,
    description = "The full name of the person being referred.",
  )
  @get:JsonProperty("name", required = true)
  val name: String,

  @Schema(
    example = "15 March 1985",
    required = true,
    description = "The date of birth of the person being referred.",
  )
  @get:JsonProperty("dateOfBirth", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val dateOfBirth: LocalDate,

  @Schema(
    example = "White",
    required = false,
    description = "The ethnicity of the person being referred.",
  )
  @get:JsonProperty("ethnicity", required = true)
  val ethnicity: String? = null,

  @Schema(
    example = "38",
    required = true,
    description = "The age of the person being referred.",
  )
  @get:JsonProperty("age", required = true)
  val age: String,

  @Schema(
    example = "Male",
    required = true,
    description = "The gender of the person being referred.",
  )
  @get:JsonProperty("gender", required = true)
  val gender: String,

  @Schema(
    example = "Community",
    required = true,
    description = "The setting where the referral will be delivered.",
  )
  @get:JsonProperty("setting", required = true)
  val setting: SettingType,

  @Schema(
    example = "North London PDU",
    required = false,
    description = "The probation delivery unit responsible for this referral.",
  )
  @get:JsonProperty("probationDeliveryUnit", required = true)
  val probationDeliveryUnit: String? = null,

  @Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("dateRetrieved", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val dateRetrieved: LocalDate,
)

fun NDeliusPersonalDetails.toModel(setting: SettingType) = PersonalDetails(
  crn = crn,
  name = name.getNameAsString(),
  dateOfBirth = LocalDate.parse(dateOfBirth),
  ethnicity = ethnicity?.description,
  age = age,
  gender = sex.description,
  setting = setting,
  probationDeliveryUnit = probationDeliveryUnit.description,
  dateRetrieved = LocalDate.now(),
)
