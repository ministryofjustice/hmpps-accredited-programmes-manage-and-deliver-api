package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.GroupAllocationNotesService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class GroupAllocationNotesController(
  private val groupAllocationNotesService: GroupAllocationNotesService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve motivations background and non-associations of a referral",
    operationId = "getReferralMotivationBackgroundAndNonAssociationsByReferralId",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the motivations background and non-associations of the referral",
        content = [Content(schema = Schema(implementation = ReferralDetails::class))],
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
  @GetMapping("/referral/{id}/motivation-background-non-associations", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getReferralMotivationBackgroundAndNonAssociationsByReferralId(
    @Parameter(description = "The id (UUID) of a referral", required = true)
    @PathVariable("id") id: UUID,
  ): ResponseEntity<ReferralMotivationBackgroundAndNonAssociations> = groupAllocationNotesService.getReferralMotivationBackgroundAndNonAssociationsByReferralId(id)
    .let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("No motivation, background and non-associations information found for referral id: $id")
}
