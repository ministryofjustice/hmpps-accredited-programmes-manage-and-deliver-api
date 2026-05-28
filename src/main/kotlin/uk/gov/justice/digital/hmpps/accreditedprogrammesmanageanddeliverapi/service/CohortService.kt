package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.AuditorContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ActivityType.OVERRIDE_COHORT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCohortHistoryRepository
import java.util.UUID

@Service
@Transactional
class CohortService(
  private val referralCohortHistoryRepository: ReferralCohortHistoryRepository,
  private val telemetryClient: TelemetryClient,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
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

    val programmeGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralEntity.id!!)
    telemetryClient.logToAppInsights(
      "Referral.update-cohort.success",
      mapOf(
        "activityType" to OVERRIDE_COHORT.name,
        "regionName" to (referralEntity.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (referralEntity.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to (programmeGroupMembership?.programmeGroup?.deliveryLocationName ?: ""),
      ),
    )

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

  private fun isSignificantRisk(riskLevel: String): Boolean = riskLevel != RiskScoreLevel.NOT_APPLICABLE.type

  private fun hasSignificantSexDomainScore(pniScore: PniScore): Boolean = with(pniScore.domainScores.sexDomainScore.individualSexScores) {
    listOfNotNull(sexualPreOccupation, offenceRelatedSexualInterests, emotionalCongruence)
      .any { it > SEX_DOMAIN_MINIMUM_THRESHOLD }
  }
}
