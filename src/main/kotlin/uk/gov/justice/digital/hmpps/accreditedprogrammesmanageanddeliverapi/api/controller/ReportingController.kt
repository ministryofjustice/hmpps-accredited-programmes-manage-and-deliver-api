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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    private const val DOSAGE_FILE_NAME_SUFFIX = "dosage.csv"
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

    val csv = reportingService.getGroupSizeReportCsv(groupStartedSince)

    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$GROUP_SIZE_FILE_NAME_SUFFIX"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
      .body(csv)
  }

  @Operation(
    tags = ["Reporting"],
    summary = "Download dosage reporting data as CSV",
    operationId = "getDosageReport",
    description = "Returns dosage reporting data for Building Choices referrals filtered by either creation date or completed date.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing dosage reporting data.",
        content = [
          Content(
            mediaType = CSV_MEDIA_TYPE,
            schema = Schema(type = "string", format = "binary"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid query parameters. At least one of referralsCreatedSince or referralsCompletedAfter must be provided.",
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
  @GetMapping("/reporting/dosage.csv", produces = [CSV_MEDIA_TYPE])
  fun getDosageCsv(
    @Parameter(
      description = "Only include referrals created after this date.",
      required = false,
      example = "2026-05-01",
    )
    @RequestParam(name = "referralsCreatedSince", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    referralsCreatedSince: LocalDate?,
    @Parameter(
      description = "Only include referrals with completed status set after this date.",
      required = false,
      example = "2026-05-01",
    )
    @RequestParam(name = "referralsCompletedAfter", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    referralsCompletedAfter: LocalDate?,
  ): ResponseEntity<String> {
    if (referralsCreatedSince == null && referralsCompletedAfter == null) {
      return ResponseEntity.badRequest()
        .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
        .body("At least one of referralsCreatedSince or referralsCompletedAfter must be provided")
    }

    val csv = reportingService.getGroupFaciltiatorContinutiyReport(referralsCreatedSince, referralsCompletedAfter)
    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$DOSAGE_FILE_NAME_SUFFIX"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
      .body(csv)
  }
}
