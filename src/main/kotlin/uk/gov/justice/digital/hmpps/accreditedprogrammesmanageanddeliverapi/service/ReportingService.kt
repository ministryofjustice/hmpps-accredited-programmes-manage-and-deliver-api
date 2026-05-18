package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReportingGroupSizeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReportingGroupSizeRepository
import java.time.ZoneId
import java.util.Date

@Service
class ReportingService(
  private val reportingGroupSizeRepository: ReportingGroupSizeRepository,
) {
  companion object {
    private const val CSV_HEADER =
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode"
  }

  fun getGroupSizeReportCsv(firstSessionAfter: Date): String {
    val rows = getGroupSizeReportRows(firstSessionAfter)

    val body = rows.joinToString("\n") { row ->
      listOf(
        row.id,
        row.code,
        row.createdAt,
        row.sex,
        row.cohort,
        row.isLdc,
        row.earliestPossibleStartDate,
        row.regionName,
        row.pduCode,
        row.pduName,
        row.locationCode,
        row.locationName,
        row.groupSize,
        row.facilitatorStaffCode.orEmpty(),
      ).joinToString(",") { value -> toCsvField(value) }
    }

    return if (body.isBlank()) CSV_HEADER else "$CSV_HEADER\n$body"
  }

  private fun getGroupSizeReportRows(firstSessionAfter: Date): List<ReportingGroupSizeEntity> {
    val firstSessionDate = firstSessionAfter.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return reportingGroupSizeRepository.getAllGroupsWithEarliestStartDateAfter(firstSessionDate)
  }

  private fun toCsvField(value: Any): String {
    val raw = value.toString()
    if (!raw.contains(',') && !raw.contains('"') && !raw.contains('\n') && !raw.contains('\r')) {
      return raw
    }

    return "\"${raw.replace("\"", "\"\"")}\""
  }
}
