package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType
import java.util.UUID

interface ReferralStatusDescriptionRepository : JpaRepository<ReferralStatusDescriptionEntity, UUID> {

  /**
   * Right now, all ReferralStatusDescriptions are Reference data which should be created or modified only
   * by developers / system maintainers via SQL migration scripts.  Attempting to use this method (even in tests)
   * will create multiple status description, which have historically caused red-herring errors elsewhere in the
   * system, around the creation of Referrals, in the EventListener work, and in the Referral Service.
   * --TJWC 2025-09-17
   */
  @Override()
  fun save(entity: ReferralStatusDescriptionEntity): Unit = throw NotImplementedError("Not implemented yet")
  fun findAllByDeletedAtIsNull(): MutableList<ReferralStatusDescriptionEntity>
  fun findAllByIsClosedAndDeletedAtIsNull(isClosed: Boolean): MutableList<ReferralStatusDescriptionEntity>

  @Query(
    """
        SELECT rsd FROM ReferralEntity r 
        JOIN r.statusHistories sh 
        JOIN sh.referralStatusDescription rsd 
        WHERE r.id = :referralId 
        ORDER BY sh.createdAt DESC 
        LIMIT 1
    """,
  )
  fun findMostRecentStatusByReferralId(referralId: UUID): ReferralStatusDescriptionEntity?

  @Query("SELECT r FROM ReferralStatusDescriptionEntity r WHERE r.referralStatusType = :referralStatusType")
  fun findByReferralStatusType(referralStatusType: ReferralStatusType): ReferralStatusDescriptionEntity?
}
