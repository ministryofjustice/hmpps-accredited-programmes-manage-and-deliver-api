package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SexualPredictorScore

class RiskResponseFactory {
  private var sexualPredictorScore: SexualPredictorScore? = SexualPredictorScoreFactory().produce()

  fun withSexualPredictorScore(score: SexualPredictorScore?) = apply { this.sexualPredictorScore = score }

  fun produce() = RiskResponse(
    sexualPredictorScore = this.sexualPredictorScore,
  )
}
