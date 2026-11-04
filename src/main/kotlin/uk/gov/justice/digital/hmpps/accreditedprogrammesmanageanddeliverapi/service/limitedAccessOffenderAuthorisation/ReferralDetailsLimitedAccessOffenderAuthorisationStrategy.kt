package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.LimitedAccessOffenderAuthorisationStrategy.Companion.REFERRAL_ID_PATH_VARIABLE_NAME
import java.util.UUID

@Component
class ReferralDetailsLimitedAccessOffenderAuthorisationStrategy(
  private val referralService: ReferralService,
  private val userAccessService: UserAccessService,
) : LimitedAccessOffenderAuthorisationStrategy {
  private val log = LoggerFactory.getLogger(this::class.java)

  companion object {
    private const val REFERRAL_DETAILS_URI_PATTERN_REGEX =
      "^/referral-details/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?:/.*)?$"
    private const val REFERRAL_DETAILS_URI_PATTERN_ANT = "/referral-details/{referralId}/**"
  }

  override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean {
    val referralDetailsUriPattern = Regex(REFERRAL_DETAILS_URI_PATTERN_REGEX)

    return RequestMethod.GET.name == httpRequestMethod && referralDetailsUriPattern.matches(httpRequestPath)
  }

  override fun isAuthorised(httpRequestPath: String, username: String): Boolean {
    log.debug("START Checking referral details limited access offender authorisation for httpRequestPath: $httpRequestPath and username: $username")
    val referralId =
      getId(httpRequestPath, REFERRAL_ID_PATH_VARIABLE_NAME, REFERRAL_DETAILS_URI_PATTERN_ANT) ?: return true
    val access = getUserAccess(username, referralId)
    val authorisation = !(access?.isExcluded ?: false)
    log.debug("END Checking referral details limited access offender authorisation for httpRequestPath: $httpRequestPath and username: $username with authorisation: $authorisation")

    return authorisation
  }

  private fun getUserAccess(username: String, referralId: UUID): UserAccessService.Access? {
    val referralEntity = referralService.getReferralById(referralId)
    val caseReferenceNumber = referralEntity.crn
    val limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, listOf(caseReferenceNumber))

    return limitedAccessOffenderAccessMap[caseReferenceNumber]
  }
}
