package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralCompletionData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralStatusService
import java.util.UUID

/**
 * This controller is secured by a different role other endpoints as it will be called from external services
 * and we want to restrict any access they may have to only READ data.
 */
@RestController
@PreAuthorize("hasAnyRole('ACCREDITED_PROGRAMMES__MANAGE_AND_DELIVER__READ_ONLY')")
class EventDetailsController(
  private val referralStatusService: ReferralStatusService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Event Details"],
    summary = "Retrieve status change details for a referral",
    operationId = "getStatusChangeDetails",
    description = "Returns details of the most recent status change for a given referral. Secured with a read-only role for use by external services.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Status change details for the referral",
        content = [Content(schema = Schema(implementation = ReferralStatusInfo::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral/{referralId}/status-change-details")
  fun getStatusChangeDetails(
    @PathVariable @Parameter(description = "The id (UUID) of a referral", required = true) referralId: UUID,
  ): ResponseEntity<ReferralStatusInfo> {
    log.info("Request to retrieve details of status change for referral with id: $referralId")
    val statusInfo = referralStatusService.getStatusChangeDetailsForReferral(referralId)

    return ResponseEntity.ok(statusInfo)
  }

  @Operation(
    tags = ["Event Details"],
    summary = "Retrieve completion data for a referral",
    operationId = "getCompletionData",
    description = "Returns completion data for a given referral. Secured with a read-only role for use by external services.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Completion data for the referral",
        content = [Content(schema = Schema(implementation = ReferralCompletionData::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/referral/{referralId}/completion-data")
  fun getCompletionData(
    @PathVariable @Parameter(description = "The id (UUID) of a referral", required = true) referralId: UUID,
  ): ResponseEntity<ReferralCompletionData> {
    log.info("Request to retrieve completion data for referral with id: $referralId")
    val statusInfo = referralStatusService.getCompletionDataForReferral(referralId)

    return ResponseEntity.ok(statusInfo)
  }
}
