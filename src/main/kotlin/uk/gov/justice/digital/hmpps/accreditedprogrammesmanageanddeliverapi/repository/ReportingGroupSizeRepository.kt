package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReportingGroupSizeEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface ReportingGroupSizeRepository : JpaRepository<ReportingGroupSizeEntity, UUID> {
  @Query(
    value = "SELECT * FROM reporting_group_size WHERE created_at::date > :date",
    nativeQuery = true,
  )
  fun getAllGroupsCreatedAfter(date: LocalDate): List<ReportingGroupSizeEntity>

  /**
   * The number of PoPs scheduled to attend each main session a group has held. Main sessions are group sessions rather
   * than one-to-ones. A PoP scheduled for both a session and its catch-up is counted once.
   */
  @Query(
    value = """
      SELECT s.programme_group_id AS groupId,
             s.module_session_template_id AS sessionTemplateId,
             COUNT(DISTINCT a.referral_id) AS scheduledCount
      FROM session s
      JOIN module_session_template mst ON mst.id = s.module_session_template_id
      LEFT JOIN attendee a ON a.session_id = s.id
      WHERE s.programme_group_id IN (:groupIds)
        AND mst.session_type = 'GROUP'
        AND s.is_placeholder = FALSE
        AND s.starts_at <= :now
      GROUP BY s.programme_group_id, s.module_session_template_id
      """,
    nativeQuery = true,
  )
  fun getScheduledCountsForMainSessions(
    groupIds: Collection<UUID>,
    now: LocalDateTime,
  ): List<MainSessionScheduledCountProjection>
}

interface MainSessionScheduledCountProjection {
  val groupId: UUID
  val sessionTemplateId: UUID
  val scheduledCount: Long
}
