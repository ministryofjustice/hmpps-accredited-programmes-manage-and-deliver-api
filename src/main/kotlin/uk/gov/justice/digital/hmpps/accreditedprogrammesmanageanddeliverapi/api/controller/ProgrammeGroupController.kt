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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AllocateToGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupsByRegion
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.RegionService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ScheduleService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserService
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
  private val userService: UserService,
  private val regionService: RegionService,
  private val scheduleService: ScheduleService,
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
    // This logic is non-trivial, please see [this doc](docs/2025-11-group-details-page-tabs.md)
    // for an explanation of the flow of data and expected behaviour.
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
    summary = "Get programme groups by region",
    operationId = "getProgrammeGroupsByRegion",
    description = "Retrieve a paged list of programme groups for a specified region",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Programme groups retrieved successfully",
        content = [Content(schema = Schema(implementation = GroupsByRegion::class))],
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
      ApiResponse(
        responseCode = "404",
        description = "No groups found for the specified region",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/groups/{selectedTab}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getProgrammeGroupsByRegion(
    @PageableDefault(
      page = 0,
      size = 50,
      sort = ["startedAtDate"],
      direction = Sort.Direction.ASC,
    ) pageable: Pageable,
    @Parameter(description = "Return table data for either the Not started tab or the In progress/Completed tab")
    @PathVariable("selectedTab") selectedTab: GroupPageByRegionTab,
    @Parameter(description = "Filter by the unique group code")
    @RequestParam(name = "groupCode", required = false) groupCode: String?,
    @Parameter(description = "Filter by the human readable pdu of the group, i.e. 'All London'")
    @RequestParam(name = "pdu", required = false) pdu: String?,
    @Parameter(description = "Filter by the delivery location name")
    @RequestParam(name = "deliveryLocations", required = false) deliveryLocations: List<String>?,
    @Parameter(description = "Filter by the cohort of the group Eg: 'Sexual Offence' or 'General Offence - LDC")
    @RequestParam(name = "cohort", required = false) cohort: String?,
    @Parameter(description = "Filter by the sex that the group is being run for: 'Male', 'Female' or 'Mixed'")
    @RequestParam(name = "sex", required = false) sex: String?,
  ): ResponseEntity<GroupsByRegion> {
    val username = getUsername()

    val groups = programmeGroupService.getProgrammeGroupsForRegion(
      pageable = pageable,
      groupCode = groupCode,
      pdu = pdu,
      deliveryLocations = deliveryLocations,
      cohort = cohort,
      sex = sex,
      selectedTab = selectedTab,
      username,
    )

    return ResponseEntity.ok(groups)
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
    summary = "Remove a referral from a programme group",
    operationId = "removeFromProgrammeGroup",
    description = "Remove a referral from a specific programme group and update its status",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Referral successfully removed from the programme group",
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
        description = "Forbidden. The client is not authorised to remove from this group.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The group, referral, or status description does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping("/group/{groupId}/remove/{referralId}")
  fun removeFromProgrammeGroup(
    @Parameter(
      description = "The group_id (UUID) of the group to remove from",
      required = true,
    )
    @PathVariable("groupId") groupId: UUID,
    @Parameter(
      description = "The referralId (UUID) of a referral",
      required = true,
    )
    @PathVariable("referralId") referralId: UUID,
    @Valid
    @RequestBody removeFromGroupRequest: RemoveFromGroupRequest,
  ): ResponseEntity<RemoveFromGroupResponse> {
    val referral = programmeGroupMembershipService.removeReferralFromGroup(
      referralId,
      groupId,
      authenticationHolder.username ?: "SYSTEM",
      removeFromGroupRequest.additionalDetails,
      removeFromGroupRequest.referralStatusDescriptionId,
    )

    val response = RemoveFromGroupResponse(
      message = "${referral.personName} was removed from this group. Their referral status is now ${referral.statusHistories.maxByOrNull { it.createdAt }?.referralStatusDescription?.description}.",
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
    val username = getUsername()
    val group = programmeGroupService.createGroup(createGroupRequest, username)
    scheduleService.scheduleSessionsForGroup(group.id!!)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Get group by GroupCode",
    operationId = "getGroupInUserRegion",
    description = "Get group by GroupCode and in User region",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns programme group if exists",
        content = [Content(schema = Schema(implementation = Group::class))],
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
        description = "Forbidden. The client is not authorised to retrieve group details.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/group/{groupCode}/details")
  fun getGroupInRegion(@PathVariable("groupCode") groupCode: String): ResponseEntity<Group>? {
    val username = getUsername()
    return ResponseEntity.ok(programmeGroupService.getGroupInRegion(groupCode, username)?.toApi())
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "BFF endpoint to get a list of PDUs for the user region.",
    operationId = "getPdusInRegion",
    description = "BFF endpoint to retrieve a list of PDUs for the region the logged in user is in.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns a list of PDUs",
        content = [Content(array = ArraySchema(schema = Schema(implementation = CodeDescription::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to retrieve pdus.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/pdus-for-user-region")
  fun getPdusInUserRegion(): ResponseEntity<List<CodeDescription>> {
    val username = getUsername()
    val (userRegion) = userService.getUserRegions(username)
    val pdusForRegion =
      regionService.getPdusForRegion(userRegion.code).map { CodeDescription(it.code, it.description) }
    return ResponseEntity.ok(pdusForRegion)
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "BFF endpoint to get a list of office locations for the selected PDU.",
    operationId = "getOfficeLocationsInPdu",
    description = "BFF endpoint to retrieve a list of office locations for the selected PDU.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns a list of office locations",
        content = [Content(array = ArraySchema(schema = Schema(implementation = CodeDescription::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to retrieve office locations.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/office-locations-for-pdu/{pduCode}")
  fun getOfficeLocationsInPdu(@PathVariable pduCode: String): ResponseEntity<List<CodeDescription>> {
    val officeLocationsForPdu =
      regionService.getOfficeLocationsForPdu(pduCode).map { CodeDescription(it.code, it.description) }
    return ResponseEntity.ok(officeLocationsForPdu)
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "BFF endpoint to get a list of members for the logged user's region Region.",
    operationId = "getMembersInRegion",
    description = "BFF endpoint to retrieve a list of team members for the logged in user's Region.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns a list of members",
        content = [Content(array = ArraySchema(schema = Schema(implementation = UserTeamMember::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to retrieve members for region.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/region/members")
  fun getMembersInUserRegion(): ResponseEntity<List<UserTeamMember>> {
    val username = getUsername()
    val (userRegion) = userService.getUserRegions(username)
    val teamMembers = regionService.getTeamMembersForPdu(userRegion.code)
    return ResponseEntity.ok(teamMembers)
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Schedule a one-to-one session for group members",
    operationId = "scheduleSession",
    description = "Schedule a one-to-one session for members of a programme group. This endpoint allows facilitators to schedule individual sessions that must be coordinated across multiple group members.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Session successfully scheduled",
        content = [Content(schema = Schema(implementation = ScheduleSessionResponse::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Group or session template not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping("/group/{groupId}/session/schedule")
  fun scheduleSession(
    @Parameter(
      description = "The UUID of the programme group",
      required = true,
    )
    @PathVariable("groupId") groupId: UUID,
    @Valid
    @RequestBody scheduleSessionRequest: ScheduleSessionRequest,
  ): ResponseEntity<ScheduleSessionResponse> {
    // Placeholder implementation - actual business logic to be implemented
    val response = ScheduleSessionResponse(
      message = "Session scheduled successfully",
    )

    return ResponseEntity.status(HttpStatus.OK).body(response)
  }

  private fun getUsername(): String {
    val username = authenticationHolder.username
    if (username == null || username.isBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    return username
  }
}
