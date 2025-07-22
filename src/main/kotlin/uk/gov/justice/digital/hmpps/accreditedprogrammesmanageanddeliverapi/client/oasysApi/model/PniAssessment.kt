package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

data class PniAssessment(
  val id: Long,
  val ldc: Ldc?,
  val ldcMessage: String?,
  val ogrs3Risk: RiskScoreLevel?,
  val ovpRisk: RiskScoreLevel?,
  val osp: Osp?,
  val rsrPercentage: Double?,
  val offenderAge: Int,
  val questions: Questions,
)

enum class RiskScoreLevel(val type: String) {
  LOW("Low"),
  MEDIUM("Medium"),
  HIGH("High"),
  VERY_HIGH("Very High"),
  NOT_APPLICABLE("Not Applicable"),
}

data class Ldc(val score: Int, val subTotal: Int)
data class Osp(val cdc: RiskScoreLevel?, val iiic: RiskScoreLevel?)

data class Questions(
  val everCommittedSexualOffence: ScoredAnswer.YesNo,
  val openSexualOffendingQuestions: ScoredAnswer.YesNo?,
  val sexualPreOccupation: ScoredAnswer.Problem,
  val offenceRelatedSexualInterests: ScoredAnswer.Problem,
  val emotionalCongruence: ScoredAnswer.Problem,
  val proCriminalAttitudes: ScoredAnswer.Problem,
  val hostileOrientation: ScoredAnswer.Problem,
  val relCloseFamily: ScoredAnswer.Problem,
  val prevCloseRelationships: ScoredAnswer.Problem,
  val easilyInfluenced: ScoredAnswer.Problem,
  val aggressiveControllingBehaviour: ScoredAnswer.Problem,
  val impulsivity: ScoredAnswer.Problem,
  val temperControl: ScoredAnswer.Problem,
  val problemSolvingSkills: ScoredAnswer.Problem,
  val difficultiesCoping: ScoredAnswer.Problem,
)
