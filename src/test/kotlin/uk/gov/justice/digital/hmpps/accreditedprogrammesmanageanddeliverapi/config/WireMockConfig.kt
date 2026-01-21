package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class WireMockConfig {

  @Bean
  fun wireMockServer(): WireMockServer = WireMockHolder.server
}
