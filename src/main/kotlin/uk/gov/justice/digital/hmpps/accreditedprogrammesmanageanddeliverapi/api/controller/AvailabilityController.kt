package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AvailabilityService
import java.util.UUID

@RestController
@RequestMapping("availability")
@Tag(
  name = "Availability",
  description = """
    This endpoint will refresh all of the prisoners within ACP - BE GENTLE.
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
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referral/{referralId}"],
    produces = ["application/json"],
  )
  fun getAvailabilityByReferralId(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("referralId") referralId: UUID,
  ): ResponseEntity<Availability> = ResponseEntity
    .ok(
      availabilityService.getAvailableSlots(referralId),
    )
}
