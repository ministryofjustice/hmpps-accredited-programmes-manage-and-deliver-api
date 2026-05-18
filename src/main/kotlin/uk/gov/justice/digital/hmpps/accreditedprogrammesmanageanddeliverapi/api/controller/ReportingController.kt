package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.constraints.Past
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReportingService
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@RestController
@Validated
@ConditionalOnProperty(prefix = "reporting", name = ["enabled"], havingValue = "true")
class ReportingController(
  private val reportingService: ReportingService,
  private val clock: Clock,
) {
  companion object {
    private val fileNameDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")

    private const val CSV_MEDIA_TYPE = "text/csv"
    private const val GROUP_SIZE_FILE_NAME_SUFFIX = "manage-and-deliver-group-size.csv"
  }

  @Operation(
    tags = ["Reporting"],
    summary = "Download group size reporting data as CSV",
    operationId = "getGroupSizeReport",
    description = "Returns group size reporting data where the earliest possible start date is after groupStartedSince.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing group size reporting data.",
        content = [
          Content(
            mediaType = CSV_MEDIA_TYPE,
            schema = Schema(type = "string", format = "binary"),
            examples = [
              ExampleObject(
                name = "group-size-csv-example",
                value = "id,code,createdAt,sex,cohort,isLdc,earliestPossibleStartDate,regionName,pduCode,pduName,locationCode,locationName,groupSize,facilitatorStaffCode\\n123e4567-e89b-12d3-a456-426614174000,GROUP01,2026-05-01T10:00,MIXED,SEXUAL_OFFENCE,true,2026-05-20,North East,LDS01,Leeds PDU,LOC01,Leeds Office,1,FAC123",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid or non-past groupStartedSince query parameter.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/reporting/group-size.csv", produces = [CSV_MEDIA_TYPE])
  fun getGroupSizeCsv(
    @Parameter(
      description = "Only include groups that started after this date-time (must be in the past).",
      required = true,
      example = "2026-05-18T13:30:00",
    )
    @RequestParam(name = "groupStartedSince", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Past(message = "groupStartedSince must be in the past")
    groupStartedSince: LocalDateTime,
  ): ResponseEntity<String> {
    // TODO 2026-05-18 --TJWC: Add @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_REPORTING')") when reporting role rollout is complete.

    val csv = reportingService.getGroupSizeReportCsv(
      Date.from(groupStartedSince.atZone(ZoneId.systemDefault()).toInstant()),
    )

    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$GROUP_SIZE_FILE_NAME_SUFFIX"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
      .body(csv)
  }
}
