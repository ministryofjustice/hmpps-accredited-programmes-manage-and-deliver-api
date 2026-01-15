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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.CrnRegex.CRN_REGEX
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.AlcoholMisuseDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Attitude
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.DrugDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.EmotionalWellbeing
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LearningNeeds
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Relationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.ThinkingAndBehaviour
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.RisksAndNeedsService

@Tag(
  name = "Risk and Needs controller",
  description = "A series of endpoints to populate the risks and needs sections for the Referral details page",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class RisksAndNeedsController(private val risksAndNeedsService: RisksAndNeedsService) {

  @Operation(
    summary = "Risks details as held by Oasys",
    operationId = "getRisksByCrn",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Risk details held by Oasys",
        content = [Content(schema = Schema(implementation = Risks::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The risks and needs information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/risks-and-alerts"],
    produces = ["application/json"],
  )
  fun getRisksByCrn(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<Risks> = ResponseEntity.ok(risksAndNeedsService.getRisksByCrn(crn))

  @Operation(
    summary = "Retrieve a person's Learning needs as held in Oasys",
    operationId = "getLearningNeeds",
    description = "",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Learning needs held by Oasys",
        content = [Content(schema = Schema(implementation = LearningNeeds::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The learning needs information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/learning-needs"],
    produces = ["application/json"],
  )
  fun getLearningNeeds(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<LearningNeeds> = ResponseEntity.ok(risksAndNeedsService.getLearningNeedsForCrn(crn))

  @Operation(
    summary = "Health details as held by Oasys",
    operationId = "getHealth",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Health::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The health information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/health"],
    produces = ["application/json"],
  )
  fun getHealth(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<Health> = ResponseEntity
    .ok(
      risksAndNeedsService
        .getHealth(crn),
    )

  @Operation(
    summary = "Retrieve a person's relationship details as held in Oasys",
    operationId = "getRelationships",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Relationship details held by Oasys",
        content = [Content(schema = Schema(implementation = Relationships::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The relationship information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/relationships"],
    produces = ["application/json"],
  )
  fun getRelationships(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<Relationships> = ResponseEntity.ok(risksAndNeedsService.getRelationshipsForCrn(crn))

  @Operation(
    summary = "ROSH details as held by Oasys",
    operationId = "getRoshAnalysisByCrn",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "ROSH details held by Oasys",
        content = [Content(schema = Schema(implementation = RoshAnalysis::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The ROSH information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/rosh-analysis"],
    produces = ["application/json"],
  )
  fun getRoshAnalysisByCrn(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<RoshAnalysis> = ResponseEntity.ok(risksAndNeedsService.getRoshFullForCrn(crn))

  @Operation(
    summary = "Offence Analysis details as held by Oasys",
    operationId = "getOffenceAnalysisByCrn",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Offence analysis details held by Oasys",
        content = [Content(schema = Schema(implementation = OffenceAnalysis::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
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
        description = "The Offence analysis does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/offence-analysis"],
    produces = ["application/json"],
  )
  fun getOffenceAnalysisForCrn(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<OffenceAnalysis> = ResponseEntity.ok(risksAndNeedsService.getOffenceAnalysis(crn))

  @Operation(
    summary = "Get drug details as held by Oasys",
    operationId = "getDrugDetails",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = DrugDetails::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access drug details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The drug detail information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/drug-details"],
    produces = ["application/json"],
  )
  fun getDrugDetails(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<DrugDetails> = ResponseEntity
    .ok(
      risksAndNeedsService
        .getDrugDetails(crn),
    )

  @Operation(
    summary = "Lifestyle and Associate details as held by Oasys",
    operationId = "getLifestyleAndAssociates",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = LifestyleAndAssociates::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
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
        description = "The health information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/lifestyle-and-associates"],
    produces = ["application/json"],
  )
  fun getLifestyleAndAssociates(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<LifestyleAndAssociates> = ResponseEntity
    .ok(
      risksAndNeedsService
        .getLifestyleAndAssociates(crn),
    )

  @Operation(
    summary = "Get alcohol misuse details as held by Oasys",
    operationId = "getAlcoholMisuseDetails",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = AlcoholMisuseDetails::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access alcohol misuse details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The alcohol misuse detail information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/alcohol-misuse-details"],
    produces = ["application/json"],
  )
  fun getAlcoholMisuseDetails(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<AlcoholMisuseDetails> = ResponseEntity
    .ok(risksAndNeedsService.getAlcoholMisuseDetails(crn))

  @Operation(
    summary = "Get emotional wellbeing details as held by Oasys",
    operationId = "getEmotionalWellbeing",
    description = """Fetch emotional needs of the person based on crn""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = EmotionalWellbeing::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access emotional wellbeing details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The emotional wellbeing detail information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/emotional-wellbeing"],
    produces = ["application/json"],
  )
  fun getEmotionalWellbeingDetails(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<EmotionalWellbeing> = ResponseEntity
    .ok(
      risksAndNeedsService
        .getEmotionalWellbeing(crn),
    )

  @Operation(
    summary = "Get thinking and behaviour details as held by Oasys",
    operationId = "getThinkingAndBehaviourDetails",
    description = """Fetch thinking and behaviour data """,
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = ThinkingAndBehaviour::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access thinking and behaviour details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The thinking and behaviour details information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/thinking-and-behaviour"],
    produces = ["application/json"],
  )
  fun getThinkingAndBehaviourDetails(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<ThinkingAndBehaviour> = ResponseEntity
    .ok(
      risksAndNeedsService
        .getThinkingAndBehaviour(crn),
    )

  @Operation(
    summary = "Get attitude details as held by Oasys",
    operationId = "getAttitude",
    description = """Fetch attitude data""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = Attitude::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid code format. Expected format for CRN: X718255.",
        content = [Content(schema = Schema(implementation = HandlerMethodValidationException::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access attitude details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The attitude detail information does not exist for the CRN provided.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/risks-and-needs/{crn}/attitude"],
    produces = ["application/json"],
  )
  fun getAttitude(
    @PathVariable @Pattern(
      regexp = CRN_REGEX,
      message = "Invalid code format. Expected format for CRN: X718255",
    )
    @Parameter(
      description = "CRN",
      required = true,
    ) crn: String,
  ): ResponseEntity<Attitude> = ResponseEntity.ok(risksAndNeedsService.getAttitude(crn))
}
