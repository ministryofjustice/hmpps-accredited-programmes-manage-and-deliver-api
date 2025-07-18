package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.Optional

const val AUTHORITY_CLAIM_KEY = "authorities"

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
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

@Bean
fun auditorAware() = AuditorAware {
  Optional.ofNullable(
    when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
      is String -> principal
      is UserDetails -> principal.username
      is Map<*, *> -> principal["username"] as String
      else -> null
    },
  )
}
