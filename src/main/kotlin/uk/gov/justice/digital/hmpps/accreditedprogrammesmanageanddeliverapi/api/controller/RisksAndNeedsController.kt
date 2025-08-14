package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.annotation.HandlerMethodValidationException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.NomisIdOrCrnRegex.NOMIS_ID_CRN_REGEX
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.RisksAndNeedsService

@Tag(
  name = "Risk and Needs controller",
  description = "A series of endpoints to populate the risks and needs sections for the Referral details page",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class RisksAndNeedsController(private val risksAndNeedsService: RisksAndNeedsService) {

  @Operation(
    tags = ["Oasys Integration"],
    summary = "Risks details as held by Oasys",
    operationId = "getRisksByNomisIdOrCrn",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Risk details held by Oasys",
        content = [Content(schema = Schema(implementation = Risks::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255 or PrisonNumber: A1234AA",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The risks and needs information does not exist for the nomisId or CRN",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{nomisIdOrCrn}/risks-and-alerts"],
    produces = ["application/json"],
  )
  fun getRisksByNomisIdOrCrn(
    @Pattern(
      regexp = NOMIS_ID_CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255 or PrisonNumber: A1234AA",
    )
    @Parameter(
      description = "Prison nomis identifier or CRN",
      required = true,
    )
    @PathVariable("nomisIdOrCrn") nomisIdOrCrn: String,
  ): ResponseEntity<Risks> = ResponseEntity.ok(risksAndNeedsService.getRisksByNomisIdOrCrn(nomisIdOrCrn))
}
