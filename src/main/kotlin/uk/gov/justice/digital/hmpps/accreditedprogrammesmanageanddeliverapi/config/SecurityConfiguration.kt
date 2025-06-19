package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
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
                    .anyRequest().permitAll() // TODO remove this
//                  .anyRequest().hasRole("SOME_ROLE_TBD") // TODO add appropriate role here
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }

}