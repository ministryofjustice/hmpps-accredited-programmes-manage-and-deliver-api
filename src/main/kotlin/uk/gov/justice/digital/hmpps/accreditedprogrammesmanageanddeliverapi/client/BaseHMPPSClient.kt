package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ServiceUnavailableException

abstract class BaseHMPPSClient(
  private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  protected inline fun <reified ResponseType : Any> getRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.GET, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> postRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.POST, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> request(
    method: HttpMethod,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String,
  ): ClientResult<ResponseType> {
    val typeReference = object : TypeReference<ResponseType>() {}

    return doRequest(typeReference, method, requestBuilderConfiguration, serviceName)
  }

  fun <ResponseType : Any> doRequest(
    typeReference: TypeReference<ResponseType>,
    method: HttpMethod,
    requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String,
  ): ClientResult<ResponseType> {
    val requestBuilder = HMPPSRequestConfiguration()
    requestBuilderConfiguration(requestBuilder)

    try {
      val request = webClient.method(method)
        .uri(requestBuilder.path ?: "")
        .headers { it.addAll(requestBuilder.headers) }

      if (requestBuilder.body != null) {
        request.bodyValue(requestBuilder.body!!)
      }

      val result = request.retrieve().toEntity<String>().block()!!

      objectMapper.apply { registerModule((JavaTimeModule())) }
      val deserialized = objectMapper.readValue(result.body, typeReference)

      return ClientResult.Success(result.statusCode, deserialized)
    } catch (exception: WebClientResponseException) {
      if (exception.statusCode.is5xxServerError) {
        log.error("Request to $serviceName failed with status code ${exception.statusCode.value()} reason ${exception.message}.")
        throw ServiceUnavailableException(
          "$serviceName is temporarily unavailable. Please try again later.",
          exception,
        )
      } else if (!exception.statusCode.is2xxSuccessful) {
        return ClientResult.Failure.StatusCode(
          method,
          requestBuilder.path ?: "",
          exception.statusCode,
          exception.responseBodyAsString,
        )
      } else {
        log.error(
          "Request to $serviceName failed with status code ${exception.statusCode.value()} reason ${exception.message}.",
          exception,
        )
        throw exception
      }
    } catch (exception: Exception) {
      log.error("Exception occurred whilst processing request: ${exception.message}.", exception)
      return ClientResult.Failure.Other(method, requestBuilder.path ?: "", exception, serviceName)
    }
  }

  class HMPPSRequestConfiguration {
    internal var path: String? = null
    internal var body: Any? = null
    internal var headers = HttpHeaders()
    fun withHeader(key: String, value: String) = headers.add(key, value)
  }
}

sealed interface ClientResult<ResponseType> {
  class Success<ResponseType>(val status: HttpStatusCode, val body: ResponseType) : ClientResult<ResponseType>
  sealed interface Failure<ResponseType> : ClientResult<ResponseType> {
    fun throwException(): Nothing = throw toException()
    fun toException(): Throwable
    fun getErrorMessage(): String

    class StatusCode<ResponseType>(
      val method: HttpMethod,
      val path: String,
      val status: HttpStatusCode,
      val body: String?,
    ) : Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("Unable to complete $method request to $path: $status")
      override fun getErrorMessage() = body ?: ""

      inline fun <reified ResponseType> deserializeTo(): ResponseType = jacksonObjectMapper().readValue(body, ResponseType::class.java)
    }

    class Other<ResponseType>(
      val method: HttpMethod,
      val path: String,
      val exception: Exception,
      val serviceName: String,
    ) : Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("Unable to complete request. Service $serviceName for $method request to $path", exception)

      override fun getErrorMessage() = exception.message ?: "Unknown error from $serviceName"
    }
  }
}
