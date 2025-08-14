package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val scoreLevel: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class RsrScore(
  val scoreLevel: String? = null,
  val percentageScore: BigDecimal? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class SexualPredictorScore(
  val ospIndecentPercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospIndecentPercentageScoreLevel: String? = null,
  val ospContactPercentageScoreLevel: String? = null,
)
