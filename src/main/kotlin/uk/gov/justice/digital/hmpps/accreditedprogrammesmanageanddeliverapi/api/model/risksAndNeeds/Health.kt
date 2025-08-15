package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param anyHealthConditions
 * @param description
 */
data class Health(

  @Schema(example = "null", description = "")
  @get:JsonProperty("anyHealthConditions") val anyHealthConditions: Boolean? = false,

  @Schema(example = "Blind in one eye", description = "")
  @get:JsonProperty("description") val description: String? = null,
)
