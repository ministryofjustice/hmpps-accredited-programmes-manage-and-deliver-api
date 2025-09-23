package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository
import java.util.UUID

@Service
class LdcService(
  private val ldcHistoryRepository: ReferralLdcHistoryRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun updateLdcStatusForReferral(referralEntity: ReferralEntity, updateLdc: UpdateLdc) {
    log.info("Updating LDC status to '${updateLdc.hasLdc}' for referral with Id: '${referralEntity.id}'")
    ldcHistoryRepository.save(updateLdc.toEntity(referralEntity))
  }

  fun hasOverriddenLdcStatus(referralId: UUID): Boolean {
    ldcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)?.let {
      return it.createdBy != "SYSTEM"
    }
    return false
  }
}
