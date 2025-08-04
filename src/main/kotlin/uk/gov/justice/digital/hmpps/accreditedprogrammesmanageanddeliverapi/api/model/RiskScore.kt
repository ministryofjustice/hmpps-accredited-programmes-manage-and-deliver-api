package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class RiskScore(

  @Schema(example = "High Risk", description = "classification associated with PNI Eg. HIGH_RISK, MEDIUM_RISK, LOW_RISK")
  @get:JsonProperty("classification") val classification: String,

  @Schema(example = "2", description = "")
  @get:JsonProperty("IndividualRiskScores") val individualRiskScores: IndividualRiskScores,
)
