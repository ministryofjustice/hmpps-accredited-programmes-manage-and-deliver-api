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
    private const val FACILITATOR_CONTINUITY_FILE_NAME_SUFFIX = "facilitator-continuity.csv"
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
    summary = "Download facilitator continuity reporting data as CSV",
    operationId = "getGroupFacilitatorContinuityReport",
    description = "Returns facilitator continuity reporting data for happened sessions where at least one filter is provided.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing facilitator continuity reporting data.",
        content = [
          Content(
            mediaType = CSV_MEDIA_TYPE,
            schema = Schema(type = "string", format = "binary"),
            examples = [
              ExampleObject(
                name = "facilitator-continuity-csv-example",
                value = "code,region,location,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,sessionStartTime,sessionCreatedAt\\nGROUP01,North East,HMP Example,1,Session 1,group,false,6,FAC123,2026-05-01 10:00,2026-04-28 12:00",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid query parameters, or no query parameters provided.",
        content = [
          Content(
            mediaType = CSV_MEDIA_TYPE,
            schema = Schema(type = "string"),
            examples = [
              ExampleObject(
                name = "facilitator-continuity-bad-request-example",
                value = "At least one query parameter must be provided.",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/reporting/facilitator-continuity.csv", produces = [CSV_MEDIA_TYPE])
  fun getFacilitatorContinuityCsv(
    @Parameter(
      description = "Only include groups created at or after this date-time.",
      required = false,
      example = "2026-05-01T00:00:00",
    )
    @RequestParam(name = "groupsCreatedSince", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    groupsCreatedSince: LocalDateTime?,
    @Parameter(
      description = "Only include groups where first happened session is at or after this date-time.",
      required = false,
      example = "2026-05-01T00:00:00",
    )
    @RequestParam(name = "firstSessionAtOrAfter", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    firstSessionAtOrAfter: LocalDateTime?,
    @Parameter(
      description = "Only include groups where last happened session is at or before this date-time.",
      required = false,
      example = "2026-06-30T23:59:59",
    )
    @RequestParam(name = "lastSessionAtOrBefore", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    lastSessionAtOrBefore: LocalDateTime?,
  ): ResponseEntity<String> {
    if (groupsCreatedSince == null && firstSessionAtOrAfter == null && lastSessionAtOrBefore == null) {
      return ResponseEntity.badRequest()
        .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
        .body("At least one of groupsCreatedSince, firstSessionAtOrAfter, lastSessionAtOrBefore must be provided")
    }

    val csv = reportingService.getFacilitatorContinuityReportCsv(
      groupsCreatedSince = groupsCreatedSince,
      firstSessionAtOrAfter = firstSessionAtOrAfter,
      lastSessionAtOrBefore = lastSessionAtOrBefore,
    )

    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$FACILITATOR_CONTINUITY_FILE_NAME_SUFFIX"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
      .body(csv)
  }

  @Operation(
    tags = ["Reporting"],
    summary = "Download session rate reporting data as CSV",
    operationId = "getSessionRateReport",
    description = "Returns weekly session rate data where at least one group filter is provided.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing session rate reporting data.",
        content = [
          Content(
            mediaType = CSV_MEDIA_TYPE,
            schema = Schema(type = "string", format = "binary"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid query parameters. At least one of groupsFinishedAfter or groupsStartedAfter must be provided.",
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
  @GetMapping("/reporting/session-rate.csv", produces = [CSV_MEDIA_TYPE])
  fun getSessionRateCsv(
    @Parameter(
      description = "Only include groups with final session date on or after this date.",
      required = false,
      example = "2026-05-01",
    )
    @RequestParam(name = "groupsFinishedAfter", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    groupsFinishedAfter: LocalDate?,
    @Parameter(
      description = "Only include groups with earliest possible start date on or after this date.",
      required = false,
      example = "2026-05-01",
    )
    @RequestParam(name = "groupsStartedAfter", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    groupsStartedAfter: LocalDate?,
  ): ResponseEntity<String> {
    if (groupsFinishedAfter == null && groupsStartedAfter == null) {
      return ResponseEntity.badRequest()
        .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
        .body("At least one of groupsFinishedAfter or groupsStartedAfter must be provided")
    }

    val csv = reportingService.getSessionRate(groupsFinishedAfter, groupsStartedAfter)
    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$DOSAGE_FILE_NAME_SUFFIX"

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

    val csv = reportingService.getDosageReportCsv(referralsCreatedSince, referralsCompletedAfter)
    val fileName = "${LocalDateTime.now(clock).format(fileNameDateFormatter)}-$DOSAGE_FILE_NAME_SUFFIX"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(MediaType.parseMediaType(CSV_MEDIA_TYPE))
      .body(csv)
  }
}
