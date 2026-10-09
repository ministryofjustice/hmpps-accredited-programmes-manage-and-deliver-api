package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListReferrals
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralCaseListItemService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
@RestController
@Tag(
  name = "Caselist",
  description = "The endpoint fetches the referrals details for the case list view",
)
class CaseListController(
  private val referralCaseListItemService: ReferralCaseListItemService,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  companion object {
    private const val REQUEST_PARAM_NAME_PROBATION_DELIVERY_UNIT = "pdu"
    private const val REQUEST_PARAM_NAME_REPORTING_TEAM = "reportingTeam"
    private const val REQUEST_PARAM_NAME_STATUS = "status"
    private const val REQUEST_PARAM_NAME_COHORT = "cohort"
    private const val REQUEST_PARAM_NAME_SEX = "sex"
  }

  @Operation(
    tags = ["Caselist"],
    summary = "Get all referrals for the case list view",
    operationId = "getCaseListReferrals",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paged list of all open/closed referrals for a PDU",
        content = [Content(schema = Schema(implementation = CaseListReferrals::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/pages/caselist/{openOrClosed}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getCaseListReferrals(
    @PageableDefault(page = 0, size = 50, sort = ["personSurname", "personForename"]) pageable: Pageable,
    @PathVariable(required = true) openOrClosed: OpenOrClosed,
    @Parameter(description = "CRN or persons name")
    @RequestParam(name = "crnOrPersonName", required = false) caseReferenceNumberOrPersonName: String?,
    @RequestParam requestParams: MultiValueMap<String, String>,
  ): CaseListReferrals {
    val username = authenticationHolder.username

    if (username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }

    // Read raw repeated query params so comma-containing values are treated as single values.
    // Decode each value for exact DB matching (e.g. "%2C" -> ",").
    val probationDeliveryUnits = requestParams[REQUEST_PARAM_NAME_PROBATION_DELIVERY_UNIT]
      ?.takeIf { it.isNotEmpty() }
      ?.map { URLDecoder.decode(it, UTF_8.name()) }

    val reportingTeamsDecoded = requestParams[REQUEST_PARAM_NAME_REPORTING_TEAM]
      ?.takeIf { it.isNotEmpty() }
      ?.map { URLDecoder.decode(it, UTF_8.name()) }

    val statusesDecoded = requestParams[REQUEST_PARAM_NAME_STATUS]
      ?.takeIf { it.isNotEmpty() }
      ?.map { URLDecoder.decode(it, UTF_8.name()) }

    val cohortsDecoded = requestParams[REQUEST_PARAM_NAME_COHORT]
      ?.takeIf { it.isNotEmpty() }
      ?.mapNotNull { ProgrammeGroupCohort.fromString(it) }

    val sexesDecoded = requestParams[REQUEST_PARAM_NAME_SEX]
      ?.takeIf { it.isNotEmpty() }
      ?.map { URLDecoder.decode(it, UTF_8.name()) }

    return referralCaseListItemService.getReferralCaseListItemServiceByCriteria(
      pageable = pageable,
      openOrClosed = openOrClosed,
      username = username,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      cohorts = cohortsDecoded,
      statuses = statusesDecoded,
      sexes = sexesDecoded,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeamsDecoded,
    )
  }
}

enum class OpenOrClosed {
  OPEN,
  CLOSED,
}
