package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
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
        .anyRequest().hasRole("ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR")
    }
    .sessionManagement { session ->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
    .oauth2ResourceServer { oauth2 ->
      oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
    }
    .build()
}
