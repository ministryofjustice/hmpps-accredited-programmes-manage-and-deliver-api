package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param activitiesEncourageOffending
 * @param lifestyleIssues
 * @param easilyInfluenced
 */
data class Lifestyle(

  @Schema(example = "Drug addiction")
  @get:JsonProperty("activitiesEncourageOffending") val activitiesEncourageOffending: String? = null,

  @Schema(example = "Commits robbery to fund drug addiction")
  @get:JsonProperty("lifestyleIssues") val lifestyleIssues: String? = null,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("easilyInfluenced") val easilyInfluenced: String? = null,
)
