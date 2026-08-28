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
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

class LimitedAccessOffenderAuthorisationInterceptorTest {
  private val request = mockk<HttpServletRequest>()
  private val response = mockk<HttpServletResponse>()
  private val handler = mockk<Any>()
  private val referralDetailsStrategy = mockk<ReferralDetailsLimitedAccessOffenderAuthorisationStrategy>()
  private val authenticationHolder = mockk<HmppsAuthenticationHolder>()
  private lateinit var interceptor: LimitedAccessOffenderAuthorisationInterceptor

  @BeforeEach
  fun setup() {
    interceptor = LimitedAccessOffenderAuthorisationInterceptor(true, referralDetailsStrategy, authenticationHolder)
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

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 1) { authenticationHolder.username }
    verify(exactly = 1) { request.method }
    verify(exactly = 1) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
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
    verify(exactly = 2) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { referralDetailsStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should return false if user is authorised`() {
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
    verify(exactly = 2) { request.requestURI }
    verify(exactly = 1) { referralDetailsStrategy.isSupportedPath(requestMethod, requestPath) }
    verify(exactly = 1) { referralDetailsStrategy.isAuthorised(requestPath, username) }
  }

  @Test
  fun `preHandle should return true if limited access offender check is disabled`() {
    // Given
    interceptor = LimitedAccessOffenderAuthorisationInterceptor(false, referralDetailsStrategy, authenticationHolder)

    // When
    val result = interceptor.preHandle(request, response, handler)

    // Then
    assertThat(result).isTrue
    verify(exactly = 0) { authenticationHolder.username }
    verify(exactly = 0) { request.method }
    verify(exactly = 0) { request.requestURI }
    verify(exactly = 0) { referralDetailsStrategy.isSupportedPath(any(), any()) }
    verify(exactly = 0) { referralDetailsStrategy.isAuthorised(any(), any()) }
  }
}
