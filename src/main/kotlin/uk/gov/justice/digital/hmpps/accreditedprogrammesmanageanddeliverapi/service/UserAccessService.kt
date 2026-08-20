package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient

@Service
class UserAccessService(
  private val probationAccessControlApiClient: ProbationAccessControlApiClient,
  private val userService: UserService,
  @Value("\${app.features.lao-access-check-enabled:false}")
  private val laoAccessCheckEnabled: Boolean,
  @Value("\${app.features.exclusion-access-check-enabled}")
  private val restrictionEnabled: Boolean,
) {
  data class Access(
    val lao: Boolean,
    val isExcluded: Boolean,
  )

  fun determineUserAccess(username: String, crnList: List<String>): Map<String, Access> {
    val uniqueCRNs = crnList.distinct()
    if (uniqueCRNs.isEmpty()) return emptyMap()

    val limitedAccessOffenders = determineLimitedAccessOffendersByCRN(uniqueCRNs)
    val accessibleCRNs = determineAccessibleCRNs(username, uniqueCRNs)

    return uniqueCRNs
      .associateWith { crn ->
        Access(
          lao = limitedAccessOffenders[crn] ?: false,
          isExcluded = crn !in accessibleCRNs,
        )
      }
      .let(::filterAccessByRestrictionFeature)
  }

  private fun determineLimitedAccessOffendersByCRN(crnList: List<String>): Map<String, Boolean> = if (laoAccessCheckEnabled) crnList.associateWith(::isLimitedAccessOffender) else emptyMap()

  private fun determineAccessibleCRNs(username: String, crnList: List<String>): Set<String> {
    val accessChecksEnabled = laoAccessCheckEnabled || restrictionEnabled

    return if (accessChecksEnabled) {
      userService.getAccessibleOffenders(username, crnList)
    } else {
      crnList.toSet()
    }
  }

  private fun filterAccessByRestrictionFeature(accessMap: Map<String, Access>): Map<String, Access> = if (restrictionEnabled) accessMap else accessMap.filterValues { !it.isExcluded }

  fun isLimitedAccessOffender(crn: String): Boolean = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body.excludedFrom.isNotEmpty() || response.body.restrictedTo.isNotEmpty()
    is ClientResult.Failure -> false
  }
}
