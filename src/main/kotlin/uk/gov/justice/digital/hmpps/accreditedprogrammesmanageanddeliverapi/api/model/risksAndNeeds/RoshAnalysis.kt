package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offenceDetails
 * @param whereAndWhen
 * @param howDone
 * @param whoVictims
 * @param anyoneElsePresent
 * @param whyDone
 * @param sources
 */
data class RoshAnalysis(

  @Schema(example = "Tax evasion")
  @get:JsonProperty("offenceDetails") val offenceDetails: String? = null,

  @Schema(example = "at home")
  @get:JsonProperty("whereAndWhen") val whereAndWhen: String? = null,

  @Schema(example = "false accounting")
  @get:JsonProperty("howDone") val howDone: String? = null,

  @Schema(example = "hmrc")
  @get:JsonProperty("whoVictims") val whoVictims: String? = null,

  @Schema(example = "company secretary")
  @get:JsonProperty("anyoneElsePresent") val anyoneElsePresent: String? = null,

  @Schema(example = "Greed")
  @get:JsonProperty("whyDone") val whyDone: String? = null,

  @Schema(example = "crown court")
  @get:JsonProperty("sources") val sources: String? = null,
)
