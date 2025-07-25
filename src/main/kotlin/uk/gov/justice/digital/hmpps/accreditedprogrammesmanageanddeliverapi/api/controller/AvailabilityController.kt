package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update.UpdateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AvailabilityService
import java.util.UUID

@RestController
@Tag(
  name = "Availability",
  description = """
    The endpoints fetches the availability details
  """,
)
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class AvailabilityController(private val availabilityService: AvailabilityService) {

  @Operation(
    tags = ["Availability"],
    summary = "Get all availabilities for a referral",
    operationId = "getAvailabilityByReferralId",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the availability for a given referral",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/availability/referral/{referralId}"],
    produces = ["application/json"],
  )
  fun getAvailabilityByReferralId(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("referralId") referralId: UUID,
  ): ResponseEntity<Availability> = ResponseEntity
    .ok(
      availabilityService.getAvailability(referralId),
    )

  @Operation(
    tags = ["Referrals"],
    summary = "Create a new availability",
    operationId = "createAvailability",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Availability created",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/availability"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun createAvailability(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody createAvailability: CreateAvailability,
  ): ResponseEntity<Availability> {
    val availabilityResponse = availabilityService.createAvailability(createAvailability)

    if (availabilityResponse.second) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(availabilityResponse.first)
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(availabilityResponse.first)
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Update availability",
    operationId = "updateAvailability",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Availability updated",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = Availability::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/availability"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun updateAvailability(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody updateAvailability: UpdateAvailability,
  ): ResponseEntity<Availability> {
    val availability = availabilityService.updateAvailability(updateAvailability)

    return ResponseEntity.status(HttpStatus.OK).body(availability)
  }
}
