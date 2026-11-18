package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import io.flipt.client.FliptClient
import io.flipt.client.models.ErrorStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Feature flags are served by Flipt, see https://github.com/ministryofjustice/hmpps-feature-flags. The client fetches
 * every flag in our namespace and keeps them up to date in the background, so evaluating a flag doesn't call Flipt.
 *
 * Without a flipt.url there is no client and every flag is disabled.
 */
@Configuration
@ConditionalOnProperty("flipt.url")
class FliptConfiguration(
  @Value("\${flipt.url}") private val url: String,
) {
  @Bean
  fun fliptClient(): FliptClient = FliptClient.builder()
    .url(url)
    .namespace("accredited-programmes-community")
    // If Flipt can't be reached, keep using the flags from the last successful fetch
    .errorStrategy(ErrorStrategy.FALLBACK)
    .build()
}
