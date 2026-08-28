package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import java.util.UUID

@Component
class ReferralDetailsLimitedAccessOffenderAuthorisationStrategy(
  private val referralService: ReferralService,
  private val userAccessService: UserAccessService,
) : LimitedAccessOffenderAuthorisationStrategy {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val pathMatcher = AntPathMatcher()

  companion object {
    private const val REFERRAL_DETAILS_URI_PATTERN_REGEX =
      "^/referral-details/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?:/.*)?$"
    private const val REFERRAL_DETAILS_URI_PATTERN_ANT = "/referral-details/{referralId}/**"
    private const val REFERRAL_ID_PATH_VARIABLE_NAME = "referralId"
  }

  override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean {
    val referralDetailsUriPattern = Regex(REFERRAL_DETAILS_URI_PATTERN_REGEX)

    return RequestMethod.GET.name == httpRequestMethod && referralDetailsUriPattern.matches(httpRequestPath)
  }

  override fun isAuthorised(httpRequestPath: String, username: String): Boolean {
    val referralId = getReferralId(httpRequestPath) ?: return false
    val access = getUserAccess(username, referralId)

    return !(access?.isExcluded ?: false)
  }

  private fun getUserAccess(username: String, referralId: UUID): UserAccessService.Access? {
    val referralEntity = referralService.getReferralById(referralId)
    val caseReferenceNumber = referralEntity.crn
    val limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, listOf(caseReferenceNumber))

    return limitedAccessOffenderAccessMap[caseReferenceNumber]
  }

  private fun getReferralId(httpRequestPath: String): UUID? {
    var referralId: UUID? = null
    try {
      val variables = pathMatcher.extractUriTemplateVariables(REFERRAL_DETAILS_URI_PATTERN_ANT, httpRequestPath)
      val uuidStr = variables[REFERRAL_ID_PATH_VARIABLE_NAME]
      referralId = UUID.fromString(uuidStr)
    } catch (ex: IllegalArgumentException) {
      log.error("Failed to parse referralId from path: $httpRequestPath", ex)
    }

    return referralId
  }
}
