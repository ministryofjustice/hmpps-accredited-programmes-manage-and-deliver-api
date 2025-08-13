package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.RsrScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Score
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.SexualPredictorScore
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

class OasysRiskPredictorScoresFactory {
  private var groupReconvictionScore: Score? = if (Random.nextBoolean()) ScoreFactory().produce() else null
  private var violencePredictorScore: Score? = if (Random.nextBoolean()) ScoreFactory().produce() else null
  private var generalPredictorScore: Score? = if (Random.nextBoolean()) ScoreFactory().produce() else null
  private var riskOfSeriousRecidivismScore: RsrScore? = if (Random.nextBoolean()) RsrScoreFactory().produce() else null
  private var sexualPredictorScore: SexualPredictorScore? =
    if (Random.nextBoolean()) SexualPredictorScoreFactory().produce() else null

  fun withGroupReconvictionScore(groupReconvictionScore: Score?) = apply { this.groupReconvictionScore = groupReconvictionScore }

  fun withViolencePredictorScore(violencePredictorScore: Score?) = apply { this.violencePredictorScore = violencePredictorScore }

  fun withGeneralPredictorScore(generalPredictorScore: Score?) = apply { this.generalPredictorScore = generalPredictorScore }

  fun withRiskOfSeriousRecidivismScore(riskOfSeriousRecidivismScore: RsrScore?) = apply { this.riskOfSeriousRecidivismScore = riskOfSeriousRecidivismScore }

  fun withSexualPredictorScore(sexualPredictorScore: SexualPredictorScore?) = apply { this.sexualPredictorScore = sexualPredictorScore }

  fun produce() = OasysRiskPredictorScores(
    groupReconvictionScore = this.groupReconvictionScore,
    violencePredictorScore = this.violencePredictorScore,
    generalPredictorScore = this.generalPredictorScore,
    riskOfSeriousRecidivismScore = this.riskOfSeriousRecidivismScore,
    sexualPredictorScore = this.sexualPredictorScore,
  )
}

class ScoreFactory {
  private var oneYear: BigDecimal? = if (Random.nextBoolean()) {
    BigDecimal.valueOf(Random.nextDouble(0.0, 100.0))
      .setScale(2, RoundingMode.HALF_UP)
  } else {
    null
  }
  private var twoYears: BigDecimal? = if (Random.nextBoolean()) {
    BigDecimal.valueOf(Random.nextDouble(0.0, 100.0))
      .setScale(2, RoundingMode.HALF_UP)
  } else {
    null
  }
  private var scoreLevel: String? =
    if (Random.nextBoolean()) listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random() else null

  fun withOneYear(oneYear: BigDecimal?) = apply { this.oneYear = oneYear }
  fun withTwoYears(twoYears: BigDecimal?) = apply { this.twoYears = twoYears }
  fun withScoreLevel(scoreLevel: String?) = apply { this.scoreLevel = scoreLevel }

  fun produce() = Score(
    oneYear = this.oneYear,
    twoYears = this.twoYears,
    scoreLevel = this.scoreLevel,
  )
}

class RsrScoreFactory {
  private var scoreLevel: String? =
    if (Random.nextBoolean()) listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random() else null
  private var percentageScore: BigDecimal? = if (Random.nextBoolean()) {
    BigDecimal.valueOf(Random.nextDouble(0.0, 100.0))
      .setScale(2, RoundingMode.HALF_UP)
  } else {
    null
  }

  fun withScoreLevel(scoreLevel: String?) = apply { this.scoreLevel = scoreLevel }
  fun withPercentageScore(percentageScore: BigDecimal?) = apply { this.percentageScore = percentageScore }

  fun produce() = RsrScore(
    scoreLevel = this.scoreLevel,
    percentageScore = this.percentageScore,
  )
}

class SexualPredictorScoreFactory {
  private var ospIndecentPercentageScore: BigDecimal? =
    if (Random.nextBoolean()) {
      BigDecimal.valueOf(Random.nextDouble(0.0, 100.0))
        .setScale(2, RoundingMode.HALF_UP)
    } else {
      null
    }
  private var ospContactPercentageScore: BigDecimal? =
    if (Random.nextBoolean()) {
      BigDecimal.valueOf(Random.nextDouble(0.0, 100.0))
        .setScale(2, RoundingMode.HALF_UP)
    } else {
      null
    }
  private var ospIndecentPercentageScoreLevel: String? =
    if (Random.nextBoolean()) listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random() else null
  private var ospContactPercentageScoreLevel: String? =
    if (Random.nextBoolean()) listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH").random() else null

  fun withOspIndecentPercentageScore(ospIndecentPercentageScore: BigDecimal?) = apply { this.ospIndecentPercentageScore = ospIndecentPercentageScore }

  fun withOspContactPercentageScore(ospContactPercentageScore: BigDecimal?) = apply { this.ospContactPercentageScore = ospContactPercentageScore }

  fun withOspIndecentPercentageScoreLevel(ospIndecentPercentageScoreLevel: String?) = apply { this.ospIndecentPercentageScoreLevel = ospIndecentPercentageScoreLevel }

  fun withOspContactPercentageScoreLevel(ospContactPercentageScoreLevel: String?) = apply { this.ospContactPercentageScoreLevel = ospContactPercentageScoreLevel }

  fun produce() = SexualPredictorScore(
    ospIndecentPercentageScore = this.ospIndecentPercentageScore,
    ospContactPercentageScore = this.ospContactPercentageScore,
    ospIndecentPercentageScoreLevel = this.ospIndecentPercentageScoreLevel,
    ospContactPercentageScoreLevel = this.ospContactPercentageScoreLevel,
  )
}
