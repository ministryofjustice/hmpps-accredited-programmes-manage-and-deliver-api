package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.LimitedAccessOffenderAuthorisationStrategy.Companion.SESSION_ID_PATH_VARIABLE_NAME
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional
class GroupSessionLimitedAccessOffenderAuthorisationStrategy(
  private val userAccessService: UserAccessService,
  private val sessionRepository: SessionRepository,
) : LimitedAccessOffenderAuthorisationStrategy {
  private val log = LoggerFactory.getLogger(this::class.java)

  companion object {
    private const val GROUP_SESSION_URI_PATTERN_REGEX =
      "^/bff/group/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/session/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?:/.*)?$"
    private const val GROUP_SESSION_URI_PATTERN_ANT = "/bff/group/{groupId}/session/{sessionId}/**"
  }

  override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean {
    val sessionUriPattern = Regex(GROUP_SESSION_URI_PATTERN_REGEX)

    return RequestMethod.GET.name == httpRequestMethod && sessionUriPattern.matches(httpRequestPath)
  }

  override fun isAuthorised(httpRequestPath: String, username: String): Boolean {
    log.info("START Checking group session limited access offender authorisation for httpRequestPath: $httpRequestPath and username: $username")
    val sessionId = getId(httpRequestPath, SESSION_ID_PATH_VARIABLE_NAME, GROUP_SESSION_URI_PATTERN_ANT) ?: return true
    val session = sessionRepository.findById(sessionId).getOrNull() ?: return true
    if (session.sessionType != SessionType.ONE_TO_ONE) return true
    val access = getUserAccess(username, session)
    val authorisation = !(access?.isExcluded ?: false)
    log.info("END Checking group session limited access offender authorisation for httpRequestPath: $httpRequestPath and username: $username with authorisation: $authorisation")

    return authorisation
  }

  private fun getUserAccess(username: String, session: SessionEntity): UserAccessService.Access? {
    val caseReferenceNumber = session.attendees.firstOrNull()?.referral?.crn ?: return null
    val limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, listOf(caseReferenceNumber))

    return limitedAccessOffenderAccessMap[caseReferenceNumber]
  }
}
