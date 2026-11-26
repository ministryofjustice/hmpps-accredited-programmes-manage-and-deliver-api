package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.BuildingChoicesSessionColumnProjection
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DosageAttendanceProjection
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReportingGroupSizeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.sql.Types
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class ReportingService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val reportingGroupSizeRepository: ReportingGroupSizeRepository,
  private val referralRepository: ReferralRepository,
  private val sessionAttendanceRepository: SessionAttendanceRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
  private val clock: Clock,
) {
  companion object {
    /** "Introduction to Building Choices", shown to users as Getting started 1. Its name has changed, its id has not. */
    private val GETTING_STARTED_1_SESSION_TEMPLATE_ID = UUID.fromString("e17396c4-0638-4100-7f90-e7f8091a2b3c")

    private val GROUP_SIZE_CSV_HEADER = listOf(
      "id",
      "code",
      "createdAt",
      "sex",
      "cohort",
      "isLdc",
      "earliestPossibleStartDate",
      "regionName",
      "pduCode",
      "pduName",
      "locationCode",
      "locationName",
      "groupSize",
      "facilitatorStaffCode",
    )
    private const val FACILITATOR_CONTINUITY_CSV_HEADER =
      "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt"
    private const val SESSION_RATE_CSV_HEADER =
      "licReqNo,crn,groupCode,regionName,pduName,deliveryLocationName,weekStarting,numberSessionAttended,numberSessionsNotAttended,numberSessionsScheduled"

    /** Selects the sessions (s) of groups (pg) that match a filter, using the parameters from [heldSessionsParameters] */
    private const val HELD_SESSIONS_CONDITIONS = """
      pg.deleted_at IS NULL
      AND (
        pg.region_name = CAST(:regionName AS TEXT)
        OR (CAST(:regionName AS TEXT) IS NULL AND pg.region_name NOT IN (:excludedRegionNames))
      )
      AND s.is_placeholder = FALSE
      AND s.starts_at::date BETWEEN :startDate AND :endDate
      AND s.starts_at <= :now
    """
  }

  private val csvMapper = CsvMapper.builder().findAndAddModules().build().apply {
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  private val facilitatorContinuityCsvSchema: CsvSchema = CsvSchema.builder()
    .addColumn("code")
    .addColumn("sessionNumber")
    .addColumn("sessionName")
    .addColumn("sessionType")
    .addColumn("isCatchUp")
    .addColumn("attendeeCount")
    .addColumn("facilitatorStaffCodes")
    .addColumn("regionName")
    .addColumn("deliveryLocationName")
    .addColumn("probationDeliveryUnitName")
    .addColumn("sessionStartTime")
    .addColumn("sessionCreatedAt")
    .build()

  private val sessionRateCsvSchema: CsvSchema = CsvSchema.builder()
    .addColumn("licReqNo")
    .addColumn("crn")
    .addColumn("groupCode")
    .addColumn("regionName")
    .addColumn("pduName")
    .addColumn("deliveryLocationName")
    .addColumn("weekStarting")
    .addColumn("numberSessionAttended")
    .addColumn("numberSessionsNotAttended")
    .addColumn("numberSessionsScheduled")
    .build()

  private fun heldSessionsParameters(filter: ReportingFilter) = MapSqlParameterSource()
    .addValue("regionName", filter.regionName, Types.VARCHAR)
    .addValue("excludedRegionNames", ReportingRegions.EXCLUDED_FROM_NATIONAL)
    .addValue("startDate", filter.startDate, Types.DATE)
    .addValue("endDate", filter.endDate, Types.DATE)
    .addValue("now", LocalDateTime.now(clock), Types.TIMESTAMP)

  private fun getGroupIds(filter: ReportingFilter): List<UUID> = programmeGroupRepository.findIdsBySessionFirstScheduledBetween(
    sessionTemplateId = GETTING_STARTED_1_SESSION_TEMPLATE_ID,
    startDate = filter.startDate,
    endDate = filter.endDate,
    regionName = filter.regionName,
    excludedRegionNames = ReportingRegions.EXCLUDED_FROM_NATIONAL,
  )

  fun getGroupSizeReportCsv(filter: ReportingFilter): String {
    val mainSessions = moduleSessionTemplateRepository.getBuildingChoicesSessionColumns()
      .filter { it.sessionType == SessionType.GROUP.name }
    val header = GROUP_SIZE_CSV_HEADER + mainSessions.map { it.toSessionColumn().header }

    val groupIds = getGroupIds(filter)
    if (groupIds.isEmpty()) {
      return renderCsv(header, emptyList())
    }

    val scheduledCounts = reportingGroupSizeRepository
      .getScheduledCountsForMainSessions(groupIds, LocalDateTime.now(clock))
      .associate { (it.groupId to it.sessionTemplateId) to it.scheduledCount }

    val rows = reportingGroupSizeRepository.findAllById(groupIds)
      .sortedWith(compareBy({ it.regionName }, { it.code }))
      .map { group ->
        listOf(
          group.id.toString(),
          group.code,
          group.createdAt.toString(),
          group.sex.toString(),
          group.cohort.toString(),
          group.isLdc.toString(),
          group.earliestPossibleStartDate.toString(),
          group.regionName,
          group.pduCode,
          group.pduName,
          group.locationCode,
          group.locationName,
          group.groupSize.toString(),
          group.facilitatorStaffCode.orEmpty(),
        ) + mainSessions.map { scheduledCounts[group.id to it.sessionTemplateId]?.toString().orEmpty() }
      }

    return renderCsv(header, rows)
  }

  fun getSessionRateReportCsv(filter: ReportingFilter): String {
    val csvRows = getSessionRateRows(filter).map {
      SessionRateCsvRow(
        licReqNo = it.licReqNo,
        crn = it.crn,
        groupCode = it.groupCode,
        regionName = it.regionName,
        pduName = it.pduName,
        deliveryLocationName = it.deliveryLocationName,
        weekStarting = it.weekStarting.toString(),
        numberSessionAttended = it.numberSessionAttended,
        numberSessionsNotAttended = it.numberSessionsNotAttended,
        numberSessionsScheduled = it.numberSessionsScheduled,
      )
    }

    val csvBody = csvMapper.writer(sessionRateCsvSchema).writeValueAsString(csvRows).trimEnd('\n', '\r')
    return if (csvBody.isBlank()) SESSION_RATE_CSV_HEADER else "$SESSION_RATE_CSV_HEADER\n$csvBody"
  }

  private fun getSessionRateRows(filter: ReportingFilter): List<SessionRateReportRow> {
    val sql =
      """
      WITH scheduled_sessions AS (
        SELECT
          r.id AS referral_id,
          COALESCE(r.event_id, '') AS lic_req_no,
          r.crn,
          s.id AS session_id,
          DATE_TRUNC('week', s.starts_at)::date AS week_starting,
          s.starts_at AS session_start_time,
          pg.code AS group_code,
          pg.region_name,
          pg.probation_delivery_unit_name AS pdu_name,
          pg.delivery_location_name
        FROM attendee a
        JOIN referral r ON r.id = a.referral_id
        JOIN session s ON s.id = a.session_id
        JOIN programme_group pg ON pg.id = s.programme_group_id
        WHERE $HELD_SESSIONS_CONDITIONS
      ),
      latest_attendance AS (
        SELECT DISTINCT ON (sa.session_id, gm.referral_id)
          sa.session_id,
          gm.referral_id,
          outcome.attendance AS attended
        FROM session_attendance sa
        JOIN programme_group_membership gm ON gm.id = sa.group_membership_id
        JOIN session_attendance_ndelius_outcome outcome ON outcome.code = sa.outcome_type_code
        ORDER BY sa.session_id, gm.referral_id, COALESCE(sa.recorded_at, sa.created_at) DESC, sa.created_at DESC
      ),
      weekly_actual AS (
        SELECT
          ss.referral_id,
          ss.lic_req_no,
          ss.crn,
          ss.group_code,
          ss.region_name,
          ss.pdu_name,
          ss.delivery_location_name,
          ss.week_starting,
          COUNT(DISTINCT ss.session_id)::INTEGER AS number_sessions_scheduled,
          COUNT(DISTINCT ss.session_id) FILTER (WHERE la.attended = TRUE)::INTEGER AS number_sessions_attended,
          COUNT(DISTINCT ss.session_id) FILTER (WHERE la.attended = FALSE)::INTEGER AS number_sessions_not_attended
        FROM scheduled_sessions ss
        LEFT JOIN latest_attendance la
          ON la.session_id = ss.session_id
          AND la.referral_id = ss.referral_id
        GROUP BY
          ss.referral_id,
          ss.lic_req_no,
          ss.crn,
          ss.group_code,
          ss.region_name,
          ss.pdu_name,
          ss.delivery_location_name,
          ss.week_starting
      ),
      -- One row for each week a referral has sessions, with the next such week, so that the gap between them can be
      -- filled without searching all of the weeks again for each gap
      referral_weeks AS (
        SELECT
          rw.*,
          LEAD(rw.week_starting) OVER (PARTITION BY rw.referral_id ORDER BY rw.week_starting) AS next_week_starting
        FROM (
          SELECT DISTINCT ON (wa.referral_id, wa.week_starting)
            wa.referral_id,
            wa.lic_req_no,
            wa.crn,
            wa.group_code,
            wa.region_name,
            wa.pdu_name,
            wa.delivery_location_name,
            wa.week_starting
          FROM weekly_actual wa
          ORDER BY wa.referral_id, wa.week_starting, wa.group_code
        ) rw
      ),
      gap_weeks AS (
        SELECT
          rw.referral_id,
          rw.lic_req_no,
          rw.crn,
          rw.group_code,
          rw.region_name,
          rw.pdu_name,
          rw.delivery_location_name,
          gs.week_starting::date AS week_starting,
          0::INTEGER AS number_sessions_scheduled,
          0::INTEGER AS number_sessions_attended,
          0::INTEGER AS number_sessions_not_attended
        FROM referral_weeks rw
        CROSS JOIN LATERAL GENERATE_SERIES(
          rw.week_starting + 7,
          rw.next_week_starting - 7,
          INTERVAL '1 week'
        ) AS gs(week_starting)
      )
      SELECT
        combined.lic_req_no,
        combined.crn,
        combined.group_code,
        combined.region_name,
        combined.pdu_name,
        combined.delivery_location_name,
        combined.week_starting,
        combined.number_sessions_attended,
        combined.number_sessions_not_attended,
        combined.number_sessions_scheduled
      FROM (
        SELECT
          wa.lic_req_no,
          wa.crn,
          wa.group_code,
          wa.region_name,
          wa.pdu_name,
          wa.delivery_location_name,
          wa.week_starting,
          wa.number_sessions_attended,
          wa.number_sessions_not_attended,
          wa.number_sessions_scheduled
        FROM weekly_actual wa
        UNION ALL
        SELECT
          gw.lic_req_no,
          gw.crn,
          gw.group_code,
          gw.region_name,
          gw.pdu_name,
          gw.delivery_location_name,
          gw.week_starting,
          gw.number_sessions_attended,
          gw.number_sessions_not_attended,
          gw.number_sessions_scheduled
        FROM gap_weeks gw
      ) combined
      ORDER BY combined.crn, combined.week_starting, combined.group_code
      """.trimIndent()

    return namedParameterJdbcTemplate.query(sql, heldSessionsParameters(filter)) { rs, _ ->
      SessionRateReportRow(
        licReqNo = rs.getString("lic_req_no") ?: "",
        crn = rs.getString("crn"),
        groupCode = rs.getString("group_code"),
        regionName = rs.getString("region_name"),
        pduName = rs.getString("pdu_name"),
        deliveryLocationName = rs.getString("delivery_location_name"),
        weekStarting = rs.getDate("week_starting").toLocalDate(),
        numberSessionAttended = rs.getInt("number_sessions_attended"),
        numberSessionsNotAttended = rs.getInt("number_sessions_not_attended"),
        numberSessionsScheduled = rs.getInt("number_sessions_scheduled"),
      )
    }
  }

  fun getFacilitatorContinuityReportCsv(filter: ReportingFilter): String {
    val rows = getFacilitatorContinuityReportRows(filter)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val csvRows = rows.map {
      FacilitatorContinuityCsvRow(
        code = it.code,
        sessionNumber = it.sessionNumber,
        sessionName = it.sessionName,
        sessionType = it.sessionType,
        isCatchUp = it.isCatchUp,
        attendeeCount = it.attendeeCount,
        facilitatorStaffCodes = it.facilitatorStaffCodes,
        regionName = it.regionName,
        deliveryLocationName = it.deliveryLocationName,
        probationDeliveryUnitName = it.probationDeliveryUnitName,
        sessionStartTime = it.sessionStartTime.format(dateFormatter),
        sessionCreatedAt = it.sessionCreatedAt.format(dateFormatter),
      )
    }

    val csvBody = csvMapper.writer(facilitatorContinuityCsvSchema).writeValueAsString(csvRows).trimEnd('\n', '\r')
    return if (csvBody.isBlank()) FACILITATOR_CONTINUITY_CSV_HEADER else "$FACILITATOR_CONTINUITY_CSV_HEADER\n$csvBody"
  }

  private fun getFacilitatorContinuityReportRows(filter: ReportingFilter): List<FacilitatorContinuityReportRow> {
    val sql =
      """
      WITH template_session_numbers AS (
        SELECT
          mst.id AS module_session_template_id,
          ROW_NUMBER() OVER (
            PARTITION BY m.accredited_programme_template_id
            ORDER BY m.module_number, mst.session_number, mst.id
          ) AS template_session_number
        FROM module_session_template mst
        JOIN module m ON m.id = mst.module_id
      ),
      attendee_counts AS (
        SELECT
          a.session_id,
          COUNT(*)::INTEGER AS attendee_count
        FROM attendee a
        GROUP BY a.session_id
      ),
      facilitator_codes AS (
        SELECT
          sf.session_id,
          STRING_AGG(DISTINCT f.ndelius_person_code, ',' ORDER BY f.ndelius_person_code) AS facilitator_staff_codes
        FROM session_facilitator sf
        JOIN facilitator f ON f.id = sf.facilitator_id
        GROUP BY sf.session_id
      )
      SELECT
        pg.code AS code,
        tsn.template_session_number AS session_number,
        mst.name AS session_name,
        CASE WHEN mst.session_type = 'GROUP' THEN 'group' ELSE 'one-to-one' END AS session_type,
        s.is_catchup AS is_catch_up,
        COALESCE(ac.attendee_count, 0) AS attendee_count,
        COALESCE(fc.facilitator_staff_codes, '') AS facilitator_staff_codes,
        pg.region_name AS region_name,
        pg.delivery_location_name AS delivery_location_name,
        pg.probation_delivery_unit_name AS probation_delivery_unit_name,
        s.starts_at AS session_start_time,
        s.created_at AS session_created_at
      FROM session s
      JOIN programme_group pg ON pg.id = s.programme_group_id
      JOIN module_session_template mst ON mst.id = s.module_session_template_id
      LEFT JOIN template_session_numbers tsn ON tsn.module_session_template_id = mst.id
      LEFT JOIN attendee_counts ac ON ac.session_id = s.id
      LEFT JOIN facilitator_codes fc ON fc.session_id = s.id
      WHERE $HELD_SESSIONS_CONDITIONS
      ORDER BY pg.code, s.starts_at, s.id
      """.trimIndent()

    return namedParameterJdbcTemplate.query(sql, heldSessionsParameters(filter)) { rs, _ ->
      FacilitatorContinuityReportRow(
        code = rs.getString("code"),
        sessionNumber = rs.getInt("session_number"),
        sessionName = rs.getString("session_name"),
        sessionType = rs.getString("session_type"),
        isCatchUp = rs.getBoolean("is_catch_up"),
        attendeeCount = rs.getInt("attendee_count"),
        facilitatorStaffCodes = rs.getString("facilitator_staff_codes") ?: "",
        regionName = rs.getString("region_name"),
        deliveryLocationName = rs.getString("delivery_location_name"),
        probationDeliveryUnitName = rs.getString("probation_delivery_unit_name"),
        sessionStartTime = rs.getTimestamp("session_start_time").toLocalDateTime(),
        sessionCreatedAt = rs.getTimestamp("session_created_at").toLocalDateTime(),
      )
    }
  }

  fun getDosageReportCsv(filter: ReportingFilter): String {
    val groupIds = getGroupIds(filter)
    val groupMembers = if (groupIds.isEmpty()) emptyList() else referralRepository.getDosageReportReferrals(groupIds)
    val attendanceByReferralId =
      (if (groupMembers.isEmpty()) emptyList() else sessionAttendanceRepository.getDosageAttendanceRows(groupMembers.map { it.referralId }))
        .groupBy { it.referralId }

    // PoPs who have not attended any sessions are left out
    val referrals = groupMembers.filter { it.referralId in attendanceByReferralId }
    val groupCodesByReferralId =
      (if (referrals.isEmpty()) emptyList() else programmeGroupMembershipRepository.getDosageGroupCodes(referrals.map { it.referralId }))
        .groupBy({ it.referralId }, { it.groupCode })

    // One column per group, sized to the PoP who has been assigned to the most groups
    val groupCodeColumnCount = groupCodesByReferralId.values.maxOfOrNull { it.size } ?: 1
    val sessionColumns = moduleSessionTemplateRepository.getBuildingChoicesSessionColumns().map { it.toSessionColumn() }
    val header = listOf("licReqNo", "crn") +
      (1..groupCodeColumnCount).map { "groupCode$it" } +
      listOf("regionName", "numberSessionAttended", "numberMainAttended", "numberCatchUpAttended") +
      sessionColumns.map { it.header }

    val lines = referrals.map { referral ->
      val attendedRows = attendanceByReferralId.getValue(referral.referralId)
      val groupCodes = groupCodesByReferralId[referral.referralId].orEmpty()

      val valuesBySession = attendedRows.groupBy {
        SessionColumn(
          moduleNumber = it.moduleNumber,
          sessionNumber = it.sessionNumber,
          sessionName = it.sessionName,
        )
      }.mapValues { (_, rows) ->
        rows.map { "${it.groupCode} - ${it.sessionStartAt.toLocalDate()} - ${if (it.sessionIsCatchup) "Catch Up" else "Non-Catch Up"}" }
          .distinct().sorted().joinToString(",")
      }

      listOf(referral.licReqNo.orEmpty(), referral.crn) +
        List(groupCodeColumnCount) { groupCodes.getOrElse(it) { "" } } +
        listOf(
          referral.regionName,
          attendedRows.countSessions { true },
          // Main sessions are group sessions rather than one-to-ones, and include catch-ups of group sessions
          attendedRows.countSessions { it.sessionType == SessionType.GROUP.name },
          attendedRows.countSessions { it.sessionIsCatchup },
        ) +
        sessionColumns.map { valuesBySession[it].orEmpty() }
    }

    return renderCsv(header, lines)
  }

  private fun List<DosageAttendanceProjection>.countSessions(predicate: (DosageAttendanceProjection) -> Boolean): String = filter(predicate).distinctBy { it.sessionId }.size.toString()

  private fun BuildingChoicesSessionColumnProjection.toSessionColumn() = SessionColumn(
    moduleNumber = moduleNumber,
    sessionNumber = sessionNumber,
    sessionName = sessionName,
  )

  private fun renderCsv(header: List<String>, rows: List<List<String>>): String {
    val headerLine = header.joinToString(",") { escapeCsv(it) }
    if (rows.isEmpty()) {
      return headerLine
    }

    val body = rows.joinToString("\n") { row -> row.joinToString(",") { escapeCsv(it) } }
    return "$headerLine\n$body"
  }

  private fun escapeCsv(value: String): String {
    if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
      return value
    }
    return "\"${value.replace("\"", "\"\"")}\""
  }

  data class FacilitatorContinuityReportRow(
    val code: String,
    val sessionNumber: Int,
    val sessionName: String,
    val sessionType: String,
    val isCatchUp: Boolean,
    val attendeeCount: Int,
    val facilitatorStaffCodes: String,
    val regionName: String,
    val deliveryLocationName: String,
    val probationDeliveryUnitName: String,
    val sessionStartTime: LocalDateTime,
    val sessionCreatedAt: LocalDateTime,
  )

  data class FacilitatorContinuityCsvRow(
    val code: String,
    val sessionNumber: Int,
    val sessionName: String,
    val sessionType: String,
    val isCatchUp: Boolean,
    val attendeeCount: Int,
    val facilitatorStaffCodes: String,
    val regionName: String,
    val deliveryLocationName: String,
    val probationDeliveryUnitName: String,
    val sessionStartTime: String,
    val sessionCreatedAt: String,
  )

  data class SessionColumn(
    val moduleNumber: Int,
    val sessionNumber: Int,
    val sessionName: String,
  ) {
    val header: String = "M$moduleNumber S$sessionNumber $sessionName"
  }

  data class SessionRateReportRow(
    val licReqNo: String,
    val crn: String,
    val groupCode: String,
    val regionName: String,
    val pduName: String,
    val deliveryLocationName: String,
    val weekStarting: LocalDate,
    val numberSessionAttended: Int,
    val numberSessionsNotAttended: Int,
    val numberSessionsScheduled: Int,
  )

  data class SessionRateCsvRow(
    val licReqNo: String,
    val crn: String,
    val groupCode: String,
    val regionName: String,
    val pduName: String,
    val deliveryLocationName: String,
    val weekStarting: String,
    val numberSessionAttended: Int,
    val numberSessionsNotAttended: Int,
    val numberSessionsScheduled: Int,
  )
}
