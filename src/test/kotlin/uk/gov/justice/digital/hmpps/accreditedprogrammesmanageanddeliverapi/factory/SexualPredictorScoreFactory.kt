package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SexualPredictorScore

class SexualPredictorScoreFactory {
  private var ospIndecentPercentageScore: Int = 12
  private var ospContactPercentageScore: Int = 23
  private var ospIndirectImagePercentageScore: Int = 7
  private var ospDirectContactPercentageScore: Int = 18

  fun withOspIndecentPercentageScore(score: Int) = apply { this.ospIndecentPercentageScore = score }
  fun withOspContactPercentageScore(score: Int) = apply { this.ospContactPercentageScore = score }
  fun withOspIndirectImagePercentageScore(score: Int) = apply { this.ospIndirectImagePercentageScore = score }
  fun withOspDirectContactPercentageScore(score: Int) = apply { this.ospDirectContactPercentageScore = score }

  fun produce() = SexualPredictorScore(
    ospIndecentPercentageScore,
    ospContactPercentageScore,
    ospIndirectImagePercentageScore,
    ospDirectContactPercentageScore,
  )
}
