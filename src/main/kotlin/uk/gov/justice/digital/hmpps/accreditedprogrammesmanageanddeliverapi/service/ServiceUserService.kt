package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.PersonalDetails
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class ServiceUserService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  fun getPersonalDetailsByIdentifier(identifier: String): PersonalDetails {
    val userName = authenticationHolder.username ?: "UNKNOWN_USER"
    if (!hasAccessToLimitedAccessOffender(userName, identifier)) {
      throw AccessDeniedException(
        "You are not authorized to view this person's details. Either contact your administrator or enter a different CRN or Prison Number",
      )
    }

    return when (val result = nDeliusIntegrationApiClient.getPersonalDetails(identifier)) {
      is ClientResult.Success -> result.body
      is ClientResult.Failure -> result.throwException()
    }
  }

  fun hasAccessToLimitedAccessOffender(username: String, identifier: String): Boolean = when (val result = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(identifier))) {
    is ClientResult.Success -> {
      val response = result.body
      val accessCheck = response.access.firstOrNull { it.crn == identifier }
      accessCheck?.let { !it.userExcluded && !it.userRestricted } ?: false
    }

    is ClientResult.Failure -> result.throwException()
  }
}
