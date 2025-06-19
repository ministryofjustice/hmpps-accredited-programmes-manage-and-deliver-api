package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

const val AUTHORITY_CLAIM_KEY = "authorities"

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {

  override fun convert(jwt: Jwt): AbstractAuthenticationToken = AuthAwareAuthenticationToken(
    jwt = jwt,
    authorities = extractAuthorities(jwt),
  )

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> = jwt.claims[AUTHORITY_CLAIM_KEY]
    ?.let { claimValue ->
      claimValue.let { it as Collection<*> }
        .mapNotNull { it as? String }
        .map { SimpleGrantedAuthority(it) }
    } ?: emptyList()
}

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities)
