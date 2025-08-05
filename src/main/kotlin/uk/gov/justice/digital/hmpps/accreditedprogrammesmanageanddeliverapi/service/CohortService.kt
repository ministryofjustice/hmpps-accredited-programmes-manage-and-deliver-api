package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.NeedLevel

@Service
class CohortService(
  private val pniService: PniService,
) {

  fun determineOffenceCohort(crn: String): OffenceCohort {
    val pniScore = pniService.getPniScore(crn)

    return if (pniScore.domainScores.sexDomainScore.overallSexDomainLevel == NeedLevel.HIGH_NEED) {
      OffenceCohort.SEXUAL_OFFENCE
    } else {
      OffenceCohort.GENERAL_OFFENCE
    }
  }
}
