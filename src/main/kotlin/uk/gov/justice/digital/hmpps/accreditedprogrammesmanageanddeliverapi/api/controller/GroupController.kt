package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.AllocatedOrWaitlist
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ProgrammeGroupService
import java.util.UUID

@Tag(
  name = "Programme Group controller",
  description = "A series of endpoints to retrieve and manage group details and allocations",
)
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class GroupController(
  private val programmeGroupService: ProgrammeGroupService,
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
  @GetMapping("/bff/group/{groupId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getGroupDetails(
    @Parameter(description = "The id (UUID) of a group", required = true)
    @PathVariable("groupId") groupId: UUID,
    @Parameter(description = "View referrals allocated to a group or are in waitlist to a group")
    @RequestParam(name = "allocationAndWaitlistTab", required = true) allocationAndWaitlistTab: AllocatedOrWaitlist,
    @Parameter(description = "Filter by the sex of the person in the referral")
    @RequestParam(name = "sex", required = false) sex: String?,
    @Parameter(description = "Filter by the cohort of the referral Eg: SEXUAL_OFFENCE or GENERAL_OFFENCE")
    @RequestParam(name = "cohort", required = false) cohort: OffenceCohort?,
    @Parameter(description = "Search by the name or the CRN of the offender in the referral")
    @RequestParam(name = "nameOrCRN", required = false) nameOrCRN: String?,
    @Parameter(description = "Filter by the pdu of the referral")
    @RequestParam(name = "pdu", required = false) pdu: String?,
    @Parameter(description = "Page number (0-based)")
    @RequestParam(name = "page", defaultValue = "0") page: Int,
    @Parameter(description = "Page size")
    @RequestParam(name = "size", defaultValue = "20") size: Int,
  ): ResponseEntity<ProgrammeGroupDetails> {
    val groupEntity = programmeGroupService.getGroupById(groupId)
    val pageable = PageRequest.of(page, size)

    val allocationAndWaitlistData = if (allocationAndWaitlistTab == AllocatedOrWaitlist.WAITLIST) {
      val pagedWaitlistData = programmeGroupService.getGroupWaitlistData(
        groupId = groupId,
        sex = sex,
        cohort = cohort,
        nameOrCRN = nameOrCRN,
        pdu = pdu,
        pageable = pageable,
      )

      ProgrammeGroupDetails.AllocationAndWaitlistData(
        counts = ProgrammeGroupDetails.Counts(waitlist = pagedWaitlistData.totalElements.toInt()),
        filters = programmeGroupService.getGroupFilters(),
        pagination = ProgrammeGroupDetails.Pagination(page = pagedWaitlistData.number, size = pagedWaitlistData.size),
        paginatedWaitlistData = pagedWaitlistData.content,
      )
    } else {
      throw NotImplementedError("Allocated view not yet implemented")
    }

    return ResponseEntity.ok(
      ProgrammeGroupDetails(
        group = ProgrammeGroupDetails.Group(
          code = groupEntity.code,
          regionName = "TODO: Region mapping to be implemented",
        ),
        allocationAndWaitlistData = allocationAndWaitlistData,
      ),
    )
  }
}
