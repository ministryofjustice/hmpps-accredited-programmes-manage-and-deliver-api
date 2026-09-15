package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import java.util.UUID

@Component
class SessionNoteLimitedAccessOffenderAuthorisationStrategy(
  private val referralRepository: ReferralRepository,
  private val userAccessService: UserAccessService,
) : LimitedAccessOffenderAuthorisationStrategy {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val pathMatcher = AntPathMatcher()

  companion object {
    private const val SESSION_NOTE_URI_PATTERN_REGEX =
      "^/bff/session/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/referral/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/session-notes/?$"
    private const val SESSION_NOTE_URI_PATTERN_ANT = "/bff/session/{sessionId}/referral/{referralId}/session-notes"
    private const val REFERRAL_ID_PATH_VARIABLE_NAME = "referralId"
  }

  override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean {
    val sessionNoteUriPattern = Regex(SESSION_NOTE_URI_PATTERN_REGEX)

    return RequestMethod.GET.name == httpRequestMethod && sessionNoteUriPattern.matches(httpRequestPath)
  }

  override fun isAuthorised(httpRequestPath: String, username: String): Boolean {
    val referralId = getReferralId(httpRequestPath) ?: return true
    val access = getUserAccess(username, referralId)

    return !(access?.isExcluded ?: false)
  }

  private fun getUserAccess(username: String, referralId: UUID): UserAccessService.Access? {
    val referralEntity = referralRepository.findByIdOrNull(referralId) ?: return null
    val caseReferenceNumber = referralEntity.crn
    val limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, listOf(caseReferenceNumber))

    return limitedAccessOffenderAccessMap[caseReferenceNumber]
  }

  private fun getReferralId(httpRequestPath: String): UUID? {
    var referralId: UUID? = null
    try {
      val variables = pathMatcher.extractUriTemplateVariables(SESSION_NOTE_URI_PATTERN_ANT, httpRequestPath)
      val uuidStr = variables[REFERRAL_ID_PATH_VARIABLE_NAME]
      referralId = UUID.fromString(uuidStr)
    } catch (ex: IllegalArgumentException) {
      log.error("Failed to parse referralId from path: $httpRequestPath", ex)
    }

    return referralId
  }
}
