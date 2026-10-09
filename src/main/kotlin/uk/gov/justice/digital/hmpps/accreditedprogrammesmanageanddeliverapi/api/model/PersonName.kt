package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of a person's name that has been referred by a referral")
data class PersonName(
  @field:Schema(description = "The first name of a person", example = "John")
  val forename: String,

  @field:Schema(description = "The middle names of a person", example = "Oliver")
  val middleNames: String? = null,

  @field:Schema(description = "The surname of a person", example = "Smith")
  val surname: String,
)
