package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter

@Configuration
@EnableMethodSecurity
@EnableAsync
class SecurityConfiguration {

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
    .csrf { it.disable() }
    .authorizeHttpRequests { authorize ->
      authorize
        .requestMatchers(
          "/health/**",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/api.yml",
          "/info",
          "/swagger-ui.html",
        ).permitAll()
        .anyRequest().authenticated()
    }
    .sessionManagement { session ->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
    .oauth2ResourceServer { oauth2 ->
      oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
    }
    .build()
}
