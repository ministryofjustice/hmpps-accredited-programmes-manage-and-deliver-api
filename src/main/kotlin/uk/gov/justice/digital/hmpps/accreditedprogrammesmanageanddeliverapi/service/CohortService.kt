package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore

@Service
class CohortService(
  private val pniService: PniService,
) {
  companion object {
    private const val SEX_DOMAIN_MINIMUM_THRESHOLD = 0.0
    private const val OSP_SEXUAL_OFFENCE_MINIMUM_THRESHOLD = 0.0
  }

  fun determineOffenceCohort(crn: String): OffenceCohort {
    val pniScore = pniService.getPniScore(crn)

    // Business rule 1: OSP score indicates a sexual offence risk
    val hasSexualOffenceByOsp = hasSignificantOspScore(pniScore)

    // Business rule 2: PNI sex domain score indicates a sexual offence risk
    val hasSexualOffenceByPni = hasSignificantSexDomainScore(pniScore)

    return if (hasSexualOffenceByOsp || hasSexualOffenceByPni) {
      OffenceCohort.SEXUAL_OFFENCE
    } else {
      OffenceCohort.GENERAL_OFFENCE
    }
  }

  private fun hasSignificantOspScore(pniScore: PniScore): Boolean {
    val ospDcScore = pniScore.riskScore.individualRiskScores.ospDc?.toDoubleOrNull()
    val ospIicScore = pniScore.riskScore.individualRiskScores.ospIic?.toDoubleOrNull()
    return listOfNotNull(ospDcScore, ospIicScore)
      .any { it > OSP_SEXUAL_OFFENCE_MINIMUM_THRESHOLD }
  }

  private fun hasSignificantSexDomainScore(pniScore: PniScore): Boolean {
    return with(pniScore.domainScores.sexDomainScore.individualSexScores) {
      listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
        .any { it > SEX_DOMAIN_MINIMUM_THRESHOLD }
    }
  }
}