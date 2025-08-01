package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param ogrs3
 * @param ovp
 * @param ospDc
 * @param ospIic
 * @param rsr
 * @param sara
 */
data class RiskScore(

  @Schema(example = "High Risk", description = "")
  @get:JsonProperty("classification") val classification: String,

  @Schema(example = "2", description = "")
  @get:JsonProperty("IndividualRiskScores") val individualRiskScores: IndividualRiskScores,
)
