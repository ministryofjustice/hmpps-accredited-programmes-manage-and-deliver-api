package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel

@Service
class CohortService(
  private val pniService: PniService,
) {
  companion object {
    private const val SEX_DOMAIN_MINIMUM_THRESHOLD = 0.0
  }

  fun determineOffenceCohort(crn: String): OffenceCohort {
    val pniScore = pniService.getPniScore(crn)

    return if (hasSignificantOspScore(pniScore) || hasSignificantSexDomainScore(pniScore)) {
      OffenceCohort.SEXUAL_OFFENCE
    } else {
      OffenceCohort.GENERAL_OFFENCE
    }
  }

  private fun hasSignificantOspScore(pniScore: PniScore): Boolean {
    val ospDc = pniScore.riskScore.individualRiskScores.ospDc
    val ospIic = pniScore.riskScore.individualRiskScores.ospIic

    return listOfNotNull(ospDc, ospIic).any { isSignificantRisk(it) }
  }

  private fun isSignificantRisk(riskLevel: String): Boolean = riskLevel != RiskScoreLevel.NOT_APPLICABLE.toString()

  private fun hasSignificantSexDomainScore(pniScore: PniScore): Boolean = with(pniScore.domainScores.sexDomainScore.individualSexScores) {
    listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
      .any { it > SEX_DOMAIN_MINIMUM_THRESHOLD }
  }
}
