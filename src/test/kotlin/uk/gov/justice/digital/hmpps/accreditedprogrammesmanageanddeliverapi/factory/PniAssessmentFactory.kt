package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Osp
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Questions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.ScoredAnswer

class PniAssessmentFactory {
  private var id: Long = 10082385
  private var ldc: Ldc? = null
  private var ldcMessage: String? = null
  private var ogrs3Risk: RiskScoreLevel? = null
  private var ovpRisk: RiskScoreLevel? = null
  private var osp: Osp = Osp(null, null)
  private var rsrPercentage: Double? = null
  private var offenderAge: Int = 32
  private var questions: Questions = Questions(
    everCommittedSexualOffence = ScoredAnswer.YesNo.Unknown,
    openSexualOffendingQuestions = ScoredAnswer.YesNo.NO,
    sexualPreOccupation = ScoredAnswer.Problem.SIGNIFICANT,
    offenceRelatedSexualInterests = ScoredAnswer.Problem.SOME,
    emotionalCongruence = ScoredAnswer.Problem.SOME,
    proCriminalAttitudes = ScoredAnswer.Problem.SOME,
    hostileOrientation = ScoredAnswer.Problem.SOME,
    relCloseFamily = ScoredAnswer.Problem.NONE,
    prevCloseRelationships = ScoredAnswer.Problem.NONE,
    easilyInfluenced = ScoredAnswer.Problem.NONE,
    aggressiveControllingBehaviour = ScoredAnswer.Problem.NONE,
    impulsivity = ScoredAnswer.Problem.NONE,
    temperControl = ScoredAnswer.Problem.NONE,
    problemSolvingSkills = ScoredAnswer.Problem.NONE,
    difficultiesCoping = ScoredAnswer.Problem.MISSING,
  )

  fun withId(id: Long) = apply { this.id = id }
  fun withLdc(ldc: Ldc?) = apply { this.ldc = ldc }
  fun withLdcMessage(ldcMessage: String?) = apply { this.ldcMessage = ldcMessage }
  fun withOgrs3Risk(ogrs3Risk: RiskScoreLevel?) = apply { this.ogrs3Risk = ogrs3Risk }
  fun withOvpRisk(ovpRisk: RiskScoreLevel?) = apply { this.ovpRisk = ovpRisk }
  fun withOsp(osp: Osp) = apply { this.osp = osp }
  fun withRsrPercentage(rsrPercentage: Double?) = apply { this.rsrPercentage = rsrPercentage }
  fun withOffenderAge(offenderAge: Int) = apply { this.offenderAge = offenderAge }
  fun withQuestions(questions: Questions) = apply { this.questions = questions }

  fun produce() = PniAssessment(
    id = id,
    ldc = ldc,
    ldcMessage = ldcMessage,
    ogrs3Risk = ogrs3Risk,
    ovpRisk = ovpRisk,
    osp = osp,
    rsrPercentage = rsrPercentage,
    offenderAge = offenderAge,
    questions = questions,
  )
}
