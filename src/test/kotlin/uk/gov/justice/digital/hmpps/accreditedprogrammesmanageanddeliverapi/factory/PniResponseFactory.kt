package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniCalculation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse

class PniResponseFactory {
  private var pniCalculation: PniCalculation? = PniCalculationFactory().produce()
  private var assessment: PniAssessment? = PniAssessmentFactory().produce()

  fun withPniCalculation(pniCalculation: PniCalculation?) = apply { this.pniCalculation = pniCalculation }
  fun withAssessment(assessment: PniAssessment?) = apply { this.assessment = assessment }

  fun produce() = PniResponse(
    pniCalculation = this.pniCalculation,
    assessment = this.assessment,
  )
}
