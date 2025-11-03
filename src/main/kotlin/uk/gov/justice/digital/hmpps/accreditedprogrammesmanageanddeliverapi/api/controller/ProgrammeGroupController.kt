package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.GroupWaitlistItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toAllocatedItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupMembershipService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
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
  private val userService: UserService,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val programmeGroupMembershipService: ProgrammeGroupMembershipService,
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
    @PageableDefault(page = 0, size = 10, sort = ["sentenceEndDate"], direction = Sort.Direction.ASC) pageable: Pageable,
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
  ): ResponseEntity<ProgrammeGroupDetails> {
    val groupEntity = programmeGroupService.getGroupById(groupId)
    val groupCohort = if (cohort.isNullOrEmpty()) null else ProgrammeGroupCohort.fromString(cohort)
    val pagedWaitlistData = programmeGroupService.getGroupWaitlistData(
      selectedTab,
      groupId,
      sex,
      cohort = groupCohort,
      nameOrCRN,
      pdu,
      pageable,
    )

    val otherTabCount = programmeGroupService.getGroupWaitlistCount(
      selectedTab = if (selectedTab == GroupPageTab.ALLOCATED) GroupPageTab.WAITLIST else GroupPageTab.ALLOCATED,
      groupId,
      sex,
      cohort = groupCohort,
      nameOrCRN,
      pdu,
    )

    val allocationAndWaitlistData = buildAllocationAndWaitlistData(selectedTab, pagedWaitlistData, otherTabCount)
    val userRegion = getUserRegion()

    return ResponseEntity.ok(
      ProgrammeGroupDetails(
        group = ProgrammeGroupDetails.Group(
          code = groupEntity.code,
          regionName = userRegion,
        ),
        allocationAndWaitlistData = allocationAndWaitlistData,
      ),
    )
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
  @PostMapping(
    "/group/{groupId}/allocate/{referralId}",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
  )
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
  ): ResponseEntity<Void> {
    programmeGroupMembershipService.allocateReferralToGroup(referralId, groupId)
    return ResponseEntity.status(HttpStatus.OK).build()
  }

  @Operation(
    tags = ["Programme Group controller"],
    summary = "Create a new programme group",
    operationId = "createProgrammeGroup",
    description = "Create a new programme group with the specified group code",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Programme group successfully created",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request format or group code",
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
    @RequestBody createGroup: CreateGroup,
  ): ResponseEntity<ProgrammeGroupEntity> {
    val programmeGroup = programmeGroupService.createGroup(createGroup)
    return ResponseEntity.status(HttpStatus.CREATED).body(programmeGroup)
  }

  private fun buildAllocationAndWaitlistData(
    selectedTab: GroupPageTab,
    pagedWaitlistData: Page<GroupWaitlistItem>,
    otherTabCount: Int,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData {
    val (waitlistItems, allocatedItems) = pagedWaitlistData.content.partition { isAwaitingAllocation(it) }

    return when (selectedTab) {
      GroupPageTab.WAITLIST -> createWaitlistTabData(waitlistItems, pagedWaitlistData, otherTabCount)
      GroupPageTab.ALLOCATED -> createAllocatedTabData(allocatedItems, pagedWaitlistData, otherTabCount)
    }
  }

  private fun createWaitlistTabData(
    waitlistItems: List<GroupWaitlistItem>,
    pagedData: Page<GroupWaitlistItem>,
    otherTabCount: Int,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData = ProgrammeGroupDetails.AllocationAndWaitlistData(
    counts = ProgrammeGroupDetails.Counts(
      waitlist = waitlistItems.size,
      allocated = otherTabCount,
    ),
    filters = programmeGroupService.getGroupFilters(),
    pagination = ProgrammeGroupDetails.Pagination(page = pagedData.number, size = pagedData.size),
    paginatedWaitlistData = waitlistItems,
  )

  private fun createAllocatedTabData(
    allocatedItems: List<GroupWaitlistItem>,
    pagedData: Page<GroupWaitlistItem>,
    otherTabCount: Int,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData = ProgrammeGroupDetails.AllocationAndWaitlistData(
    counts = ProgrammeGroupDetails.Counts(
      waitlist = otherTabCount,
      allocated = allocatedItems.size,
    ),
    filters = programmeGroupService.getGroupFilters(),
    pagination = ProgrammeGroupDetails.Pagination(page = pagedData.number, size = pagedData.size),
    paginatedAllocationData = allocatedItems.map(GroupWaitlistItem::toAllocatedItem),
  )

  private fun isAwaitingAllocation(item: GroupWaitlistItem): Boolean = item.status == ReferralStatusType.AWAITING_ALLOCATION.description

  private fun getUserRegion(): String {
    val username = authenticationHolder.username
    if (username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    val userRegions = userService.getUserRegions(username)
    return if (userRegions.isNotEmpty()) userRegions.first() else "Region not found"
  }
}
