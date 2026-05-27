package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AdminService
import java.util.UUID

data class FetchPersonalDetailsRequest(
  @get:JsonProperty("referralIds", required = true)
  @Schema(
    example = "[\"981421e1-0242-4cde-92a2-44c737077f86\", \"af2e88f7-8a89-4a01-b52a-5d7e6805f605\"]",
    description = """List of referral IDs to populate personal details for.""",
    required = true,
  )
  val referralIds: List<String>,
)

data class FetchPersonalDetailsResponse(
  val successIds: List<String>,
  val notFoundIds: List<String>,
  val failureIds: List<String>,
)

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class OnboardingController(
  private val adminService: AdminService,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Onboarding"],
    summary = "Fetches the personal details and OASys details for specified Referrals",
  )
  @PostMapping("/onboarding/referrals", consumes = [MediaType.APPLICATION_JSON_VALUE])
  suspend fun fetchPersonalDetailsForReferrals(
    @Parameter(
      description = """IDs of the Referrals to fetch personal details for.""",
      required = true,
    )
    @RequestBody request: FetchPersonalDetailsRequest,
  ): ResponseEntity<FetchPersonalDetailsResponse> {
    val parsedUuids = request.referralIds.map { UUID.fromString(it) }

    try {
      log.info("Processing {} specific referrals", parsedUuids)
      val result = adminService.refreshPersonalDetailsForReferrals(parsedUuids)
      return ResponseEntity.ok(
        FetchPersonalDetailsResponse(
          successIds = result.successIds.map(UUID::toString),
          notFoundIds = result.notFoundIds.map(UUID::toString),
          failureIds = result.failureIds.map(UUID::toString),
        ),
      )
    } catch (e: Exception) {
      log.error("Error during background processing of personal details", e)
      return ResponseEntity.ok(
        FetchPersonalDetailsResponse(
          successIds = emptyList(),
          notFoundIds = emptyList(),
          failureIds = parsedUuids.map(UUID::toString),
        ),
      )
    }
  }
}
