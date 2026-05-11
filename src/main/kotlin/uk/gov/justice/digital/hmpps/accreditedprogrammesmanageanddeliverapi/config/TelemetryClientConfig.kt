package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelemetryClientConfig {

  @Bean
  fun telemetryClient(): TelemetryClient = TelemetryClient()
}

fun TelemetryClient.logToAppInsights(eventName: String, properties: Map<String, String>) = this.trackEvent(eventName, properties, null)
