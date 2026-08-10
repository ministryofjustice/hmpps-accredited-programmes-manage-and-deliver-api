package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.manageUsersApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.manageUsersApi.model.UserDto

private const val MANAGE_USERS_API = "Manage Users API"

@Component
class ManageUsersApiClient(
  @Qualifier("manageUsersWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getUserDetails(username: String) = getRequest<UserDto>(MANAGE_USERS_API) {
    path = "/users/$username"
  }
}
