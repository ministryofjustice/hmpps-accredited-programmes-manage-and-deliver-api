package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.LdcNeedsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.LdcNeedsRepository

@Service
class LdcNeedsService(
  private val pniService: PniService,
  private val ldcNeedsRepository: LdcNeedsRepository,
) {
  companion object {
    private const val LDC_NEEDS_THRESHOLD = 3
  }

  fun hasLdcNeeds(crn: String): Boolean {
    val pniScore = pniService.getRawPniResponse(crn)
    val ldcScore = pniScore.assessment?.ldc?.score

    return ldcScore != null && ldcScore >= LDC_NEEDS_THRESHOLD
  }

  fun resolveLdcNeeds(referral: ReferralEntity): LdcNeedsEntity {
    val existing = ldcNeedsRepository.findByReferralId(referral.id!!)

    return if (existing == null) {
      val determination = hasLdcNeeds(referral.crn)
      val newEntity = LdcNeedsEntity(
        referral = referral,
        hasLdcNeeds = determination,
        overridden = false,
      )
      ldcNeedsRepository.save(newEntity)
    } else if (!existing.overridden) {
      val determination = hasLdcNeeds(referral.crn)
      if (existing.hasLdcNeeds != determination) {
        existing.hasLdcNeeds = determination
        ldcNeedsRepository.save(existing)
      }
      existing
    } else {
      existing
    }
  }
}
