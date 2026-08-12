package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.manageUsersApi.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model.AllCaseAccess
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.Constants.UNKNOWN_USER_USERNAME
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_LIMITED_ACCESS_OFFENDER_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_PERSONAL_DETAILS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_USER_MANAGE_USERS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_USER_TEAM_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.User
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.UserRegionOverrideRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class UserService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val telemetryService: TelemetryService,
  private val userRegionOverrideRepository: UserRegionOverrideRepository,
  private val manageUsersApiClient: ManageUsersApiClient,
  private val probationAccessControlApiClient: ProbationAccessControlApiClient,
) {

  val log = LoggerFactory.getLogger(this::class.java)

  fun getUserRegionNames(username: String): List<String> {
    val nDeliusRegionNames = this.getUserRegions(username)
      .map { it.description.trim() }
      .filter { it.isNotEmpty() }
      .distinct()

    val manualRegionNames = userRegionOverrideRepository.findActiveRegionNamesByUsername(username)
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinct()
      .sorted()

    val nDeliusRegionNameSet = nDeliusRegionNames.toSet()
    val mergedRegionNames = nDeliusRegionNames + manualRegionNames.filterNot { nDeliusRegionNameSet.contains(it) }

    if (mergedRegionNames.isNotEmpty()) {
      log.debug(
        "User $username has effective access to regions: ${mergedRegionNames.joinToString(", ")}",
      )
    }

    return mergedRegionNames
  }

  fun getPersonalDetailsByIdentifier(identifier: String): NDeliusPersonalDetails {
    val userName = authenticationHolder.username ?: UNKNOWN_USER_USERNAME
    if (!hasAccessToLimitedAccessOffender(userName, identifier)) {
      throw AccessDeniedException(
        "You are not authorized to view this person's details. Either contact your administrator or enter a different CRN or Prison Number",
      )
    }

    return getPersonalDetailsWithoutAuthentication(identifier)
  }

  fun getPersonalDetailsWithoutAuthentication(identifier: String): NDeliusPersonalDetails = when (val result = nDeliusIntegrationApiClient.getPersonalDetails(identifier)) {
    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_PERSONAL_DETAILS_N_DELIUS.eventName}.success",
        integrationActionType = GET_PERSONAL_DETAILS_N_DELIUS.name,
        outcome = "success",
      )

      result.body
    }

    is ClientResult.Failure -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_PERSONAL_DETAILS_N_DELIUS.eventName}.failure",
        integrationActionType = GET_PERSONAL_DETAILS_N_DELIUS.name,
        outcome = "failure",
      )

      result.throwException()
    }
  }

  fun hasAccessToLimitedAccessOffender(username: String, identifier: String): Boolean = getAccessibleOffenders(username, listOf(identifier)).contains(identifier)

  @Cacheable(value = ["user-details-or-null"], key = "#username", unless = "#result == null")
  fun getUserByUsernameOrNull(username: String): User? {
    try {
      return if (username.isNotBlank() && username != UNKNOWN_USER_USERNAME) getUserByUsername(username) else null
    } catch (exception: Exception) {
      log.error("Failed to get user by username: $username", exception)
    }

    return null
  }

  @Cacheable(value = ["user-details"], key = "#username")
  fun getUserByUsername(username: String): User = when (val result = manageUsersApiClient.getUserDetails(username)) {
    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_USER_MANAGE_USERS.eventName}.success",
        integrationActionType = GET_USER_MANAGE_USERS.name,
        outcome = "success",
      )

      val dto = result.body

      return User(
        username = dto.username,
        name = dto.name,
        active = dto.active,
      )
    }

    is ClientResult.Failure -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_USER_MANAGE_USERS.eventName}.failure",
        integrationActionType = GET_USER_MANAGE_USERS.name,
        outcome = "failure",
      )

      result.throwException()
    }
  }

  fun getAccessibleOffenders(username: String, identifiers: List<String>): Set<String> = when (val result = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, identifiers)) {
    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_LIMITED_ACCESS_OFFENDER_N_DELIUS.eventName}.success",
        integrationActionType = GET_LIMITED_ACCESS_OFFENDER_N_DELIUS.name,
        outcome = "success",
      )

      result.body.access
        .filter { !it.userExcluded && !it.userRestricted }
        .map { it.crn }
        .toSet()
    }

    is ClientResult.Failure -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_LIMITED_ACCESS_OFFENDER_N_DELIUS.eventName}.failure",
        integrationActionType = GET_LIMITED_ACCESS_OFFENDER_N_DELIUS.name,
        outcome = "failure",
      )

      result.throwException()
    }
  }

  /**
   * Fetches the list of region names that the given user has access to via their teams in nDelius.
   *
   * Note: @Cacheable only works when called from outside this class (via Spring proxy).
   * Calls from within this class (e.g. getFirstUserRegionDescription) bypass the cache.
   * This is acceptable — most call sites invoke getUserRegions() directly from other services.
   *
   * @param username The username to fetch regions for
   * @return List of region codes and names (descriptions) the user has access to
   */
  @Cacheable(value = ["user-regions"], key = "#username", unless = "#result.isEmpty()")
  fun getUserRegions(username: String): List<CodeDescription> = when (val result = nDeliusIntegrationApiClient.getTeamsForUser(username)) {
    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_USER_TEAM_N_DELIUS.eventName}.success",
        integrationActionType = GET_USER_TEAM_N_DELIUS.name,
        outcome = "success",
      )
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
      telemetryService.logToAppInsights(
        eventName = "${GET_USER_TEAM_N_DELIUS.eventName}.failure",
        integrationActionType = GET_USER_TEAM_N_DELIUS.name,
        outcome = "failure",
      )

      emptyList()
    }
  }

  fun getCaseAccessByCrn(crn: String): AllCaseAccess = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body
    is ClientResult.Failure -> {
      val exception = response.toException()
      log.error("Failed to retrieve LAO case access for CRN $crn: ${response.getErrorMessage()}", exception)
      throw response.toException()
    }
  }
}
