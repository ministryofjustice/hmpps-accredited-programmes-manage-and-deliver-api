package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType.GET_PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType.GET_SESSION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType.GET_SESSION_NOTE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.ReferralDetailsLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.SessionLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.SessionNoteLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Component
class LimitedAccessOffenderAuthorisationInterceptor(
  private val referralDetailsStrategy: ReferralDetailsLimitedAccessOffenderAuthorisationStrategy,
  private val sessionNoteStrategy: SessionNoteLimitedAccessOffenderAuthorisationStrategy,
  private val sessionStrategy: SessionLimitedAccessOffenderAuthorisationStrategy,
  private val authenticationHolder: HmppsAuthenticationHolder,
) : HandlerInterceptor {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    log.info("START Checking for limited access offender authorisation")
    val username = authenticationHolder.username

    if (username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }

    val requestUri = request.requestURI
    val requestMethod = request.method
    val requestType = getHttpRequestType(requestMethod, requestUri)
    val isAuthorisedRequest = when (requestType) {
      GET_PERSONAL_DETAILS -> referralDetailsStrategy.isAuthorised(requestUri, username)
      GET_SESSION_NOTE -> sessionNoteStrategy.isAuthorised(requestUri, username)
      GET_SESSION -> sessionStrategy.isAuthorised(requestUri, username)
      else -> true
    }

    if (!isAuthorisedRequest) {
      log.warn("Unauthorised request encountered for: $requestUri")
      throw AccessDeniedException("Access to this person's record is restricted in NDelius. Speak to your Programme Manager for more information.")
    }

    log.info("END Checking for limited access offender authorisation")
    return true
  }

  private fun getHttpRequestType(requestMethod: String, requestUri: String): HttpRequestType? = when {
    referralDetailsStrategy.isSupportedPath(requestMethod, requestUri) -> GET_PERSONAL_DETAILS
    sessionNoteStrategy.isSupportedPath(requestMethod, requestUri) -> GET_SESSION_NOTE
    sessionStrategy.isSupportedPath(requestMethod, requestUri) -> GET_SESSION
    else -> null
  }
}
