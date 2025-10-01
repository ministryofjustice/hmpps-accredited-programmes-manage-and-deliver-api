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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralCaseListItemService
import java.net.URLDecoder

@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
@RestController
@Tag(
  name = "Caselist",
  description = "The endpoint fetches the referrals details for the case list view",
)
class CaseListController(private val referralCaseListItemService: ReferralCaseListItemService) {
  @Operation(
    tags = ["Caselist"],
    summary = "Get all referrals for the case list view",
    operationId = "getCaseListReferrals",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paged list of all open/closed referrals for a PDU",
        content = [Content(schema = Schema(implementation = ReferralCaseListItem::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/pages/caselist/{openOrClosed}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getCaseListReferrals(
    @PageableDefault(page = 0, size = 10, sort = ["personName"]) pageable: Pageable,
    @PathVariable(
      name = "openOrClosed",
      required = true,
    ) openOrClosed: OpenOrClosed,
    @Parameter(description = "CRN or persons name")
    @RequestParam(name = "crnOrPersonName", required = false) crnOrPersonName: String?,
    @Parameter(description = "Filter by the cohort of the referral Eg: SEXUAL_OFFENCE or GENERAL_OFFENCE") @RequestParam(
      value = "cohort",
      required = false,
    ) cohort: OffenceCohort?,
    @Parameter(description = "Filter by the status of the referral") @RequestParam(
      value = "status",
      required = false,
    ) status: String?,
  ): Page<ReferralCaseListItem> = referralCaseListItemService.getReferralCaseListItemServiceByCriteria(
    pageable = pageable,
    openOrClosed = openOrClosed,
    crnOrPersonName = crnOrPersonName,
    cohort = cohort?.name,
    status = if (status.isNullOrEmpty()) null else URLDecoder.decode(status, "UTF-8"),
  )

  @Operation(
    tags = ["Caselist"],
    summary = "Get reference data for displaying the possible filters for the ui",
    operationId = "getCaseListFilterData",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The filter reference data to display in the UI",
        content = [Content(schema = Schema(implementation = CaseListFilterValues::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/bff/caselist/filters", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getCaseListFilterData(): ResponseEntity<CaseListFilterValues> = ResponseEntity.ok().body(referralCaseListItemService.getCaseListFilterData())
}

enum class OpenOrClosed {
  OPEN,
  CLOSED,
}
