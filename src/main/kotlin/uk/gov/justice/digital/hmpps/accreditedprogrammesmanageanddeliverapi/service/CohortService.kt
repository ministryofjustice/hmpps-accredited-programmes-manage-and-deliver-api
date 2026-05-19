package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.AuditorContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCohortHistoryRepository
import java.util.UUID

@Service
class CohortService(
  private val referralCohortHistoryRepository: ReferralCohortHistoryRepository,
) {
  companion object {
    private const val SEX_DOMAIN_MINIMUM_THRESHOLD = 0.0
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun updateCohortForReferral(
    referralEntity: ReferralEntity,
    cohort: OffenceCohort,
    createdBy: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
  ): ReferralEntity {
    // Overwrite the username to be written when system is automatically updating this value
    AuditorContext.set(createdBy)
    try {
      val latestCohortHistory =
        referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)
      if (latestCohortHistory?.cohort != cohort) {
        log.info("Updating cohort to '$cohort' for referral with Id: '${referralEntity.id}'")
        referralCohortHistoryRepository.save(
          ReferralCohortHistoryEntity(
            referral = referralEntity,
            cohort = cohort,
            createdBy = createdBy,
          ),
        )
      }
    } finally {
      AuditorContext.clear()
    }
    return referralEntity
  }

  fun hasOverriddenCohort(referralId: UUID): Boolean {
    referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)?.let {
      return it.createdBy != "SYSTEM"
    }
    return false
  }

  fun determineOffenceCohort(pniScore: PniScore): OffenceCohort = if (hasSignificantOspScore(pniScore) || hasSignificantSexDomainScore(pniScore)) {
    OffenceCohort.SEXUAL_OFFENCE
  } else {
    OffenceCohort.GENERAL_OFFENCE
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
