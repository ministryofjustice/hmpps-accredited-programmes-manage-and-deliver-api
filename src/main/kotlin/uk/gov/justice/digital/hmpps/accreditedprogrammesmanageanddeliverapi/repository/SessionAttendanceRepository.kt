package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.util.UUID

@Repository
interface SessionAttendanceRepository : JpaRepository<SessionAttendanceEntity, UUID> {

  fun findFirstBySessionIdAndGroupMembershipIdOrderByRecordedAtDesc(
    sessionId: UUID,
    groupMembershipId: UUID,
  ): SessionAttendanceEntity?

  @Query(
    value = """
      SELECT gm.referral_id AS referralId,
             pg.region_name AS regionName,
             m.module_number AS moduleNumber,
             mst.session_number AS sessionNumber,
             mst.name AS sessionName,
             pg.code AS groupCode,
             s.id AS sessionId
      FROM session_attendance sa
      JOIN programme_group_membership gm ON sa.group_membership_id = gm.id
      JOIN session s ON sa.session_id = s.id
      JOIN programme_group pg ON s.programme_group_id = pg.id
      JOIN module_session_template mst ON s.module_session_template_id = mst.id
      JOIN module m ON mst.module_id = m.id
      JOIN accredited_programme_template apt ON m.accredited_programme_template_id = apt.id
      JOIN session_attendance_ndelius_outcome outcome ON outcome.code = sa.outcome_type_code
      WHERE gm.referral_id IN (:referralIds)
        AND apt.name = 'Building Choices'
        AND outcome.attendance = true
        AND s.starts_at::date >= (
          SELECT MIN(gm2.created_at::date)
          FROM programme_group_membership gm2
          WHERE gm2.referral_id = gm.referral_id
        )
      """,
    nativeQuery = true,
  )
  fun getDosageAttendanceRows(referralIds: List<UUID>): List<DosageAttendanceProjection>
}

interface DosageAttendanceProjection {
  val referralId: UUID
  val regionName: String
  val moduleNumber: Int
  val sessionNumber: Int
  val sessionName: String
  val groupCode: String
  val sessionId: UUID
}
