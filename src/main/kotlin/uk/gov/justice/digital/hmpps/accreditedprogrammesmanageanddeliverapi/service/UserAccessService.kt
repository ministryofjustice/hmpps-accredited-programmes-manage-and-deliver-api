package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient

@Service
class UserAccessService(
  private val probationAccessControlApiClient: ProbationAccessControlApiClient,
  private val userService: UserService,
) {
  data class Access(
    val isLimitedAccessOffender: Boolean,
    val isExcluded: Boolean,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  fun determineUserAccess(username: String, crnList: List<String>): Map<String, Access> {
    val uniqueCRNs = crnList.distinct()
    if (uniqueCRNs.isEmpty()) return emptyMap()

    val limitedAccessOffenders = determineLimitedAccessOffendersByCRN(uniqueCRNs)
    val accessibleCRNs = determineAccessibleCRNs(username, uniqueCRNs)

    return uniqueCRNs
      .associateWith { crn ->
        Access(
          isLimitedAccessOffender = limitedAccessOffenders[crn] ?: false,
          isExcluded = crn !in accessibleCRNs,
        )
      }
  }

  private fun determineLimitedAccessOffendersByCRN(crnList: List<String>): Map<String, Boolean> = crnList.associateWith(::isLimitedAccessOffender)

  private fun determineAccessibleCRNs(username: String, crnList: List<String>): Set<String> = userService.getAccessibleOffenders(username, crnList)

  fun isLimitedAccessOffender(crn: String): Boolean = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body.excludedFrom.isNotEmpty() || response.body.restrictedTo.isNotEmpty()
    is ClientResult.Failure -> false
  }

  fun isUserExcluded(username: String, crn: String): Boolean = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body.excludedFrom.any { it.username == username }
    is ClientResult.Failure -> {
      log.error("Failed to retrieve case access for CRN $crn", response.toException())
      throw response.toException()
    }
  }
}
