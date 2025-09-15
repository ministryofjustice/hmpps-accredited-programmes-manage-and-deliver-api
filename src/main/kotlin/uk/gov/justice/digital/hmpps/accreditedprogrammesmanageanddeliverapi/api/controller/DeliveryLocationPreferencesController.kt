package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.DeliveryLocationPreferencesFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DeliveryLocationPreferencesService
import java.util.UUID

@Tag(
  name = "Delivery Location Preferences controller",
  description = "A series of endpoints to populate and retrieve the Delivery Location Preferences for a referral",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class DeliveryLocationPreferencesController(
  private val deliveryLocationPreferencesService: DeliveryLocationPreferencesService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(

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
  @GetMapping(
    "/bff/referral-delivery-location-preferences-form/{referralId}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun getDeliveryLocationPreferencesFormData(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("referralId") referralId: UUID,
  ): ResponseEntity<DeliveryLocationPreferencesFormData> {
    val formData = deliveryLocationPreferencesService.getDeliveryLocationPreferencesFormDataForReferral(referralId)
    return ResponseEntity.ok(formData)
  }

  @Operation(

    summary = "Create Delivery Location Preferences for a referral",
    operationId = "createDeliveryLocationPreferencesForReferral",
    description = "Create Delivery Location Preferences for a referral",
    responses =
    [
      ApiResponse(
        responseCode = "201",
        description = "Delivery Location Preferences created",
        content = [Content(schema = Schema(implementation = Void::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad Request. Blank or missing values",
        content = [Content(schema = Schema(implementation = MethodArgumentNotValidException::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "Conflict. Delivery location preferences already exist for this referral",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist for the provider referralId",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping(
    "/delivery-location-preferences/referral/{referralId}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun createDeliveryLocationPreferencesForReferral(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("referralId") referralId: UUID,
    @Parameter(
      description = "The delivery location preferences for a referral",
      required = true,
    ) @Valid @RequestBody createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): ResponseEntity<Void> {
    log.info("Received request to set Delivery Location preferences for ReferralId: $referralId")
    deliveryLocationPreferencesService.createDeliveryLocationPreferences(
      referralId,
      createDeliveryLocationPreferences,
    )

    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @Operation(

    summary = "Update Delivery Location Preferences for a referral",
    operationId = "updateDeliveryLocationPreferencesForReferral",
    description = "Update Delivery Location Preferences for a referral",
    responses =
    [
      ApiResponse(
        responseCode = "200",
        description = "Delivery Location Preferences updated",
        content = [Content(schema = Schema(implementation = Void::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad Request. Blank or missing values",
        content = [Content(schema = Schema(implementation = MethodArgumentNotValidException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist or delivery location preferences do not exist for this referral",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PutMapping(
    "/delivery-location-preferences/referral/{referralId}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun updateDeliveryLocationPreferencesForReferral(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("referralId") referralId: UUID,
    @Parameter(
      description = "The delivery location preferences for a referral",
      required = true,
    ) @Valid @RequestBody createDeliveryLocationPreferences: CreateDeliveryLocationPreferences,
  ): ResponseEntity<Void> {
    log.info("Received request to update Delivery Location preferences for Referral Id: $referralId")
    deliveryLocationPreferencesService.updateDeliveryLocationPreferences(
      referralId,
      createDeliveryLocationPreferences,
    )

    return ResponseEntity.ok().build()
  }
}
