package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface SessionAttendanceRepository : JpaRepository<SessionAttendanceEntity, UUID> {

  fun findFirstBySessionIdAndGroupMembershipIdOrderByRecordedAtDesc(
    sessionId: UUID,
    groupMembershipId: UUID,
  ): SessionAttendanceEntity?

  /**
   * Sessions attended by each referral. Amending attendance adds a new record rather than updating the existing one,
   * so only the latest record for each referral and session counts.
   */
  @Query(
    value = """
      WITH latest_attendance AS (
        SELECT DISTINCT ON (sa.session_id, gm.referral_id)
          sa.session_id,
          gm.referral_id,
          sa.outcome_type_code
        FROM session_attendance sa
        JOIN programme_group_membership gm ON sa.group_membership_id = gm.id
        WHERE gm.referral_id IN (:referralIds)
        ORDER BY sa.session_id, gm.referral_id, sa.created_at DESC, sa.recorded_at DESC NULLS LAST
      )
      SELECT la.referral_id AS referralId,
             m.module_number AS moduleNumber,
             mst.session_number AS sessionNumber,
             mst.name AS sessionName,
             mst.session_type AS sessionType,
             pg.code AS groupCode,
             s.id AS sessionId,
             s.is_catchup AS sessionIsCatchup,
             s.starts_at AS sessionStartAt
      FROM latest_attendance la
      JOIN session s ON la.session_id = s.id
      JOIN programme_group pg ON s.programme_group_id = pg.id
      JOIN module_session_template mst ON s.module_session_template_id = mst.id
      JOIN module m ON mst.module_id = m.id
      JOIN accredited_programme_template apt ON m.accredited_programme_template_id = apt.id
      JOIN session_attendance_ndelius_outcome outcome ON outcome.code = la.outcome_type_code
      WHERE apt.name = 'Building Choices'
        AND outcome.attendance = true
      """,
    nativeQuery = true,
  )
  fun getDosageAttendanceRows(referralIds: List<UUID>): List<DosageAttendanceProjection>
}

interface DosageAttendanceProjection {
  val referralId: UUID
  val moduleNumber: Int
  val sessionNumber: Int
  val sessionName: String
  val sessionType: String
  val groupCode: String
  val sessionId: UUID
  val sessionIsCatchup: Boolean
  val sessionStartAt: LocalDateTime
}
