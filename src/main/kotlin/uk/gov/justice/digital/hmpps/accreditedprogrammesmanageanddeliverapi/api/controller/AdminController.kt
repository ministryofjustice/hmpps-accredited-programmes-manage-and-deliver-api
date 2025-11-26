package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.PopulatePersonalDetailsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AdminService
import java.util.UUID

data class PopulatePersonalDetailsResponse(val ids: List<String>)

/**
 * Controller for developer / admin operations - not for public (e.g. Web UI) use
 */
@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class AdminController(
  private val adminService: AdminService,
  private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Admin"],
    summary = "Populate personal details for referrals",
    operationId = "populatePersonalDetails",
    description = """For the specified Referrals ('*' represents a wildcard of all Referrals), re-fetch the 
      |Personal Details from nDelius.
      |
      |This is useful if new fields have been added, or new data is available from nDelius, and we wish to (re)fetch data
      |from nDelius as a way to update our data automatically.""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Update started (not completed, process is async)",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request format or invalid UUID format",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @PostMapping("/admin/populate-personal-details", consumes = [MediaType.APPLICATION_JSON_VALUE])
  suspend fun populatePersonalDetails(
    @Parameter(
      description = """IDs of the Referrals to process.  Use "*" in the list to process all referrals.""",
      required = true,
    )
    @RequestBody request: PopulatePersonalDetailsRequest,
  ): ResponseEntity<PopulatePersonalDetailsResponse> {
    log.info("Received request to populate personal details for referrals: {}", request.referralIds)

    val containsWildcard = request.referralIds.contains("*")

    val parsedUuids: List<UUID> = if (!containsWildcard) {
      request.referralIds.mapNotNull { str ->
        try {
          UUID.fromString(str)
        } catch (e: IllegalArgumentException) {
          null
        }
      }
    } else {
      emptyList()
    }

    backgroundScope.launch {
      try {
        if (containsWildcard) {
          log.info("Processing all referrals")
          adminService.refreshPersonalDetailsForAllReferrals()
        } else {
          log.info("Processing {} specific referrals", parsedUuids)
          adminService.refreshPersonalDetailsForReferrals(parsedUuids)
        }
      } catch (e: Exception) {
        log.error("Error during background processing of personal details", e)
      }
    }

    return ResponseEntity.ok(PopulatePersonalDetailsResponse(ids = request.referralIds))
  }

  @PostMapping("/admin/check-referral-info")
  suspend fun checkIfReferralHasInfo() {
    adminService.cleanUpReferralsWithNoDeliusOrOasysData()
  }
}
