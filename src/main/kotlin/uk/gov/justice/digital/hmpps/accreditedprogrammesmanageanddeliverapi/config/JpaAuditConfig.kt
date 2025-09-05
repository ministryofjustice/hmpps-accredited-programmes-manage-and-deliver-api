package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.Optional

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
class JpaAuditConfig {
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
}
