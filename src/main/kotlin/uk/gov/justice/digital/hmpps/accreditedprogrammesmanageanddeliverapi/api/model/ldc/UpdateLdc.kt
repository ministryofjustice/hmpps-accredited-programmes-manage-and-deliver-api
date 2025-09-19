package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class UpdateLdc(
  @NotNull(message = "hasLdc must not be null")
  @get:JsonProperty("hasLdc", required = true)
  @Schema(
    example = "true",
    description = "The updated LDC status of the referral",
    allowableValues = ["true", "false"],
  )
  var hasLdc: Boolean,
)
