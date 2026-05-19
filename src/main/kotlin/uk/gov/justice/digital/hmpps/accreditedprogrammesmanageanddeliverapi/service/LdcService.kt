package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.AuditorContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository
import java.util.UUID

@Service
class LdcService(
  private val ldcHistoryRepository: ReferralLdcHistoryRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun updateLdcStatusForReferral(
    referralEntity: ReferralEntity,
    updateLdc: UpdateLdc,
    createdBy: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
  ) {
    // Overwrite the username to be written when system is automatically updating this value
    AuditorContext.set(createdBy)
    try {
      val latestLdcHistory = ldcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)
      if (latestLdcHistory?.hasLdc != updateLdc.hasLdc) {
        log.info("Updating LDC status to '${updateLdc.hasLdc}' for referral with Id: '${referralEntity.id}'")
        val entity = updateLdc.toEntity(referralEntity)
        ldcHistoryRepository.save(entity)
      }
    } finally {
      AuditorContext.clear()
    }
  }

  fun hasOverriddenLdcStatus(referralId: UUID): Boolean {
    ldcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)?.let {
      return it.createdBy != "SYSTEM"
    }
    return false
  }
}
