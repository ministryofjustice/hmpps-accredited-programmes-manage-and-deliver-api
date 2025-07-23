package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DomainScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore

data class PniResponse(val pniCalculation: PniCalculation?, val assessment: PniAssessment?)

fun PniResponse.toPniScore() = PniScore(
  overallIntensity = Type.toIntensity(pniCalculation?.pni),
  domainScores = DomainScores.from(this),
)
