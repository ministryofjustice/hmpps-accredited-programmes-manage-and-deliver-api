package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralStatusService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@Tag(
  name = "Programme Group controller",
  description = "A series of endpoints to retrieve and manage group details and allocations",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class ProgrammeGroupController(
  private val programmeGroupService: ProgrammeGroupService,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val programmeGroupMembershipService: ProgrammeGroupMembershipService,
  private val referralStatusService: ReferralStatusService,
) {

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Get group details with allocation and waitlist data",
    operationId = "getGroupDetails",
    description = "Retrieve group details including allocation and waitlist data with filtering and pagination support",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Group details retrieved successfully",
        content = [Content(schema = Schema(implementation = ProgrammeGroupDetails::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this group.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The group does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "The group already exists",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/group/{groupId}/{selectedTab}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getGroupDetails(
    @PageableDefault(
      page = 0,
      size = 10,
      sort = ["sentenceEndDate"],
      direction = Sort.Direction.ASC,
    ) pageable: Pageable,
    @Parameter(description = "The id (UUID) of a group", required = true)
    @PathVariable("groupId") groupId: UUID,
    @Parameter(description = "Return table data for either the allocated tab or the waitlist tab")
    @PathVariable("selectedTab") selectedTab: GroupPageTab,
    @Parameter(description = "Filter by the sex of the person in the referral")
    @RequestParam(name = "sex", required = false) sex: String?,
    @Parameter(description = "Filter by the cohort of the referral Eg: 'Sexual Offence' or 'General Offence - LDC")
    @RequestParam(name = "cohort", required = false) cohort: String?,
    @Parameter(description = "Search by the name or the CRN of the offender in the referral")
    @RequestParam(name = "nameOrCRN", required = false) nameOrCRN: String?,
    @Parameter(description = "Filter by the human readable pdu of the referral, i.e. 'All London'")
    @RequestParam(name = "pdu", required = false) pdu: String?,
    @Parameter(description = "Filter by one or more reporting teams. Repeat the parameter to include multiple teams.")
    @RequestParam(name = "reportingTeam", required = false) reportingTeams: List<String>?,
  ): ResponseEntity<ProgrammeGroupDetails> {
    val username = authenticationHolder.username

    if (username == null || username.isBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    val groupCohort = if (cohort.isNullOrEmpty()) null else ProgrammeGroupCohort.fromString(cohort)

    val programmeDetails = programmeGroupService.getGroupWaitlistDataByCriteria(
      pageable,
      selectedTab,
      groupId,
      sex,
      groupCohort,
      nameOrCRN,
      pdu,
      reportingTeams,
    )

    return ResponseEntity.ok(programmeDetails)
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Allocate a referral to a programme group",
    operationId = "allocateToProgrammeGroup",
    description = "Allocate a referral to a specific programme group",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Referral successfully allocated to the programme group",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request format or invalid UUID format",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to allocate to this group.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The group or referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping("/group/{groupId}/allocate/{referralId}")
  fun allocateToProgrammeGroup(
    @Parameter(
      description = "The group_id (UUID) of the group to allocate to",
      required = true,
    )
    @PathVariable("groupId") groupId: UUID,
    @Parameter(
      description = "The referralId (UUID) of a referral",
      required = true,
    )
    @PathVariable("referralId") referralId: UUID,
    @Valid
    @RequestBody allocateToGroupRequest: AllocateToGroupRequest,
  ): ResponseEntity<AllocateToGroupResponse> {
    val referral = programmeGroupMembershipService.allocateReferralToGroup(
      referralId,
      groupId,
      authenticationHolder.username ?: "SYSTEM",
      allocateToGroupRequest.additionalDetails,
    )

    val response = AllocateToGroupResponse(
      message = "${referral.personName} was added to this group. Their referral status is now Scheduled.",
    )

    return ResponseEntity.status(HttpStatus.OK).body(response)
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Create a new programme group",
    operationId = "createProgrammeGroup",
    description = "Create a new programme group",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Programme group successfully created",
        content = [Content(schema = Schema(implementation = Void::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request body",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to create groups.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping(
    "/group",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun createProgrammeGroup(
    @Valid
    @RequestBody createGroupRequest: CreateGroupRequest,
  ): ResponseEntity<Void> {
    val username = authenticationHolder.username
    if (username == null || username.isBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    programmeGroupService.createGroup(createGroupRequest, username)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @Operation(
    tags = ["Programme Group controller"],
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
  @GetMapping("/bff/remove-from-group/{referralId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getReferralStatusForm(
    @Parameter(description = "The id (UUID) of a referral status description", required = true)
    @PathVariable referralId: UUID,
  ): ResponseEntity<ReferralStatusFormData> = referralStatusService.getReferralStatusFormDataFromReferralId(referralId)
    ?.let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("Referral status history for referral with id $referralId not found")
}
