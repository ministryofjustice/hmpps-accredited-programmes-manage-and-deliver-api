package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralStatusService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class ReferralStatusController(
  private val referralStatusService: ReferralStatusService,
) {

  @Operation(
    tags = ["Referral Status"],
    summary = "Retrieve data for updating referral status form",
    operationId = "getReferralStatusForm",
    description = "Returns all possible data for the update referral status form based on the referral id",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Data for update referral status form",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ReferralStatusFormData::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this resource.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/referral-status-form/{referralId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getReferralStatusForm(
    @Parameter(description = "The id (UUID) of a referral status description", required = true)
    @PathVariable referralId: UUID,
  ): ResponseEntity<ReferralStatusFormData> = referralStatusService.getReferralStatusFormDataFromReferralId(referralId)
    ?.let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("Referral status history for referral with id $referralId not found")
}
