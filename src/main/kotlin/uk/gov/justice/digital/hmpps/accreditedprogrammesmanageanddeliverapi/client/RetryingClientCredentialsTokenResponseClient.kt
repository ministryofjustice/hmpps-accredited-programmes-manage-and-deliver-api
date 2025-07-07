package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client

import mu.KLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.hmpps-auth.token-request")
data class TokenRequestConfig(
  val connectTimeoutMs: Long,
  val readTimeoutMs: Long,
  val retries: Int,
  val retryDelayMs: Long,
)

data class TokenResponseDTO(
  val access_token: String,
  val token_type: String,
  val expires_in: Long,
  val scope: String?,
)

@Component
@EnableConfigurationProperties(TokenRequestConfig::class)
class RetryingClientCredentialsTokenResponseClient(
  private val config: TokenRequestConfig,
) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

  companion object : KLogging()

  val retryListener = object : RetryListener {
    override fun <T, E : Throwable> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean = true

    override fun <T, E : Throwable> close(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable?) {
      // optional: logic after retrying ends
    }

    override fun <T, E : Throwable> onError(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable) {
      logger.info("Token request failed; retrying", throwable)
    }
  }

  val factory = HttpComponentsClientHttpRequestFactory().apply {
    setConnectTimeout(config.connectTimeoutMs.toInt())
    setReadTimeout(config.readTimeoutMs.toInt())
  }

  private val restClient: RestClient = RestClient.builder()
    .requestFactory(factory)
    .messageConverters {
      it.add(FormHttpMessageConverter())
      it.add(OAuth2AccessTokenResponseHttpMessageConverter())
    }
    .build()

  override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest): OAuth2AccessTokenResponse {
    val retryTemplate = RetryTemplate().apply {
      setBackOffPolicy(
        FixedBackOffPolicy().apply {
          backOffPeriod = config.retryDelayMs
        },
      )
      setRetryPolicy(SimpleRetryPolicy(config.retries))
      setListeners(arrayOf(retryListener))
    }

    val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
      add(OAuth2ParameterNames.GRANT_TYPE, "client_credentials")
    }

    val headers = HttpHeaders().apply {
      contentType = MediaType.APPLICATION_FORM_URLENCODED
      setBasicAuth(request.clientRegistration.clientId, request.clientRegistration.clientSecret)
    }

    return retryTemplate.execute(
      RetryCallback {
        try {
          val dto = restClient.post()
            .uri(request.clientRegistration.providerDetails.tokenUri)
            .headers { it.addAll(headers) }
            .body(formData)
            .retrieve()
            .body(TokenResponseDTO::class.java)

          val tokenResponse = OAuth2AccessTokenResponse.withToken(dto.access_token)
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .expiresIn(dto.expires_in)
            .scopes(dto.scope?.split(" ")?.toSet() ?: emptySet())
            .build()
          tokenResponse ?: throw OAuth2AuthorizationException(
            OAuth2Error("invalid_token_response", "No token response received", null),
          )
        } catch (ex: RestClientException) {
          throw OAuth2AuthorizationException(
            OAuth2Error("invalid_token_response", "Failed to retrieve token: ${ex.message}", null),
            ex,
          )
        }
      },
    )
  }
}