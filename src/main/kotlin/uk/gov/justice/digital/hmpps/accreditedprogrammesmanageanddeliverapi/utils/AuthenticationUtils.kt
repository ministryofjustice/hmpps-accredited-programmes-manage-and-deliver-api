package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Component
class AuthenticationUtils(
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  fun getUsername(): String {
    val username = authenticationHolder.username
    if (username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    return username
  }
}
