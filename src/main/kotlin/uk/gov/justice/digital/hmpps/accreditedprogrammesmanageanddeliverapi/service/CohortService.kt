package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort

@Service
class CohortService(
  private val pniService: PniService,
) {
  companion object {
    private const val SEX_DOMAIN_MINIMUM_THRESHOLD = 0.0
    private const val SEXUAL_OFFENCE_OSP_MINIMUM_THRESHOLD = 0.0
  }

  fun determineOffenceCohort(crn: String): OffenceCohort {
    val pniScore = pniService.getPniScore(crn)

    val ospDcScore = pniScore.riskScore.individualRiskScores.ospDc?.toDoubleOrNull()
    val ospIicScore = pniScore.riskScore.individualRiskScores.ospIic?.toDoubleOrNull()

    // Business rule 1: OSP >= Low
    val isSexualOffenceByOsp = listOfNotNull(ospDcScore, ospIicScore).any { it > SEXUAL_OFFENCE_OSP_MINIMUM_THRESHOLD }

    // Business rule 2: Any sexual domain score > 0
    val isSexualOffenceByPni = with(pniScore.domainScores.sexDomainScore.individualSexScores) {
      listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
        .any { it > SEX_DOMAIN_MINIMUM_THRESHOLD }
    }

    return if (isSexualOffenceByOsp || isSexualOffenceByPni) {
      OffenceCohort.SEXUAL_OFFENCE
    } else {
      OffenceCohort.GENERAL_OFFENCE
    }
  }
}
