package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore

@Service
class PniService(
  private val oasysApiClient: OasysApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getPniCalculation(crn: String): PniScore = getPniResponse(crn)?.toPniScore() ?: PniScore.empty()

  fun getPniResponse(crn: String): PniResponse? = when (val result = oasysApiClient.getPniCalculation(crn)) {
    is ClientResult.Failure.StatusCode -> {
      if (result.status.value() == 404) {
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
