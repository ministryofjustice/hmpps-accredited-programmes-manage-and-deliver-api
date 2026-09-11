package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.ReferralDetailsLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.SessionLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation.SessionNoteLimitedAccessOffenderAuthorisationStrategy
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

class LimitedAccessOffenderAuthorisationInterceptorTest {
  private val request = mockk<HttpServletRequest>()
  private val response = mockk<HttpServletResponse>()
  private val handler = mockk<Any>()
  private val referralDetailsStrategy = mockk<ReferralDetailsLimitedAccessOffenderAuthorisationStrategy>()
  private val sessionNoteStrategy = mockk<SessionNoteLimitedAccessOffenderAuthorisationStrategy>()
  private val sessionStrategy = mockk<SessionLimitedAccessOffenderAuthorisationStrategy>()
  private val authenticationHolder = mockk<HmppsAuthenticationHolder>()
  private lateinit var interceptor: LimitedAccessOffenderAuthorisationInterceptor

  @BeforeEach
  fun setup() {
    interceptor =
      LimitedAccessOffenderAuthorisationInterceptor(
        referralDetailsStrategy,
        sessionNoteStrategy,
        sessionStrategy,
        authenticationHolder,
      )
  }

  @Test
  fun `preHandle should throw an exception when username is null`() {
    // Given
    every { authenticationHolder.username } returns null

    // When
    val exception = assertThrows<AuthenticationCredentialsNotFoundException> {
      interceptor.preHandle(request, response, handler)
    }

    // Then
    assertThat(exception.message).isEqualTo("No authenticated user found")
    verify(exactly = 1) { authenticationHolder.username }
  }

  @Test
  fun `preHandle should return true if request type not supported`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionStrategy.isSupportedPath(any(), any()) } returns false

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionNoteStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionStrategy.isSupportedPath(requestMethod, requestPath) }
  }

  @Test
  fun `preHandle should return true if user is authorised`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns true
    every { referralDetailsStrategy.isAuthorised(any(), any()) } returns true

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { referralDetailsStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should throw exception if user is unauthorised`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath = "/referral-details/edf44a90-eb51-482f-b3f2-6961e439488b"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns true
    every { referralDetailsStrategy.isAuthorised(any(), any()) } returns false

    // When
    val exception = assertThrows<AccessDeniedException> {
      interceptor.preHandle(request, response, handler)
    }

    // Then
    assertThat(exception.message).isEqualTo("Access to this person's record is restricted in NDelius. Speak to your Programme Manager for more information.")
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { referralDetailsStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should return true if user is authorised to view session note`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath =
      "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/edf44a90-eb51-482f-b3f2-6961e439488b/session-notes"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isSupportedPath(any(), any()) } returns true
    every { sessionNoteStrategy.isAuthorised(any(), any()) } returns true

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { referralDetailsStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionNoteStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionNoteStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should throw exception if user is unauthorised to view session note`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath =
      "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b/referral/edf44a90-eb51-482f-b3f2-6961e439488b/session-notes"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isSupportedPath(any(), any()) } returns true
    every { sessionNoteStrategy.isAuthorised(any(), any()) } returns false

    // When
    val exception = assertThrows<AccessDeniedException> {
      interceptor.preHandle(request, response, handler)
    }

    // Then
    assertThat(exception.message).isEqualTo("Access to this person's record is restricted in NDelius. Speak to your Programme Manager for more information.")
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { referralDetailsStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionNoteStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionNoteStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should return true if user is authorised to view session`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionStrategy.isSupportedPath(any(), any()) } returns true
    every { sessionStrategy.isAuthorised(any(), any()) } returns true

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { referralDetailsStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionNoteStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { sessionNoteStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should throw exception if user is unauthorised to view session`() {
    // Given
    val username = "test-user"
    val requestMethod = "GET"
    val requestPath = "/bff/session/edf44a90-eb51-482f-b3f2-6961e439488b"
    every { authenticationHolder.username } returns username
    every { request.method } returns requestMethod
    every { request.requestURI } returns requestPath
    every { referralDetailsStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isSupportedPath(any(), any()) } returns false
    every { sessionNoteStrategy.isAuthorised(any(), any()) } returns false
    every { sessionStrategy.isSupportedPath(any(), any()) } returns true
    every { sessionStrategy.isAuthorised(any(), any()) } returns false

    // When
    val exception = assertThrows<AccessDeniedException> {
      interceptor.preHandle(request, response, handler)
    }

    // Then
    assertThat(exception.message).isEqualTo("Access to this person's record is restricted in NDelius. Speak to your Programme Manager for more information.")
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { referralDetailsStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionNoteStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 0) { sessionNoteStrategy.isAuthorised(any(), any()) }
    verify(exactly = 1) { sessionStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { sessionStrategy.isAuthorised(requestPath, username) }
  }
}
