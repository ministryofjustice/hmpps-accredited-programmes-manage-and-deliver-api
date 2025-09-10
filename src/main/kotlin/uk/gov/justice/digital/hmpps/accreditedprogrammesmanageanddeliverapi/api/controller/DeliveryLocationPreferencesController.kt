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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferencesFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DeliveryLocationPreferencesService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class DeliveryLocationPreferencesController(
  private val deliveryLocationPreferencesService: DeliveryLocationPreferencesService,
) {

  @Operation(
    tags = ["Delivery Location Preferences"],
    summary = "A Backend-For-Frontend endpoint for the multi-page Delivery Location Preferences form",
    operationId = "getDeliveryLocationPreferencesFormData",
    description = """
      Retrieves all the data needed for the multi-page Delivery Location Preferences form, for a Referral:
      - Person on Probation summary information (from nDelius)
      - Existing delivery location preferences (or `null`)
      - Primary PDU delivery locations for the Manager associated with the Referral (from nDelius)
      - Other PDUs in the same region (from nDelius)
    """,
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Delivery Location Preferences form data",
        content = [Content(schema = Schema(implementation = DeliveryLocationPreferencesFormData::class))],
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
        description = "The referral does not exist or required data could not be found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/referral-delivery-location-preferences-form/{referral_id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getDeliveryLocationPreferencesFormData(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("referral_id") referralId: UUID,
  ): ResponseEntity<DeliveryLocationPreferencesFormData> {
    val formData = deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralId)
    return ResponseEntity.ok(formData)
  }
}
