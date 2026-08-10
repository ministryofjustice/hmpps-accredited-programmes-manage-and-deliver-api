package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.Constants.ACCREDITED_PROGRAMMES_AUTOMATED_UPDATE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.Constants.UNKNOWN_USER_USERNAME
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.AuditorContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.OVERRIDE_COHORT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCohortHistoryRepository
import java.util.UUID

@Service
@Transactional
class CohortService(
  private val referralCohortHistoryRepository: ReferralCohortHistoryRepository,
  private val telemetryService: TelemetryService,
) {
  companion object {
    private const val SEX_DOMAIN_MINIMUM_THRESHOLD = 0.0
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun updateCohortForReferral(
    referralEntity: ReferralEntity,
    cohort: OffenceCohort,
    createdBy: String = SecurityContextHolder.getContext().authentication?.name ?: UNKNOWN_USER_USERNAME,
  ): ReferralEntity {
    // Overwrite the username to be written when system is automatically updating this value
    AuditorContext.set(createdBy)
    try {
      val latestCohortHistory =
        referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)
      val referralCohortHistory = ReferralCohortHistoryEntity(
        referral = referralEntity,
        cohort = cohort,
        createdBy = createdBy,
      )
      if (latestCohortHistory == null) {
        log.info("No ReferralCohortHistory record for referral with Id: '${referralEntity.id}', creating one...")
        referralCohortHistoryRepository.save(referralCohortHistory)
      } else if (latestCohortHistory.cohort != cohort) {
        log.info("Updating cohort to '$cohort' for referral with Id: '${referralEntity.id}'")
        referralCohortHistoryRepository.save(referralCohortHistory)
      }
    } finally {
      AuditorContext.clear()
    }
    telemetryService.logToAppInsights(
      referralEntity = referralEntity,
      eventName = "Referral.update-cohort.success",
      activityType = OVERRIDE_COHORT.name,
      toReferralStatusId = null,
      appliedBy = null,
    )

    return referralEntity
  }

  fun hasOverriddenCohort(referralId: UUID): Boolean {
    referralCohortHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)?.let {
      return it.createdBy != "SYSTEM" && it.createdBy != ACCREDITED_PROGRAMMES_AUTOMATED_UPDATE
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

  private fun isSignificantRisk(riskLevel: String): Boolean = riskLevel != RiskScoreLevel.NOT_APPLICABLE.type

  private fun hasSignificantSexDomainScore(pniScore: PniScore): Boolean = with(pniScore.domainScores.sexDomainScore.individualSexScores) {
    listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
      .any { it > SEX_DOMAIN_MINIMUM_THRESHOLD }
  }
}
