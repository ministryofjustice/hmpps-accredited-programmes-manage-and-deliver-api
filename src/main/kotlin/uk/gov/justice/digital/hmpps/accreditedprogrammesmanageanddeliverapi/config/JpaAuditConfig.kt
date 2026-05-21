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
    val override = AuditorContext.get()
    if (override != null) return@AuditorAware Optional.of(override)

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

object AuditorContext {
  private val auditor = ThreadLocal<String?>()

  fun set(value: String?) = auditor.set(value)
  fun get(): String? = auditor.get()
  fun clear() = auditor.remove()
}
