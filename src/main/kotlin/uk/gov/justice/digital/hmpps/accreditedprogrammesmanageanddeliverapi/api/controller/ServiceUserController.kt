package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ServiceUser
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ServiceUserService

@RestController
@Tag(name = "ServiceUser")
class ServiceUserController(
  private val serviceUserService: ServiceUserService,
) {

  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR', 'PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
  @GetMapping(
    "/service-user/{identifier}",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    name = "Get Service User for the CRN or the Prison Number",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "OK"),
      ApiResponse(responseCode = "400", description = "Bad Request"),
    ],
  )
  fun getServiceUserByCrnOrPrisonerNumber(
    @PathVariable(name = "identifier")
    @Pattern(
      regexp = "^([A-Z]\\d{6}|[A-Z]\\d{4}[A-Z]{2})$",
      message = "Invalid code format. Expected format for CRN: X718255 or PrisonNumber: A1234AA",
    )
    identifier: String,
    authentication: JwtAuthenticationToken,
  ): ServiceUser {
    val userName = getUserNameFromAuthentication(authentication)
    if (!serviceUserService.hasAccessToLimitedAccessOffender(userName, identifier)) {
      throw AccessDeniedException(
        "You are not authorized to view this person's details. Either contact your administrator or enter a different CRN or Prison Number",
      )
    }
    return serviceUserService.getServiceUserByIdentifier(identifier)
  }

  private fun getUserNameFromAuthentication(authentication: JwtAuthenticationToken): String = authentication.token.getClaimAsString("user_name") ?: "Unknown User"
}
