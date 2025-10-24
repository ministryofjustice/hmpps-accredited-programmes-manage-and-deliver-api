package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.util.UUID

interface ReferralStatusDescriptionRepository : JpaRepository<ReferralStatusDescriptionEntity, UUID> {
  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Awaiting assessment'")
  fun getAwaitingAssessmentStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Awaiting allocation'")
  fun getAwaitingAllocationStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Suitable but not ready'")
  fun getSuitableButNotReadyStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Deprioritised'")
  fun getDeprioritisedStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Recall'")
  fun getRecallStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Return to court'")
  fun getReturnToCourtStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Scheduled'")
  fun getScheduledStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'On programme'")
  fun getOnProgrammeStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Programme complete'")
  fun getProgrammeCompleteStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Breach (non-attendance)'")
  fun getBreachNonAttendanceStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Deferred'")
  fun getDeferredStatusDescription(): ReferralStatusDescriptionEntity

  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Withdrawn'")
  fun getWithdrawnStatusDescription(): ReferralStatusDescriptionEntity

  /**
   * Right now, all ReferralStatusDescriptions are Reference data which should be created or modified only
   * by developers / system maintainers via SQL migration scripts.  Attempting to use this method (even in tests)
   * will create multiple status description, which have historically caused red-herring errors elsewhere in the
   * system, around the creation of Referrals, in the EventListener work, and in the Referral Service.
   * --TJWC 2025-09-17
   */
  @Override()
  fun save(entity: ReferralStatusDescriptionEntity): Unit = throw NotImplementedError("Not implemented yet")
  fun findAllByIsClosed(isClosed: Boolean): MutableList<ReferralStatusDescriptionEntity>

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
}
