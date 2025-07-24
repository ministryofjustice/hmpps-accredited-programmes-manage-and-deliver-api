package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException

@Service
class PniService(
  private val oasysApiClient: OasysApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPniScore(crn: String): PniScore {
    val pniResponse =
      when (val result = oasysApiClient.getPniCalculation(crn)) {
        is ClientResult.Failure -> {
          log.warn("Failure to retrieve PNI score for crn : $crn")
          throw NotFoundException("No PNI score found for crn: $crn")
        }
        is ClientResult.Success -> result.body
      }
    return pniResponse.toPniScore()
  }
}
