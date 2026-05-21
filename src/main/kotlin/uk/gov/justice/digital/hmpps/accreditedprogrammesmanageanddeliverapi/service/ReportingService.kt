package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReportingGroupSizeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReportingGroupSizeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ReportingService(
  private val reportingGroupSizeRepository: ReportingGroupSizeRepository,
  private val referralRepository: ReferralRepository,
  private val sessionAttendanceRepository: SessionAttendanceRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
) {
  companion object {
    private const val GROUP_SIZE_CSV_HEADER =
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode"
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

  fun getGroupFaciltiatorContinutiyReport(
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
    val attendanceRows = if (referralIds.isEmpty()) emptyList() else sessionAttendanceRepository.getDosageAttendanceRows(referralIds)
    val templateSessions = moduleSessionTemplateRepository.getBuildingChoicesSessionColumns()
    val regions = referrals.map { it.regionName }.distinct().sorted()
    val sessionColumns = regions.flatMap { region ->
      templateSessions.map {
        SessionColumn(
          moduleNumber = it.moduleNumber,
          sessionNumber = it.sessionNumber,
          sessionName = it.sessionName,
        )
      }
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

  data class SessionColumn(
    val moduleNumber: Int,
    val sessionNumber: Int,
    val sessionName: String,
  ) {
    val header: String = "M$moduleNumber S$sessionNumber $sessionName"
  }
}
