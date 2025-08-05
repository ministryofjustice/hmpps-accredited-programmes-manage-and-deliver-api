package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort

@Service
class CohortService(
  private val pniService: PniService,
) {

  fun determineOffenceCohort(crn: String): OffenceCohort {
    val pniScore = pniService.getPniScore(crn)

    val isSexDomainAboveZero = with(pniScore.domainScores.sexDomainScore.individualSexScores) {
      listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
        .any { it > 0 }
    }

    val ospDcScore = pniScore.riskScore.individualRiskScores.ospDc?.toDoubleOrNull()
    val ospIicScore = pniScore.riskScore.individualRiskScores.ospIic?.toDoubleOrNull()

    // Business rule 1: OSP >= Low
    val isSexualOffenceByOsp = listOfNotNull(ospDcScore, ospIicScore).any { it > 0.0 }

    // Business rule 2: Any sexual domain score > 0
    val isSexualOffenceByPni = isSexDomainAboveZero

    return if (isSexualOffenceByOsp || isSexualOffenceByPni) {
      OffenceCohort.SEXUAL_OFFENCE
    } else {
      OffenceCohort.GENERAL_OFFENCE
    }
  }
}
