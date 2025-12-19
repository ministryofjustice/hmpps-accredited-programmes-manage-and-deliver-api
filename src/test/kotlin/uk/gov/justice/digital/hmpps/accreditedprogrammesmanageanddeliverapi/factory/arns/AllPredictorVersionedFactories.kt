package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.arns

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.OspScoreDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4.BasePredictorDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4.StaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4.VersionedStaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.AssessmentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.RsrScoreSource
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreType
import java.math.BigDecimal
import java.time.LocalDateTime

class AllPredictorVersionedDtoFactory {
  private var completedDate: LocalDateTime? = LocalDateTime.now()
  private var status: AssessmentStatus? = AssessmentStatus.COMPLETE
  private var output: AllPredictorDto? = AllPredictorDtoFactory().produce()
  private var outputVersion: String = "2"

  fun withCompletedDate(completedDate: LocalDateTime?) = apply { this.completedDate = completedDate }
  fun withStatus(status: AssessmentStatus?) = apply { this.status = status }
  fun withOutput(output: AllPredictorDto?) = apply { this.output = output }
  fun withOutputVersion(outputVersion: String) = apply { this.outputVersion = outputVersion }

  fun produce() = AllPredictorVersionedDto(
    completedDate = this.completedDate,
    status = this.status,
    output = this.output,
    outputVersion = this.outputVersion,
  )
}

class AllPredictorDtoFactory {
  private var allReoffendingPredictor: StaticOrDynamicPredictorDto? = StaticOrDynamicPredictorDtoFactory().produce()
  private var violentReoffendingPredictor: StaticOrDynamicPredictorDto? = StaticOrDynamicPredictorDtoFactory().produce()
  private var seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = StaticOrDynamicPredictorDtoFactory().produce()
  private var directContactSexualReoffendingPredictor: BasePredictorDto? = BasePredictorDtoFactory().produce()
  private var indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = BasePredictorDtoFactory().produce()
  private var combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = VersionedStaticOrDynamicPredictorDtoFactory().produce()

  fun withAllReoffendingPredictor(value: StaticOrDynamicPredictorDto?) = apply { this.allReoffendingPredictor = value }
  fun withViolentReoffendingPredictor(value: StaticOrDynamicPredictorDto?) = apply { this.violentReoffendingPredictor = value }
  fun withSeriousViolentReoffendingPredictor(value: StaticOrDynamicPredictorDto?) = apply { this.seriousViolentReoffendingPredictor = value }
  fun withDirectContactSexualReoffendingPredictor(value: BasePredictorDto?) = apply { this.directContactSexualReoffendingPredictor = value }
  fun withIndirectImageContactSexualReoffendingPredictor(value: BasePredictorDto?) = apply { this.indirectImageContactSexualReoffendingPredictor = value }
  fun withCombinedSeriousReoffendingPredictor(value: VersionedStaticOrDynamicPredictorDto?) = apply { this.combinedSeriousReoffendingPredictor = value }

  fun produce() = AllPredictorDto(
    allReoffendingPredictor = this.allReoffendingPredictor,
    violentReoffendingPredictor = this.violentReoffendingPredictor,
    seriousViolentReoffendingPredictor = this.seriousViolentReoffendingPredictor,
    directContactSexualReoffendingPredictor = this.directContactSexualReoffendingPredictor,
    indirectImageContactSexualReoffendingPredictor = this.indirectImageContactSexualReoffendingPredictor,
    combinedSeriousReoffendingPredictor = this.combinedSeriousReoffendingPredictor,
  )
}

class StaticOrDynamicPredictorDtoFactory {
  private var staticOrDynamic: ScoreType? = ScoreType.STATIC
  private var score: BigDecimal? = BigDecimal.valueOf(34)
  private var band: ScoreLevel? = ScoreLevel.MEDIUM

  fun withStaticOrDynamic(value: ScoreType?) = apply { this.staticOrDynamic = value }
  fun withScore(value: BigDecimal?) = apply { this.score = value }
  fun withBand(value: ScoreLevel?) = apply { this.band = value }

  fun produce() = StaticOrDynamicPredictorDto(
    staticOrDynamic = this.staticOrDynamic,
    score = this.score,
    band = this.band,
  )
}

class VersionedStaticOrDynamicPredictorDtoFactory {
  private var algorithmVersion: String? = "1.0"
  private var staticOrDynamic: ScoreType? = ScoreType.STATIC
  private var score: BigDecimal? = BigDecimal.valueOf(7)
  private var band: ScoreLevel? = ScoreLevel.MEDIUM

  fun withAlgorithmVersion(value: String?) = apply { this.algorithmVersion = value }
  fun withStaticOrDynamic(value: ScoreType?) = apply { this.staticOrDynamic = value }
  fun withScore(value: BigDecimal?) = apply { this.score = value }
  fun withBand(value: ScoreLevel?) = apply { this.band = value }

  fun produce() = VersionedStaticOrDynamicPredictorDto(
    algorithmVersion = this.algorithmVersion,
    staticOrDynamic = this.staticOrDynamic,
    score = this.score,
    band = this.band,
  )
}

class BasePredictorDtoFactory {
  private var score: BigDecimal? = BigDecimal.valueOf(46)
  private var band: ScoreLevel? = ScoreLevel.MEDIUM

  fun withScore(value: BigDecimal?) = apply { this.score = value }
  fun withBand(value: ScoreLevel?) = apply { this.band = value }

  fun produce() = BasePredictorDto(
    score = this.score,
    band = this.band,
  )
}

class AllPredictorVersionedLegacyDtoFactory {
  private var completedDate: LocalDateTime? = LocalDateTime.now()
  private var status: AssessmentStatus? = AssessmentStatus.COMPLETE
  private var output: RiskScoresDto? = RiskScoresDtoFactory().produce()

  fun withCompletedDate(value: LocalDateTime?) = apply { this.completedDate = value }
  fun withStatus(value: AssessmentStatus?) = apply { this.status = value }
  fun withOutput(value: RiskScoresDto?) = apply { this.output = value }

  fun produce() = AllPredictorVersionedLegacyDto(
    completedDate = this.completedDate,
    status = this.status,
    output = this.output,
  )
}

class RiskScoresDtoFactory {
  private var completedDate: LocalDateTime? = LocalDateTime.now()
  private var assessmentStatus: String? = "COMPLETE"
  private var groupReconvictionScore: OgrScoreDto? = OgrScoreDtoFactory().produce()
  private var violencePredictorScore: OvpScoreDto? = OvpScoreDtoFactory().produce()
  private var generalPredictorScore: OgpScoreDto? = OgpScoreDtoFactory().produce()
  private var riskOfSeriousRecidivismScore: RsrScoreDto? = RsrScoreDtoFactory().produce()
  private var sexualPredictorScore: OspScoreDto? = OspScoreDtoFactory().produce()

  fun withCompletedDate(value: LocalDateTime?) = apply { this.completedDate = value }
  fun withAssessmentStatus(value: String?) = apply { this.assessmentStatus = value }
  fun withGroupReconvictionScore(value: OgrScoreDto?) = apply { this.groupReconvictionScore = value }
  fun withViolencePredictorScore(value: OvpScoreDto?) = apply { this.violencePredictorScore = value }
  fun withGeneralPredictorScore(value: OgpScoreDto?) = apply { this.generalPredictorScore = value }
  fun withRiskOfSeriousRecidivismScore(value: RsrScoreDto?) = apply { this.riskOfSeriousRecidivismScore = value }
  fun withSexualPredictorScore(value: OspScoreDto?) = apply { this.sexualPredictorScore = value }

  fun produce() = RiskScoresDto(
    completedDate = this.completedDate,
    assessmentStatus = this.assessmentStatus,
    groupReconvictionScore = this.groupReconvictionScore,
    violencePredictorScore = this.violencePredictorScore,
    generalPredictorScore = this.generalPredictorScore,
    riskOfSeriousRecidivismScore = this.riskOfSeriousRecidivismScore,
    sexualPredictorScore = this.sexualPredictorScore,
  )
}

class OgrScoreDtoFactory {
  private var oneYear: BigDecimal? = BigDecimal.valueOf(29)
  private var twoYears: BigDecimal? = BigDecimal.valueOf(42)
  private var scoreLevel: ScoreLevel? = ScoreLevel.MEDIUM

  fun withOneYear(value: BigDecimal?) = apply { this.oneYear = value }
  fun withTwoYears(value: BigDecimal?) = apply { this.twoYears = value }
  fun withScoreLevel(value: ScoreLevel?) = apply { this.scoreLevel = value }

  fun produce() = OgrScoreDto(
    oneYear = this.oneYear,
    twoYears = this.twoYears,
    scoreLevel = this.scoreLevel,
  )
}

class OgpScoreDtoFactory {
  private var ogpStaticWeightedScore: BigDecimal? = BigDecimal.valueOf(38)
  private var ogpDynamicWeightedScore: BigDecimal? = BigDecimal.valueOf(12)
  private var ogpTotalWeightedScore: BigDecimal? = BigDecimal.valueOf(46)
  private var ogp1Year: BigDecimal? = BigDecimal.valueOf(29)
  private var ogp2Year: BigDecimal? = BigDecimal.valueOf(39)
  private var ogpRisk: ScoreLevel? = ScoreLevel.MEDIUM

  fun withOgpStaticWeightedScore(value: BigDecimal?) = apply { this.ogpStaticWeightedScore = value }
  fun withOgpDynamicWeightedScore(value: BigDecimal?) = apply { this.ogpDynamicWeightedScore = value }
  fun withOgpTotalWeightedScore(value: BigDecimal?) = apply { this.ogpTotalWeightedScore = value }
  fun withOgp1Year(value: BigDecimal?) = apply { this.ogp1Year = value }
  fun withOgp2Year(value: BigDecimal?) = apply { this.ogp2Year = value }
  fun withOgpRisk(value: ScoreLevel?) = apply { this.ogpRisk = value }

  fun produce() = OgpScoreDto(
    ogpStaticWeightedScore = this.ogpStaticWeightedScore,
    ogpDynamicWeightedScore = this.ogpDynamicWeightedScore,
    ogpTotalWeightedScore = this.ogpTotalWeightedScore,
    ogp1Year = this.ogp1Year,
    ogp2Year = this.ogp2Year,
    ogpRisk = this.ogpRisk,
  )
}

class OvpScoreDtoFactory {
  private var ovpStaticWeightedScore: BigDecimal? = BigDecimal.valueOf(34)
  private var ovpDynamicWeightedScore: BigDecimal? = BigDecimal.valueOf(11)
  private var ovpTotalWeightedScore: BigDecimal? = BigDecimal.valueOf(45)
  private var oneYear: BigDecimal? = BigDecimal.valueOf(23)
  private var twoYears: BigDecimal? = BigDecimal.valueOf(36)
  private var ovpRisk: ScoreLevel? = ScoreLevel.MEDIUM

  fun withOvpStaticWeightedScore(value: BigDecimal?) = apply { this.ovpStaticWeightedScore = value }
  fun withOvpDynamicWeightedScore(value: BigDecimal?) = apply { this.ovpDynamicWeightedScore = value }
  fun withOvpTotalWeightedScore(value: BigDecimal?) = apply { this.ovpTotalWeightedScore = value }
  fun withOneYear(value: BigDecimal?) = apply { this.oneYear = value }
  fun withTwoYears(value: BigDecimal?) = apply { this.twoYears = value }
  fun withOvpRisk(value: ScoreLevel?) = apply { this.ovpRisk = value }

  fun produce() = OvpScoreDto(
    ovpStaticWeightedScore = this.ovpStaticWeightedScore,
    ovpDynamicWeightedScore = this.ovpDynamicWeightedScore,
    ovpTotalWeightedScore = this.ovpTotalWeightedScore,
    oneYear = this.oneYear,
    twoYears = this.twoYears,
    ovpRisk = this.ovpRisk,
  )
}

class RsrScoreDtoFactory {
  private var percentageScore: BigDecimal? = BigDecimal.valueOf(3.45)
  private var staticOrDynamic: ScoreType? = ScoreType.DYNAMIC
  private var source: RsrScoreSource = RsrScoreSource.OASYS
  private var algorithmVersion: String? = "4"
  private var scoreLevel: ScoreLevel? = null

  fun withPercentageScore(value: BigDecimal?) = apply { this.percentageScore = value }
  fun withStaticOrDynamic(value: ScoreType?) = apply { this.staticOrDynamic = value }
  fun withSource(value: RsrScoreSource) = apply { this.source = value }
  fun withAlgorithmVersion(value: String?) = apply { this.algorithmVersion = value }
  fun withScoreLevel(value: ScoreLevel?) = apply { this.scoreLevel = value }

  fun produce() = RsrScoreDto(
    percentageScore = this.percentageScore,
    staticOrDynamic = this.staticOrDynamic,
    source = this.source,
    algorithmVersion = this.algorithmVersion,
    scoreLevel = this.scoreLevel,
  )
}

class OspScoreDtoFactory {
  private var ospIndecentPercentageScore: BigDecimal? = BigDecimal.valueOf(0.11)
  private var ospContactPercentageScore: BigDecimal? = BigDecimal.valueOf(2)
  private var ospIndecentScoreLevel: ScoreLevel? = ScoreLevel.LOW
  private var ospContactScoreLevel: ScoreLevel? = ScoreLevel.HIGH
  private var ospIndirectImagePercentageScore: BigDecimal? = null
  private var ospDirectContactPercentageScore: BigDecimal? = null
  private var ospIndirectImageScoreLevel: ScoreLevel? = null
  private var ospDirectContactScoreLevel: ScoreLevel? = null

  fun withOspIndecentPercentageScore(value: BigDecimal?) = apply { this.ospIndecentPercentageScore = value }
  fun withOspContactPercentageScore(value: BigDecimal?) = apply { this.ospContactPercentageScore = value }
  fun withOspIndecentScoreLevel(value: ScoreLevel?) = apply { this.ospIndecentScoreLevel = value }
  fun withOspContactScoreLevel(value: ScoreLevel?) = apply { this.ospContactScoreLevel = value }
  fun withOspIndirectImagePercentageScore(value: BigDecimal?) = apply { this.ospIndirectImagePercentageScore = value }
  fun withOspDirectContactPercentageScore(value: BigDecimal?) = apply { this.ospDirectContactPercentageScore = value }
  fun withOspIndirectImageScoreLevel(value: ScoreLevel?) = apply { this.ospIndirectImageScoreLevel = value }
  fun withOspDirectContactScoreLevel(value: ScoreLevel?) = apply { this.ospDirectContactScoreLevel = value }

  fun produce() = OspScoreDto(
    ospIndecentPercentageScore = this.ospIndecentPercentageScore,
    ospContactPercentageScore = this.ospContactPercentageScore,
    ospIndecentScoreLevel = this.ospIndecentScoreLevel,
    ospContactScoreLevel = this.ospContactScoreLevel,
    ospIndirectImagePercentageScore = this.ospIndirectImagePercentageScore,
    ospDirectContactPercentageScore = this.ospDirectContactPercentageScore,
    ospIndirectImageScoreLevel = this.ospIndirectImageScoreLevel,
    ospDirectContactScoreLevel = this.ospDirectContactScoreLevel,
  )
}
