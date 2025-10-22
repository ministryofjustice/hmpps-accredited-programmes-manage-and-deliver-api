package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.GroupWaitlistItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toAllocatedItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType
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
class GroupController(
  private val programmeGroupService: ProgrammeGroupService,
  private val userService: UserService,
  private val authenticationHolder: HmppsAuthenticationHolder,
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
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/group/{groupId}/{selectedTab}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getGroupDetails(
    @PageableDefault(page = 0, size = 10, sort = ["personName"]) pageable: Pageable,
    @Parameter(description = "The id (UUID) of a group", required = true)
    @PathVariable("groupId") groupId: UUID,
    @Parameter(description = "Return table data for either the allocated tab or the waitlist tab")
    @PathVariable("selectedTab") selectedTab: GroupPageTab,
    @Parameter(description = "Filter by the sex of the person in the referral")
    @RequestParam(name = "sex", required = false) sex: String?,
    @Parameter(description = "Filter by the cohort of the referral Eg: SEXUAL_OFFENCE or GENERAL_OFFENCE")
    @RequestParam(name = "cohort", required = false) cohort: OffenceCohort?,
    @Parameter(description = "Search by the name or the CRN of the offender in the referral")
    @RequestParam(name = "nameOrCRN", required = false) nameOrCRN: String?,
    @Parameter(description = "Filter by the human readable pdu of the referral, i.e. 'All London'")
    @RequestParam(name = "pdu", required = false) pdu: String?,
  ): ResponseEntity<ProgrammeGroupDetails> {
    val groupEntity = programmeGroupService.getGroupById(groupId)
    val pagedWaitlistData = programmeGroupService.getGroupWaitlistData(
      groupId = groupId,
      sex = sex,
      cohort = cohort,
      nameOrCRN = nameOrCRN,
      pdu = pdu,
      pageable = pageable,
    )

    val allocationAndWaitlistData = buildAllocationAndWaitlistData(selectedTab, pagedWaitlistData)
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

  private fun buildAllocationAndWaitlistData(
    selectedTab: GroupPageTab,
    pagedWaitlistData: Page<GroupWaitlistItem>,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData {
    val waitlistItems = pagedWaitlistData.content.filter { isAwaitingAllocation(it) }
    val allocatedItems = pagedWaitlistData.content.filter { !isAwaitingAllocation(it) }

    return when (selectedTab) {
      GroupPageTab.WAITLIST -> createWaitlistTabData(waitlistItems, allocatedItems, pagedWaitlistData)
      GroupPageTab.ALLOCATED -> createAllocatedTabData(waitlistItems, allocatedItems, pagedWaitlistData)
    }
  }

  private fun createWaitlistTabData(
    waitlistItems: List<GroupWaitlistItem>,
    allocatedItems: List<GroupWaitlistItem>,
    pagedData: Page<GroupWaitlistItem>,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData = ProgrammeGroupDetails.AllocationAndWaitlistData(
    counts = ProgrammeGroupDetails.Counts(
      waitlist = waitlistItems.size,
      allocated = allocatedItems.size,
    ),
    filters = programmeGroupService.getGroupFilters(),
    pagination = ProgrammeGroupDetails.Pagination(page = pagedData.number, size = pagedData.size),
    paginatedWaitlistData = waitlistItems,
  )

  private fun createAllocatedTabData(
    waitlistItems: List<GroupWaitlistItem>,
    allocatedItems: List<GroupWaitlistItem>,
    pagedData: Page<GroupWaitlistItem>,
  ): ProgrammeGroupDetails.AllocationAndWaitlistData = ProgrammeGroupDetails.AllocationAndWaitlistData(
    counts = ProgrammeGroupDetails.Counts(
      waitlist = waitlistItems.size,
      allocated = allocatedItems.size,
    ),
    filters = programmeGroupService.getGroupFilters(),
    pagination = ProgrammeGroupDetails.Pagination(page = pagedData.number, size = pagedData.size),
    paginatedAllocationData = allocatedItems.map(GroupWaitlistItem::toAllocatedItem),
    paginatedWaitlistData = waitlistItems,
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
