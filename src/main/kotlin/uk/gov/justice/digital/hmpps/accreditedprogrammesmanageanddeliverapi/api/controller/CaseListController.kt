package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralCaseListItemService

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
    operationId = "getOpenCaseListReferrals",
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
  fun getOpenCaseListReferrals(
    @PageableDefault(page = 0, size = 10) pageable: Pageable,
    @PathVariable(
      name = "openOrClosed",
      required = true,
    ) openOrClosed: OpenOrClosed,
  ): Page<ReferralCaseListItem> = referralCaseListItemService.getReferralCaseListItemServiceByCriteria(pageable, openOrClosed)
}

enum class OpenOrClosed {
  OPEN,
  CLOSED,
}
