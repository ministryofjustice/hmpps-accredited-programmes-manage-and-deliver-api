package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DomainScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RiskScore

data class PniResponse(val pniCalculation: PniCalculation?, val assessment: PniAssessment?)

fun PniResponse.toPniScore() = PniScore(
  overallIntensity = Type.toIntensity(pniCalculation?.pni),
  domainScores = DomainScores.from(this),
  riskScore = RiskScore(
    classification = PniRiskLevel.fromLevel(pniCalculation?.riskLevel).name,
    individualRiskScores = IndividualRiskScores.from(this),
  ),
  validationErrors = emptyList(),
)
