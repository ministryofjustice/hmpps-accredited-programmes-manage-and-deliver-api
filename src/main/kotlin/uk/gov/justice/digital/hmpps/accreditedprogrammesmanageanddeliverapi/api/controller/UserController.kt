package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.AuthenticationUtils

@RestController
@PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
class UserController(
  private val userService: UserService,
  private val authenticationUtils: AuthenticationUtils,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["user"],
    summary = "Retrieve the current user's region information",
    operationId = "getCurrentUserRegion",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Region information for the current user",
        content = [Content(schema = Schema(implementation = CodeDescription::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this data.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The user does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @GetMapping("/current-user/region", produces = [MediaType.APPLICATION_JSON_VALUE])
  suspend fun getCurrentUserRegion(): ResponseEntity<CodeDescription> {
    val username = authenticationUtils.getUsername()
    val (userRegion) = userService.getUserRegions(username)
    return ResponseEntity.ok(userRegion)
  }
}
