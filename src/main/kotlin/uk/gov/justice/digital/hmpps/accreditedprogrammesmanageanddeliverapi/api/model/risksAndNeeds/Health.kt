package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 *
 * @param anyHealthConditions
 * @param description
 */
data class Health(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "Yes", description = "Does the person have any health condition. Can be Yes, empty or null")
  @get:JsonProperty("anyHealthConditions") val anyHealthConditions: Boolean? = false,

  @Schema(example = "Blind in one eye", description = "Description of the health condition")
  @get:JsonProperty("description") val description: String? = null,
)
