package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class UserService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {

  val log = LoggerFactory.getLogger(this::class.java)

  fun getPersonalDetailsByIdentifier(identifier: String): NDeliusPersonalDetails {
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

  fun hasAccessToLimitedAccessOffender(username: String, identifier: String): Boolean = getAccessibleOffenders(username, listOf(identifier)).contains(identifier)

  fun getAccessibleOffenders(username: String, identifiers: List<String>): Set<String> = when (val result = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, identifiers)) {
    is ClientResult.Success -> {
      result.body.access
        .filter { !it.userExcluded && !it.userRestricted }
        .map { it.crn }
        .toSet()
    }

    is ClientResult.Failure -> result.throwException()
  }

  /**
   * Fetches the list of region names that the given user has access to via their teams in nDelius.
   *
   * @param username The username to fetch regions for
   * @return List of region codes and names (descriptions) the user has access to
   */
  fun getUserRegions(username: String): List<CodeDescription> = when (val result = nDeliusIntegrationApiClient.getTeamsForUser(username)) {
    is ClientResult.Success -> {
      val regionNames = result.body.teams.map { it.region }
      if (regionNames.isEmpty()) {
        log.warn("User $username has teams but no regions associated with them")
        emptyList()
      } else {
        log.debug(
          "User $username has access to regions: ${
            regionNames.distinct().joinToString(", ") { it.description }
          }",
        )
        regionNames
      }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch teams for user $username: ${result.toException().message}")
      emptyList()
    }
  }
}
