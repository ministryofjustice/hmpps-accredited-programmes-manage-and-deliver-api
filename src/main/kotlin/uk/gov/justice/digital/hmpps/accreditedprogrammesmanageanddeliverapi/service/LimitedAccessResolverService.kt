package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.ProbationAccessControlApiClient

@Service
class LimitedAccessResolverService(
  private val probationAccessControlApiClient: ProbationAccessControlApiClient,
  private val userService: UserService,
  @Value("\${app.features.lao-access-check-enabled:false}")
  private val laoAccessCheckEnabled: Boolean,
  @Value("\${app.features.lao-view-restriction-enabled}")
  private val restrictionEnabled: Boolean,
) {
  data class Access(
    val lao: Boolean,
    val isExcluded: Boolean,
  )

  fun resolve(username: String, crns: List<String>): Map<String, Access> {
    val distinctCrns = crns.distinct()
    if (distinctCrns.isEmpty()) return emptyMap()

    val laoByCrn = if (laoAccessCheckEnabled) distinctCrns.associateWith(::isLao) else emptyMap()
    val accessibleCrns = if (laoAccessCheckEnabled || restrictionEnabled) {
      userService.getAccessibleOffenders(username, distinctCrns)
    } else {
      distinctCrns.toSet()
    }

    return distinctCrns.associateWith { crn ->
      Access(
        lao = laoByCrn[crn] ?: false,
        isExcluded = crn !in accessibleCrns,
      )
    }
  }

  private fun isLao(crn: String): Boolean = when (val response = probationAccessControlApiClient.getCaseAccessByCrn(crn)) {
    is ClientResult.Success -> response.body.excludedFrom.isNotEmpty() || response.body.restrictedTo.isNotEmpty()
    is ClientResult.Failure -> false
  }
}
