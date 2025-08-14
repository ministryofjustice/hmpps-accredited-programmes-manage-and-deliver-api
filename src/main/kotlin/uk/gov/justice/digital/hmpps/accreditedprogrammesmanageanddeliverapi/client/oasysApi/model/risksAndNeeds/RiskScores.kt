package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRiskPredictorScores(
  val groupReconvictionScore: Score? = null,
  val violencePredictorScore: Score? = null,
  val generalPredictorScore: Score? = null,
  val riskOfSeriousRecidivismScore: RsrScore? = null,
  val sexualPredictorScore: SexualPredictorScore? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class Score(
  @Schema(description = "One year prediction score", example = "0.75")
  val oneYear: BigDecimal? = null,
  @Schema(description = "Two year prediction score", example = "0.85")
  val twoYears: BigDecimal? = null,
  @Schema(description = "Risk level classification", example = "HIGH", allowableValues = ["LOW", "MEDIUM", "HIGH"])
  val scoreLevel: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class RsrScore(
  @Schema(example = "3.45", description = "Risk of Serious Recidivism score")
  val scoreLevel: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk of Serious Recidivism level",
    allowableValues = ["Low", "Medium", "High"],
  )
  val percentageScore: BigDecimal? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class SexualPredictorScore(
  val ospIndecentPercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospIndecentPercentageScoreLevel: String? = null,
  val ospContactPercentageScoreLevel: String? = null,
)
