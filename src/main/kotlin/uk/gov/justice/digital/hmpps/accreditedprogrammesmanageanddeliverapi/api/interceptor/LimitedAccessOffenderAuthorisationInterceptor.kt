package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.HttpRequestType.GET_PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.ReferralDetailsLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Component
class LimitedAccessOffenderAuthorisationInterceptor(
  @param:Value($$"${app.features.lao-access-check-enabled}")
  private val limitedAccessOffenderCheckEnabled: Boolean,
  private val referralDetailsStrategy: ReferralDetailsLimitedAccessOffenderAuthorisationStrategy,
  private val authenticationHolder: HmppsAuthenticationHolder,
) : HandlerInterceptor {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    log.info("START Checking for limited access offender authorisation")
    var isAuthorisedRequest = true

    if (limitedAccessOffenderCheckEnabled) {
      val username = authenticationHolder.username

      if (username.isNullOrBlank()) {
        throw AuthenticationCredentialsNotFoundException("No authenticated user found")
      }

      val requestType = getHttpRequestType(request)
      isAuthorisedRequest = when (requestType) {
        GET_PERSONAL_DETAILS -> referralDetailsStrategy.isAuthorised(request.requestURI, username)
        else -> true
      }
    }

    if (!isAuthorisedRequest) {
      throw AccessDeniedException("Access to this person's record is restricted in NDelius. Speak to your Programme Manager for more information.")
    }

    log.info("END Checking for limited access offender authorisation")
    return true
  }

  private fun getHttpRequestType(request: HttpServletRequest): HttpRequestType? = when {
    referralDetailsStrategy.isSupportedPath(request.method, request.requestURI) -> GET_PERSONAL_DETAILS
    else -> null
  }
}
