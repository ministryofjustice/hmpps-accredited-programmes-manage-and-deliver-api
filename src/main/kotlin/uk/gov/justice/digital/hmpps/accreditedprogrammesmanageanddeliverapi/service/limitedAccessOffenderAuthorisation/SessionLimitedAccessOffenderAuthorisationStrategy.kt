package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class SessionLimitedAccessOffenderAuthorisationStrategy(
  private val userAccessService: UserAccessService,
  private val sessionRepository: SessionRepository,
) : LimitedAccessOffenderAuthorisationStrategy {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val pathMatcher = AntPathMatcher()

  companion object {
    private const val SESSION_URI_PATTERN_REGEX =
      "^/bff/session/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?:/.*)?$"
    private const val SESSION_URI_PATTERN_ANT = "/bff/session/{sessionId}/**"
    private const val SESSION_ID_PATH_VARIABLE_NAME = "sessionId"
  }

  override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean {
    val sessionUriPattern = Regex(SESSION_URI_PATTERN_REGEX)

    return RequestMethod.GET.name == httpRequestMethod && sessionUriPattern.matches(httpRequestPath)
  }

  override fun isAuthorised(httpRequestPath: String, username: String): Boolean {
    val sessionId = getSessionId(httpRequestPath) ?: return true
    val session = sessionRepository.findById(sessionId).getOrNull() ?: return true
    if (session.sessionType != SessionType.ONE_TO_ONE) return true
    val access = getUserAccess(username, session)

    return !(access?.isExcluded ?: false)
  }

  private fun getUserAccess(username: String, session: SessionEntity): UserAccessService.Access? {
    val caseReferenceNumber = session.attendees.firstOrNull()?.referral?.crn ?: return null
    val limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, listOf(caseReferenceNumber))

    return limitedAccessOffenderAccessMap[caseReferenceNumber]
  }

  private fun getSessionId(httpRequestPath: String): UUID? {
    var sessionId: UUID? = null
    try {
      val variables = pathMatcher.extractUriTemplateVariables(SESSION_URI_PATTERN_ANT, httpRequestPath)
      val uuidStr = variables[SESSION_ID_PATH_VARIABLE_NAME]
      sessionId = UUID.fromString(uuidStr)
    } catch (ex: IllegalArgumentException) {
      log.error("Failed to parse sessionId from path: $httpRequestPath", ex)
    }

    return sessionId
  }
}
