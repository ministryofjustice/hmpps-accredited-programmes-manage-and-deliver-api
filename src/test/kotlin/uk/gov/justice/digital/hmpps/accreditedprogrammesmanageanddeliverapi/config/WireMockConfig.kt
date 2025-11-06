package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class WireMockConfig {

  @Bean(initMethod = "start", destroyMethod = "stop")
  fun wireMockServer(): WireMockServer {
    val server = WireMockServer(
      wireMockConfig()
        .port(8095),
    )
    return server
  }
}
