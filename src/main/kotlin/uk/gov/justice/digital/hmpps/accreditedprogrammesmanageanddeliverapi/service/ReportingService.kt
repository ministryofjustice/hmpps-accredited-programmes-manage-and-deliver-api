package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
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
    private const val GROUP_SIZE_CSV_HEADER =
      "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode"
  }

  private val csvMapper = CsvMapper.builder().findAndAddModules().build().apply {
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  private val groupSizeCsvSchema: CsvSchema = CsvSchema.builder()
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

  fun getGroupSizeReportCsv(firstSessionAfter: Date): String {
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

  private fun getGroupSizeReportRows(firstSessionAfter: Date): List<ReportingGroupSizeEntity> {
    val firstSessionDate = firstSessionAfter.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return reportingGroupSizeRepository.getAllGroupsWithEarliestStartDateAfter(firstSessionDate)
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
}
