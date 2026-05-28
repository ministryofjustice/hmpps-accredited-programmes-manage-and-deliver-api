package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReportingGroupSizeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReportingGroupSizeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.sql.Types
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReportingService(
  private val reportingGroupSizeRepository: ReportingGroupSizeRepository,
  private val referralRepository: ReferralRepository,
  private val sessionAttendanceRepository: SessionAttendanceRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
  private val clock: Clock,
) {
  companion object {
    private const val GROUP_SIZE_CSV_HEADER =
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode"
    private const val FACILITATOR_CONTINUITY_CSV_HEADER =
      "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt"
    private const val SESSION_RATE_CSV_HEADER =
      "licReqNo,crn,groupCode,regionName,pduName,deliveryLocationName,weekStarting,numberSessionAttended,numberSessionsNotAttended,numberSessionsScheduled"
  }

  private val csvMapper = CsvMapper.builder().findAndAddModules().build().apply {
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  private val groupSizeCsvSchema: CsvSchema = CsvSchema.builder()
    .disableQuoteChar()
    .addColumn("id")
    .addColumn("code")
    .addColumn("createdAt")
    .addColumn("sex")
    .addColumn("cohort")
    .addColumn("isLdc")
    .addColumn("earliestPossibleStartDate")
    .addColumn("regionName")
    .addColumn("pduCode")
    .addColumn("pduName")
    .addColumn("locationCode")
    .addColumn("locationName")
    .addColumn("groupSize")
    .addColumn("facilitatorStaffCode")
    .build()

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

  fun getGroupSizeReportCsv(firstSessionAfter: LocalDateTime): String {
    val rows = getGroupSizeReportRows(firstSessionAfter)
    val csvRows = rows.map {
      GroupSizeCsvRow(
        id = it.id.toString(),
        code = it.code,
        createdAt = it.createdAt.toString(),
        sex = it.sex.toString(),
        cohort = it.cohort.toString(),
        isLdc = it.isLdc,
        earliestPossibleStartDate = it.earliestPossibleStartDate.toString(),
        regionName = it.regionName,
        pduCode = it.pduCode,
        pduName = it.pduName,
        locationCode = it.locationCode,
        locationName = it.locationName,
        groupSize = it.groupSize,
        facilitatorStaffCode = it.facilitatorStaffCode.orEmpty(),
      )
    }

    val csvBody = csvMapper.writer(groupSizeCsvSchema).writeValueAsString(csvRows).trimEnd('\n', '\r')
    return if (csvBody.isBlank()) GROUP_SIZE_CSV_HEADER else "$GROUP_SIZE_CSV_HEADER\n$csvBody"
  }

  private fun getGroupSizeReportRows(firstSessionAfter: LocalDateTime): List<ReportingGroupSizeEntity> = reportingGroupSizeRepository.getAllGroupsWithEarliestStartDateAfter(firstSessionAfter.toLocalDate())

  fun getSessionRate(
    groupsFinishedAfter: LocalDate?,
    groupsStartedAfter: LocalDate?,
  ): String {
    require(groupsFinishedAfter != null || groupsStartedAfter != null) {
      "At least one of groupsFinishedAfter or groupsStartedAfter must be provided"
    }

    val rows = getSessionRateRows(groupsFinishedAfter, groupsStartedAfter)
    val csvRows = rows.map {
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

  private fun getSessionRateRows(
    groupsFinishedAfter: LocalDate?,
    groupsStartedAfter: LocalDate?,
  ): List<SessionRateReportRow> {
    val sql =
      """
      WITH group_final_sessions AS (
        SELECT
          s.programme_group_id,
          MAX(s.starts_at::date) AS final_session_date
        FROM session s
        WHERE s.is_placeholder = FALSE
        GROUP BY s.programme_group_id
      ),
      eligible_groups AS (
        SELECT
          pg.id,
          pg.code,
          pg.region_name,
          pg.probation_delivery_unit_name,
          pg.delivery_location_name
        FROM programme_group pg
        LEFT JOIN group_final_sessions gfs ON gfs.programme_group_id = pg.id
        WHERE pg.deleted_at IS NULL
          AND (:applyGroupsStartedAfter = FALSE OR pg.earliest_possible_start_date >= :groupsStartedAfter)
          AND (
            :applyGroupsFinishedAfter = FALSE
            OR (gfs.final_session_date IS NOT NULL AND gfs.final_session_date >= :groupsFinishedAfter)
          )
      ),
      scheduled_sessions AS (
        SELECT
          r.id AS referral_id,
          COALESCE(r.event_id, '') AS lic_req_no,
          r.crn,
          s.id AS session_id,
          DATE_TRUNC('week', s.starts_at)::date AS week_starting,
          s.starts_at AS session_start_time,
          eg.code AS group_code,
          eg.region_name,
          eg.probation_delivery_unit_name AS pdu_name,
          eg.delivery_location_name
        FROM attendee a
        JOIN referral r ON r.id = a.referral_id
        JOIN session s ON s.id = a.session_id
        JOIN eligible_groups eg ON eg.id = s.programme_group_id
        WHERE s.is_placeholder = FALSE
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
      referral_week_bounds AS (
        SELECT
          referral_id,
          MIN(week_starting) AS first_week_starting,
          MAX(week_starting) AS last_week_starting
        FROM weekly_actual
        GROUP BY referral_id
      ),
      referral_weeks AS (
        SELECT
          rwb.referral_id,
          gs.week_starting::date AS week_starting
        FROM referral_week_bounds rwb
        CROSS JOIN LATERAL GENERATE_SERIES(
          rwb.first_week_starting,
          rwb.last_week_starting,
          INTERVAL '1 week'
        ) AS gs(week_starting)
      ),
      gap_weeks AS (
        SELECT
          rw.referral_id,
          lk.lic_req_no,
          lk.crn,
          lk.group_code,
          lk.region_name,
          lk.pdu_name,
          lk.delivery_location_name,
          rw.week_starting,
          0::INTEGER AS number_sessions_scheduled,
          0::INTEGER AS number_sessions_attended,
          0::INTEGER AS number_sessions_not_attended
        FROM referral_weeks rw
        LEFT JOIN weekly_actual wa
          ON wa.referral_id = rw.referral_id
          AND wa.week_starting = rw.week_starting
        JOIN LATERAL (
          SELECT
            wa2.lic_req_no,
            wa2.crn,
            wa2.group_code,
            wa2.region_name,
            wa2.pdu_name,
            wa2.delivery_location_name
          FROM weekly_actual wa2
          WHERE wa2.referral_id = rw.referral_id
            AND wa2.week_starting < rw.week_starting
          ORDER BY wa2.week_starting DESC, wa2.group_code
          LIMIT 1
        ) lk ON TRUE
        WHERE wa.referral_id IS NULL
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

    val defaultDate = LocalDate.parse("1900-01-01")
    val parameters = MapSqlParameterSource()
      .addValue("applyGroupsFinishedAfter", groupsFinishedAfter != null)
      .addValue("applyGroupsStartedAfter", groupsStartedAfter != null)
      .addValue("groupsFinishedAfter", groupsFinishedAfter ?: defaultDate, Types.DATE)
      .addValue("groupsStartedAfter", groupsStartedAfter ?: defaultDate, Types.DATE)

    return namedParameterJdbcTemplate.query(sql, parameters) { rs, _ ->
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

  fun getFacilitatorContinuityReportCsv(
    groupsCreatedSince: LocalDateTime?,
    firstSessionAtOrAfter: LocalDateTime?,
    lastSessionAtOrBefore: LocalDateTime?,
  ): String {
    val rows = getFacilitatorContinuityReportRows(groupsCreatedSince, firstSessionAtOrAfter, lastSessionAtOrBefore)
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

  private fun getFacilitatorContinuityReportRows(
    groupsCreatedSince: LocalDateTime?,
    firstSessionAtOrAfter: LocalDateTime?,
    lastSessionAtOrBefore: LocalDateTime?,
  ): List<FacilitatorContinuityReportRow> {
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
      group_happened_sessions AS (
        SELECT
          s.programme_group_id,
          MIN(s.starts_at) AS first_session_start,
          MAX(s.starts_at) AS last_session_start
        FROM session s
        JOIN programme_group pg ON pg.id = s.programme_group_id
        WHERE s.starts_at <= :now
          AND s.is_placeholder = FALSE
          AND pg.deleted_at IS NULL
        GROUP BY s.programme_group_id
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
      JOIN group_happened_sessions ghs ON ghs.programme_group_id = pg.id
      LEFT JOIN attendee_counts ac ON ac.session_id = s.id
      LEFT JOIN facilitator_codes fc ON fc.session_id = s.id
      WHERE s.starts_at <= :now
        AND s.is_placeholder = FALSE
        AND pg.deleted_at IS NULL
        AND (:applyGroupsCreatedSince = FALSE OR pg.created_at >= :groupsCreatedSince)
        AND (:applyFirstSessionAtOrAfter = FALSE OR ghs.first_session_start >= :firstSessionAtOrAfter)
        AND (:applyLastSessionAtOrBefore = FALSE OR ghs.last_session_start <= :lastSessionAtOrBefore)
      ORDER BY pg.code, s.starts_at, s.id
      """.trimIndent()

    val defaultTimestamp = LocalDateTime.parse("1900-01-01T00:00:00")
    val parameters = MapSqlParameterSource()
      .addValue("now", LocalDateTime.now(clock), Types.TIMESTAMP)
      .addValue("applyGroupsCreatedSince", groupsCreatedSince != null)
      .addValue("applyFirstSessionAtOrAfter", firstSessionAtOrAfter != null)
      .addValue("applyLastSessionAtOrBefore", lastSessionAtOrBefore != null)
      .addValue("groupsCreatedSince", groupsCreatedSince ?: defaultTimestamp, Types.TIMESTAMP)
      .addValue("firstSessionAtOrAfter", firstSessionAtOrAfter ?: defaultTimestamp, Types.TIMESTAMP)
      .addValue("lastSessionAtOrBefore", lastSessionAtOrBefore ?: defaultTimestamp, Types.TIMESTAMP)

    return namedParameterJdbcTemplate.query(sql, parameters) { rs, _ ->
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

  fun getDosageReportCsv(
    referralsCreatedSince: LocalDate?,
    referralsCompletedAfter: LocalDate?,
  ): String {
    require(referralsCreatedSince != null || referralsCompletedAfter != null) {
      "At least one of referralsCreatedSince or referralsCompletedAfter must be provided"
    }

    val referrals = referralRepository.getDosageReportReferrals(referralsCreatedSince, referralsCompletedAfter)
      .distinctBy { it.referralId }
      .sortedBy { it.crn }
    val referralIds = referrals.map { it.referralId }
    val attendanceRows =
      if (referralIds.isEmpty()) emptyList() else sessionAttendanceRepository.getDosageAttendanceRows(referralIds)
    val templateSessions = moduleSessionTemplateRepository.getBuildingChoicesSessionColumns()
    val sessionColumns = templateSessions.map {
      SessionColumn(
        moduleNumber = it.moduleNumber,
        sessionNumber = it.sessionNumber,
        sessionName = it.sessionName,
      )
    }
    val header = listOf("licReqNo", "crn", "numberSessionAttended") + sessionColumns.map { it.header }
    val attendanceByReferralId = attendanceRows.groupBy { it.referralId }

    val lines = referrals.map { referral ->
      val rowsForReferral = attendanceByReferralId[referral.referralId].orEmpty()
      val attendedSessionCount = rowsForReferral.map { it.sessionId }.toSet().size

      val valuesBySessionAndRegion = rowsForReferral.groupBy {
        SessionColumn(
          moduleNumber = it.moduleNumber,
          sessionNumber = it.sessionNumber,
          sessionName = it.sessionName,
        )
      }.mapValues { (_, attendedRows) ->
        attendedRows.map { it.groupCode }.distinct().sorted().joinToString(",")
      }

      listOf(referral.licReqNo.orEmpty(), referral.crn, attendedSessionCount.toString()) +
        sessionColumns.map { valuesBySessionAndRegion[it].orEmpty() }
    }

    return renderCsv(header, lines)
  }

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

  data class GroupSizeCsvRow(
    val id: String,
    val code: String,
    val createdAt: String,
    val sex: String,
    val cohort: String,
    val isLdc: Boolean,
    val earliestPossibleStartDate: String,
    val regionName: String,
    val pduCode: String,
    val pduName: String,
    val locationCode: String,
    val locationName: String,
    val groupSize: Int,
    val facilitatorStaffCode: String,
  )

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
