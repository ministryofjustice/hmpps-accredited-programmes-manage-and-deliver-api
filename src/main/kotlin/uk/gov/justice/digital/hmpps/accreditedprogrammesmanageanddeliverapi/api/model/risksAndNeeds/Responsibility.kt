package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Responsibility(
  @Schema(example = "false", description = "Whether the individual accepts responsibility for their offending.")
  @get:JsonProperty("acceptsResponsibility") val acceptsResponsibility: String?,

  @Schema(
    example = "Blames victims and circumstances",
    description = "Details about the individual's acceptance of responsibility.",
  )
  @get:JsonProperty("acceptsResponsibilityDetail") val acceptsResponsibilityDetail: String?,
)
