package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client

import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets

@Service
class ManageAndDeliverRestClient(
  private val webClient: WebClient,
  private val oauth2ClientRegistrationId: String,
) {
  fun get(
    uri: String,
    queryParams: MultiValueMap<String, String>? = null,
    customAuthentication: JwtAuthenticationToken? = null,
  ): WebClient.RequestHeadersSpec<*> {
    val spec = webClient
      .get()
      .uri { uriBuilder ->
        uriBuilder
          .path(uri)
          .apply { queryParams?.let { queryParams(it) } }
          .build()
      }

    return spec
      .withDefaultHeaders()
      .withAuth(customAuthentication)
  }

  private fun WebClient.RequestHeadersSpec<*>.withDefaultHeaders(): WebClient.RequestHeadersSpec<*> = this
    .accept(MediaType.APPLICATION_JSON)
    .acceptCharset(StandardCharsets.UTF_8)

  private fun WebClient.RequestHeadersSpec<*>.withAuth(authentication: JwtAuthenticationToken?): WebClient.RequestHeadersSpec<*> = authentication?.let {
    this.header("Authorization", "Bearer ${it.token.tokenValue}")
  }
    ?: this.attributes(
      ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId(
        oauth2ClientRegistrationId,
      ),
    )
}