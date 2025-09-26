package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelemetryClientConfig {

  @Bean
  fun telemetryClient(): TelemetryClient = TelemetryClient()
}

fun TelemetryClient.logToAppInsights(page: String, messages: Map<String, String>) = this.trackEvent(page, messages, null)
