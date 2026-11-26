package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReportingFilter
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReportingRegions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReportingService
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@ConditionalOnProperty(prefix = "reporting", name = ["enabled"], havingValue = "true")
class ReportingController(
  private val reportingService: ReportingService,
  private val clock: Clock,
) {
  companion object {
    private val fileNameDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")

    private const val CSV_MEDIA_TYPE = "text/csv"
    private val CSV_UTF_8_MEDIA_TYPE = MediaType("text", "csv", Charsets.UTF_8)

    /** Without this marker Excel does not read the file as UTF-8, and garbles names such as "Exploring life’s problems" */
    private const val UTF_8_BYTE_ORDER_MARK = "﻿"

    private const val GROUPS_START_DATE_DESCRIPTION =
      "Only include groups with a first Getting started 1 session on or after this date."
    private const val GROUPS_END_DATE_DESCRIPTION =
      "Only include groups with a first Getting started 1 session on or before this date."
    private const val SESSIONS_START_DATE_DESCRIPTION = "Only include sessions held on or after this date."
    private const val SESSIONS_END_DATE_DESCRIPTION = "Only include sessions held on or before this date."
    private const val REGION_DESCRIPTION =
      "Only include groups in this region, named as in nDelius. Leave out for all regions (national)."
    private const val START_DATE_EXAMPLE = "2026-04-01"
    private const val END_DATE_EXAMPLE = "2026-06-30"
    private const val REGION_EXAMPLE = "North East Region"
    private const val BAD_REQUEST_DESCRIPTION =
      "startDate or endDate is missing, endDate is before startDate, or the region is not recognised."
  }

  @Operation(
    tags = ["Reporting"],
    summary = "Download group size reporting data as CSV",
    operationId = "getGroupSizeReport",
    description = "Returns the size of each group, and the number of PoPs scheduled to attend each main session it has held.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing group size reporting data.",
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string", format = "binary"))],
      ),
      ApiResponse(
        responseCode = "400",
        description = BAD_REQUEST_DESCRIPTION,
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string"))],
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
    @Parameter(description = GROUPS_START_DATE_DESCRIPTION, required = true, example = START_DATE_EXAMPLE)
    @RequestParam(name = "startDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startDate: LocalDate?,
    @Parameter(description = GROUPS_END_DATE_DESCRIPTION, required = true, example = END_DATE_EXAMPLE)
    @RequestParam(name = "endDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    endDate: LocalDate?,
    @Parameter(description = REGION_DESCRIPTION, example = REGION_EXAMPLE)
    @RequestParam(name = "region", required = false)
    region: String?,
  ): ResponseEntity<String> {
    // TODO 2026-05-18 --TJWC: Add @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_REPORTING')") when reporting role rollout is complete.

    return csvResponse("group-size", startDate, endDate, region, reportingService::getGroupSizeReportCsv)
  }

  @Operation(
    tags = ["Reporting"],
    summary = "Download facilitator continuity reporting data as CSV",
    operationId = "getGroupFacilitatorContinuityReport",
    description = "Returns the facilitators for each session held between two dates.",
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
                value = "code,sessionNumber,sessionName,sessionType,isCatchUp,attendeeCount,facilitatorStaffCodes,region_name,delivery_location_name,probation_delivery_unit_name,sessionStartTime,sessionCreatedAt\\nGROUP01,1,Session 1,group,false,6,FAC123,North East Region,Leeds Office,Leeds PDU,2026-05-01 10:00,2026-04-28 12:00",
              ),
            ],
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = BAD_REQUEST_DESCRIPTION,
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string"))],
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
    @Parameter(description = SESSIONS_START_DATE_DESCRIPTION, required = true, example = START_DATE_EXAMPLE)
    @RequestParam(name = "startDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startDate: LocalDate?,
    @Parameter(description = SESSIONS_END_DATE_DESCRIPTION, required = true, example = END_DATE_EXAMPLE)
    @RequestParam(name = "endDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    endDate: LocalDate?,
    @Parameter(description = REGION_DESCRIPTION, example = REGION_EXAMPLE)
    @RequestParam(name = "region", required = false)
    region: String?,
  ): ResponseEntity<String> = csvResponse("facilitator-continuity", startDate, endDate, region, reportingService::getFacilitatorContinuityReportCsv)

  @Operation(
    tags = ["Reporting"],
    summary = "Download session rate reporting data as CSV",
    operationId = "getSessionRateReport",
    description = "Returns how many of the sessions held between two dates each PoP was scheduled for and attended in each week.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing session rate reporting data.",
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string", format = "binary"))],
      ),
      ApiResponse(
        responseCode = "400",
        description = BAD_REQUEST_DESCRIPTION,
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string"))],
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
    @Parameter(description = SESSIONS_START_DATE_DESCRIPTION, required = true, example = START_DATE_EXAMPLE)
    @RequestParam(name = "startDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startDate: LocalDate?,
    @Parameter(description = SESSIONS_END_DATE_DESCRIPTION, required = true, example = END_DATE_EXAMPLE)
    @RequestParam(name = "endDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    endDate: LocalDate?,
    @Parameter(description = REGION_DESCRIPTION, example = REGION_EXAMPLE)
    @RequestParam(name = "region", required = false)
    region: String?,
  ): ResponseEntity<String> = csvResponse("session-rate", startDate, endDate, region, reportingService::getSessionRateReportCsv)

  @Operation(
    tags = ["Reporting"],
    summary = "Download dosage reporting data as CSV",
    operationId = "getDosageReport",
    description = "Returns every session attended by each PoP who is, or has been, in one of the selected groups. This includes sessions they attended with other groups. PoPs who have not attended any sessions are left out.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "CSV file containing dosage reporting data.",
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string", format = "binary"))],
      ),
      ApiResponse(
        responseCode = "400",
        description = BAD_REQUEST_DESCRIPTION,
        content = [Content(mediaType = CSV_MEDIA_TYPE, schema = Schema(type = "string"))],
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
    @Parameter(description = GROUPS_START_DATE_DESCRIPTION, required = true, example = START_DATE_EXAMPLE)
    @RequestParam(name = "startDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startDate: LocalDate?,
    @Parameter(description = GROUPS_END_DATE_DESCRIPTION, required = true, example = END_DATE_EXAMPLE)
    @RequestParam(name = "endDate", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    endDate: LocalDate?,
    @Parameter(description = REGION_DESCRIPTION, example = REGION_EXAMPLE)
    @RequestParam(name = "region", required = false)
    region: String?,
  ): ResponseEntity<String> = csvResponse("dosage", startDate, endDate, region, reportingService::getDosageReportCsv)

  private fun csvResponse(
    reportName: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    region: String?,
    generateCsv: (ReportingFilter) -> String,
  ): ResponseEntity<String> {
    if (startDate == null || endDate == null) {
      return badRequest("startDate and endDate must be provided")
    }
    if (endDate.isBefore(startDate)) {
      return badRequest("endDate must be on or after startDate")
    }
    val regionName = region?.takeIf { it.isNotBlank() }
    if (regionName != null && regionName !in ReportingRegions.REGION_NAMES) {
      return badRequest("region must be one of: ${ReportingRegions.REGION_NAMES.joinToString(", ")}")
    }

    val csv = generateCsv(ReportingFilter(startDate, endDate, regionName))

    // The report name comes first because Excel names the tab after the file, cut to 31 characters
    val fileName = "$reportName-${LocalDateTime.now(clock).format(fileNameDateFormatter)}.csv"

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .contentType(CSV_UTF_8_MEDIA_TYPE)
      .body("$UTF_8_BYTE_ORDER_MARK$csv")
  }

  // A CSV client cannot be sent a JSON error, so the message is returned as plain text
  private fun badRequest(message: String): ResponseEntity<String> = ResponseEntity.badRequest()
    .contentType(CSV_UTF_8_MEDIA_TYPE)
    .body(message)
}
