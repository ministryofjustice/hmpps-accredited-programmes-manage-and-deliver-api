
package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore
import java.time.LocalDate

@Service
class PniService(
  private val oasysApiClient: OasysApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Returns the PNI calculation for a CRN, caching the result once per day.
   * The cache key is [crn, date].
   * If OASys is unavailable, returns null and caches null for the day.
   */
  @Cacheable(value = ["pni-daily"], key = "#crn + '-' + #date", unless = "#result == null")
  fun getDailyPniCalculation(crn: String, date: LocalDate = LocalDate.now()): PniScore? = try {
    getPniResponse(crn)?.toPniScore()
  } catch (e: Exception) {
    log.warn("Failed to fetch PNI for crn $crn on $date: ${e.message}")
    null
  }

  fun getPniCalculation(crn: String): PniScore = getPniResponse(crn)?.toPniScore() ?: PniScore.empty()

  fun getPniResponse(crn: String): PniResponse? = when (val result = oasysApiClient.getPniCalculation(crn)) {
    is ClientResult.Failure.StatusCode -> {
      if (result.status.value() == 404) {
        log.warn("No PNI score found for crn : $crn")
        null
      } else {
        log.warn("Failure to retrieve PNI score for crn : $crn")
        throw result.toException()
      }
    }
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve PNI score for crn : $crn")
      throw result.toException()
    }

    is ClientResult.Success -> result.body
  }
}
