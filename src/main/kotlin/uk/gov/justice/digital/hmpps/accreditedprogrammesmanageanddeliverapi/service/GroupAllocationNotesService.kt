package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateOrUpdateReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralMotivationBackgroundAndNonAssociationsRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class GroupAllocationNotesService(
  private val referralMotivationBackgroundAndNonAssociationsRepository: ReferralMotivationBackgroundAndNonAssociationsRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralMotivationBackgroundAndNonAssociationsByReferralId(referralId: UUID): ReferralMotivationBackgroundAndNonAssociations {
    val motivationBackgroundAndNonAssociations = referralMotivationBackgroundAndNonAssociationsRepository.findByReferralId(referralId)
      ?: throw NotFoundException("No motivation, background and non-associations information found for referral id: $referralId")

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
    val savedReferralMotivationBackgroundAndNonAssociations = referralMotivationBackgroundAndNonAssociationsRepository.save(motivationBackgroundAndNonAssociations)
    log.info("Created motivation, background and non-associations for referral id: ${referral.id}")
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
    val updatedReferralMotivationBackgroundAndNonAssociations = referralMotivationBackgroundAndNonAssociationsRepository.save(referral.referralMotivationBackgroundAndNonAssociations!!)
    log.info("Updated motivation, background and non-associations for referral id: ${referral.id}")
    return ReferralMotivationBackgroundAndNonAssociations.toApi(
      updatedReferralMotivationBackgroundAndNonAssociations,
    )
  }
}
