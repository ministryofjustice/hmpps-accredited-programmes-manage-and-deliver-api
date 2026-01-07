package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.model.BankHolidaysResponse

@Component
class GovUkApiClient(
  @Qualifier("govUkApiWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Retry(name = "govUkApi")
  @Cacheable("bank-holidays", unless = "#result == null")
  fun getHolidays() = getRequest<BankHolidaysResponse>("GovUk Bank Holidays API") {
    path = "/bank-holidays.json"
  }
}
