package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ActivityType.SET_MOTIVATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ActivityType.UPDATE_MOTIVATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateOrUpdateReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralMotivationBackgroundAndNonAssociationsRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class GroupAllocationNotesService(
  private val referralMotivationBackgroundAndNonAssociationsRepository: ReferralMotivationBackgroundAndNonAssociationsRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val telemetryClient: TelemetryClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralMotivationBackgroundAndNonAssociationsByReferralId(referralId: UUID): ReferralMotivationBackgroundAndNonAssociations {
    val motivationBackgroundAndNonAssociations =
      referralMotivationBackgroundAndNonAssociationsRepository.findByReferralId(referralId)

    return ReferralMotivationBackgroundAndNonAssociations.toApi(motivationBackgroundAndNonAssociations)
  }

  fun createOrUpdateMotivationBackgroundAndNonAssociations(
    referral: ReferralEntity,
    update: CreateOrUpdateReferralMotivationBackgroundAndNonAssociations,
    createdOrUpdatedBy: String,
  ): ReferralMotivationBackgroundAndNonAssociations {
    referral.referralMotivationBackgroundAndNonAssociations?.let {
      return updateReferralMotivationBackgroundAndNonAssociations(referral, update, createdOrUpdatedBy)
    }
    return createMotivationBackgroundAndNonAssociations(referral, update, createdOrUpdatedBy)
  }

  fun createMotivationBackgroundAndNonAssociations(
    referral: ReferralEntity,
    update: CreateOrUpdateReferralMotivationBackgroundAndNonAssociations,
    createdBy: String,
  ): ReferralMotivationBackgroundAndNonAssociations {
    val motivationBackgroundAndNonAssociations = ReferralMotivationBackgroundAndNonAssociationsEntity(
      referral = referral,
      maintainsInnocence = update.maintainsInnocence,
      motivations = update.motivations,
      nonAssociations = update.nonAssociations,
      otherConsiderations = update.otherConsiderations,
      createdBy = createdBy,
    )
    val savedReferralMotivationBackgroundAndNonAssociations =
      referralMotivationBackgroundAndNonAssociationsRepository.save(motivationBackgroundAndNonAssociations)
    log.info("Created motivation, background and non-associations for referral id: ${referral.id}")
    val programmeGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referral.id!!)
    telemetryClient.logToAppInsights(
      "Referral.create-motivation-background-non-associations.success",
      mapOf(
        "activityType" to SET_MOTIVATION.name,
        "regionName" to (referral.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (referral.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to (programmeGroupMembership?.programmeGroup?.deliveryLocationName ?: ""),
      ),
    )

    return ReferralMotivationBackgroundAndNonAssociations.toApi(savedReferralMotivationBackgroundAndNonAssociations)
  }

  private fun updateReferralMotivationBackgroundAndNonAssociations(
    referral: ReferralEntity,
    update: CreateOrUpdateReferralMotivationBackgroundAndNonAssociations,
    updatedBy: String,
  ): ReferralMotivationBackgroundAndNonAssociations {
    referral.referralMotivationBackgroundAndNonAssociations?.apply {
      maintainsInnocence = update.maintainsInnocence
      motivations = update.motivations
      nonAssociations = update.nonAssociations
      otherConsiderations = update.otherConsiderations
      lastUpdatedBy = updatedBy
      lastUpdatedAt = LocalDateTime.now()
    }
    val updatedReferralMotivationBackgroundAndNonAssociations =
      referralMotivationBackgroundAndNonAssociationsRepository.save(referral.referralMotivationBackgroundAndNonAssociations!!)
    log.info("Updated motivation, background and non-associations for referral id: ${referral.id}")
    val programmeGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referral.id!!)
    telemetryClient.logToAppInsights(
      "Referral.update-motivation-background-non-associations.success",
      mapOf(
        "activityType" to UPDATE_MOTIVATION.name,
        "regionName" to (referral.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (referral.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to (programmeGroupMembership?.programmeGroup?.deliveryLocationName ?: ""),
      ),
    )

    return ReferralMotivationBackgroundAndNonAssociations.toApi(
      updatedReferralMotivationBackgroundAndNonAssociations,
    )
  }
}
