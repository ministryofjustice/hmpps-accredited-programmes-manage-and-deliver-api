package uk.gov.justice.digital.hmpps.findandreferanintervention.config

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.RetryingClientCredentialsTokenResponseClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ManageAndDeliverRestClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${hmpps-auth.baseurl}") val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${api.timeout:20s}") val timeout: Duration,
  @Value("\${webclient.connect-timeout-seconds}") private val defaultConnectTimeoutSeconds: Long,
  @Value("\${webclient.read-timeout-seconds}") private val defaultReadTimeoutSeconds: Int,
  @Value("\${webclient.write-timeout-seconds}") private val writeTimeoutSeconds: Int,
  @Value("\${manage-and-deliver-and-delius.baseurl}") private val manageAndDeliverDelius: String,
  private val webClientBuilder: WebClient.Builder,
  private val retryingClientCredentialsTokenResponseClient: RetryingClientCredentialsTokenResponseClient,
) {
  private val manageAndDeliverClientRegistrationId = "manage-and-deliver-client"

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  // TODO: Remove the health ping if no call outs to other services are made
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun ManageAndDeliverDeliusApiClient(authorizedClientManager: OAuth2AuthorizedClientManager): ManageAndDeliverRestClient = ManageAndDeliverRestClient(
    createAuthorizedWebClient(authorizedClientManager, manageAndDeliverDelius),
    manageAndDeliverClientRegistrationId,
  )

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository?,
    clientService: OAuth2AuthorizedClientService?,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials {
        it.accessTokenResponseClient(retryingClientCredentialsTokenResponseClient)
      }
      .build()

    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      clientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

    return authorizedClientManager
  }

  private fun createAuthorizedWebClient(
    clientManager: OAuth2AuthorizedClientManager,
    baseUrl: String,
    readTimeoutSeconds: Int = defaultReadTimeoutSeconds,
    connectTimeoutSeconds: Long = defaultConnectTimeoutSeconds,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager)

    val httpClient = HttpClient.create()
      .doOnConnected {
        it
          .addHandlerLast(ReadTimeoutHandler(readTimeoutSeconds))
          .addHandlerLast(WriteTimeoutHandler(writeTimeoutSeconds))
      }
      .responseTimeout(Duration.ofSeconds(connectTimeoutSeconds))

    return webClientBuilder
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .baseUrl(baseUrl)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }
}
