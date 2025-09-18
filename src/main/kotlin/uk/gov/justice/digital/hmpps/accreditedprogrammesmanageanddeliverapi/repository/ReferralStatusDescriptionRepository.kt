package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.util.UUID

interface ReferralStatusDescriptionRepository : JpaRepository<ReferralStatusDescriptionEntity, UUID> {
  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Awaiting assessment'")
  fun getAwaitingAssessmentStatusDescription(): ReferralStatusDescriptionEntity

  /**
   * Right now, all ReferralStatusDescriptions are Reference data which should be created or modified only
   * by developers / system maintainers via SQL migration scripts.  Attempting to use this method (even in tests)
   * will create multiple status description, which have historically caused red-herring errors elsewhere in the
   * system, around the creation of Referrals, in the EventListener work, and in the Referral Service.
   * --TJWC 2025-09-17
   */
  @Override()
  fun save(entity: ReferralStatusDescriptionEntity): Unit = throw NotImplementedError("Not implemented yet")
}
