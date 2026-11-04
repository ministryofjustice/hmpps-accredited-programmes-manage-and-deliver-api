package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralOnProgrammeStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService

@RestController
@PreAuthorize("hasAnyRole('ACCREDITED_PROGRAMMES__MANAGE_AND_DELIVER_OCI_READ_ONLY')")
class ReferralStatusController(
  private val referralService: ReferralService,
) {
  @Operation(
    tags = ["Referral Status"],
    summary = "Retrieve the latest on-programme status for a referral",
    operationId = "getOnProgrammeStatusForReferral",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The referral on-programme status",
        content = [Content(schema = Schema(implementation = ReferralOnProgrammeStatus::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden",
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
  @GetMapping("/referral/{crn}/latest-status", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getOnProgrammeStatusForReferral(
    @PathVariable @Parameter(description = "The crn of a referral", required = true) crn: String,
  ): ResponseEntity<ReferralOnProgrammeStatus> = referralService.getReferralOnProgrammeStatus(crn)?.let {
    ResponseEntity.ok(it)
  } ?: throw NotFoundException("Referral with id $crn not found")
}
