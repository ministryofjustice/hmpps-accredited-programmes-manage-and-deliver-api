package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DomainScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.RiskScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom

private const val LEARNING_DISABILITIES_AND_CHALLENGES_THRESHOLD = 3

data class PniResponse(val pniCalculation: PniCalculation?, val assessment: PniAssessment?)

fun PniResponse.toPniScore(sourcedFrom: ReferralEntitySourcedFrom? = null) = PniScore(
  overallIntensity = Type.toIntensity(pniCalculation?.pni),
  domainScores = DomainScores.from(this),
  riskScore = RiskScore(
    classification = PniRiskLevel.fromLevel(pniCalculation?.riskLevel).name,
    individualRiskScores = IndividualRiskScores.from(this),
  ),
  validationErrors = emptyList(),
  hasLdc = this.hasLdc(),
  ldcScore = assessment?.ldc?.score,
  displayIneligibleWarning = shouldDisplayIneligibleWarning(Type.toIntensity(pniCalculation?.pni), sourcedFrom),
)

fun PniResponse.hasLdc(): Boolean = assessment?.ldc?.subTotal?.let { subTotal ->
  subTotal >= LEARNING_DISABILITIES_AND_CHALLENGES_THRESHOLD
} ?: false

private fun shouldDisplayIneligibleWarning(
  overallIntensity: OverallIntensity?,
  sourcedFrom: ReferralEntitySourcedFrom?,
): Boolean = overallIntensity == OverallIntensity.ALTERNATIVE_PATHWAY && sourcedFrom == ReferralEntitySourcedFrom.REQUIREMENT
