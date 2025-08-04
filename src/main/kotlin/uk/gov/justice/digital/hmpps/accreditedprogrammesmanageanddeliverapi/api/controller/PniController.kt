package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.SexualOffenceCohortStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.PniService

@RestController
class PniController(
  private val pniService: PniService,
) {
  @Operation(
    tags = ["PNI Score"],
    summary = "Retrieve PNI Score",
    operationId = "getPniScoreByCrn",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The PNI Score and associated domain scores for this CRN",
        content = [Content(schema = Schema(implementation = PniScore::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this PNI Score.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The PNI Score does not exist for this CRN",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/pni-score/{crn}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPniScoreByCrn(
    @Parameter(description = "The unique crn of an individual", required = true)
    @PathVariable("crn") crn: String,
  ) = pniService.getPniScore(crn)

  @Operation(
    tags = ["PNI Score"],
    summary = "Determine if the individual is in the sexual offence cohort",
    operationId = "isSexualOffenceCohort",
    description = "Returns true if the individual's PNI score indicates they are in the sexual offence cohort",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Whether the individual is in the sexual offence cohort",
        content = [Content(schema = Schema(implementation = SexualOffenceCohortStatus::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this information.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "PNI score not found for this CRN",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")]
  )
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/pni-score/{crn}/sexual-offence-cohort", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun isSexualOffenceCohort(
    @Parameter(description = "The unique crn of an individual", required = true)
    @PathVariable("crn") crn: String,
  ): SexualOffenceCohortStatus {
    val inCohort = pniService.isSexualOffenceCohort(crn)
    return SexualOffenceCohortStatus(inSexualOffenceCohort = inCohort)
  }

}
