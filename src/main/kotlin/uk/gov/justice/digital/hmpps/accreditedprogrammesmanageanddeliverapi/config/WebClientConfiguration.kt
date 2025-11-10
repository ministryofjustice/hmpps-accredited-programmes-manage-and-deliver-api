package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${api.timeout:20s}") val timeout: Duration,
  @Value("\${max-response-in-memory-size-bytes}") private val maxResponseInMemorySizeBytes: Int,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun authorizedClientManager(clients: ClientRegistrationRepository): OAuth2AuthorizedClientManager {
    val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clients)
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service)
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()
    manager.setAuthorizedClientProvider(authorizedClientProvider)
    return manager
  }

  @Bean(name = ["findAndReferInterventionApiWebClient"])
  fun findAndReferInterventionApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.find-and-refer-intervention-api.base-url}") findAndReferInterventionApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("manage-and-deliver-api-client")
    return buildWebClient(findAndReferInterventionApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["nDeliusIntegrationWebClient"])
  fun nDeliusIntegrationWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.ndelius-integration-api.base-url}") nDeliusIntegrationApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("manage-and-deliver-api-client")
    return buildWebClient(nDeliusIntegrationApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["oasysApiWebClient"])
  fun oasysApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.oasys-api.base-url}") oasysApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("manage-and-deliver-api-client")
    return buildWebClient(oasysApiBaseUrl, oauth2Client)
  }

  fun buildWebClient(url: String, oauth2Client: ServletOAuth2AuthorizedClientExchangeFilterFunction): WebClient = WebClient.builder()
    .baseUrl(url)
    .clientConnector(
      ReactorClientHttpConnector(
        HttpClient
          .create()
          .responseTimeout(timeout)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.toMillis().toInt()),
      ),
    )
    .exchangeStrategies(
      ExchangeStrategies.builder().codecs {
        it.defaultCodecs().maxInMemorySize(maxResponseInMemorySizeBytes)
      }.build(),
    )
    .filter(oauth2Client)
    .build()
}
