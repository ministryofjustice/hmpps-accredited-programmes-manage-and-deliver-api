package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {
  @Query("SELECT r FROM ReferralEntity r LEFT JOIN FETCH r.programmeGroupMemberships WHERE r.id = :id")
  fun findByIdWithMemberships(id: UUID): ReferralEntity?

  @Query("SELECT id from ReferralEntity")
  fun getAllIds(): List<UUID>

  @EntityGraph(attributePaths = ["statusHistories"])
  override fun findById(id: UUID): Optional<ReferralEntity>

  @EntityGraph(attributePaths = ["statusHistories"])
  fun findByCrn(crn: String): List<ReferralEntity>

  @EntityGraph(attributePaths = ["statusHistories"])
  @Query("SELECT r FROM ReferralEntity r WHERE r.crn = :crn ORDER BY r.createdAt DESC LIMIT 1")
  fun findMostRecentByCrn(crn: String): ReferralEntity?

  fun existsByCrn(crn: String): Boolean

  fun findByCrnAndEventIdAndSourcedFrom(
    crn: String,
    eventId: String,
    sourcedFrom: ReferralEntitySourcedFrom,
  ): ReferralEntity?

  fun existsByCrnAndEventIdAndSourcedFrom(
    crn: String,
    eventId: String,
    sourcedFrom: ReferralEntitySourcedFrom,
  ): Boolean

  fun findAllByCreatedAtBefore(createdAtBefore: LocalDateTime): MutableList<ReferralEntity>

  fun findAllByUpdatedAtBefore(updatedAtBefore: LocalDateTime, pageable: Pageable): Page<ReferralEntity>

  @Query("SELECT r FROM ReferralEntity r WHERE r.personSurname IS NULL")
  fun findAllWherePersonSurnameIsNull(pageable: Pageable): Page<ReferralEntity>

  @Query(
    value = """
      SELECT r.id AS referralId,
             r.event_id AS licReqNo,
             r.crn AS crn,
             STRING_AGG(DISTINCT pg.region_name, '; ' ORDER BY pg.region_name) AS regionName
      FROM referral r
      JOIN programme_group_membership gm ON gm.referral_id = r.id
      JOIN programme_group pg ON pg.id = gm.programme_group_id
      WHERE r.intervention_name = 'Building Choices'
        AND pg.id IN (:groupIds)
      GROUP BY r.id, r.event_id, r.crn
      ORDER BY r.crn, r.id
      """,
    nativeQuery = true,
  )
  fun getDosageReportReferrals(groupIds: Collection<UUID>): List<DosageReportReferralProjection>

  fun findByEventNumber(eventNumber: Int): List<ReferralEntity>
}

interface DosageReportReferralProjection {
  val referralId: UUID
  val licReqNo: String?
  val crn: String
  val regionName: String
}
