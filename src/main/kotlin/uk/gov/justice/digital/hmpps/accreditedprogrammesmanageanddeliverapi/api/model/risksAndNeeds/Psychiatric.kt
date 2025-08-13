package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param description
 * @param difficultiesCoping
 * @param currPsychologicalProblems
 * @param selfHarmSuicidal
 */
data class Psychiatric(

  @Schema(example = "0-No problems")
  @get:JsonProperty("description") val description: String? = null,

  @Schema(example = "null")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: String? = null,

  @Schema(example = "null")
  @get:JsonProperty("currPsychologicalProblems") val currPsychologicalProblems: String? = null,

  @Schema(example = "null")
  @get:JsonProperty("selfHarmSuicidal") val selfHarmSuicidal: String? = null,
)
