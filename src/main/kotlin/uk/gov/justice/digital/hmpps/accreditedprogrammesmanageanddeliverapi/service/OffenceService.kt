package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException

@Service
class OffenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getOffenceHistory(referral: ReferralEntity): OffenceHistory {
    if (referral.eventNumber == null) {
      log.warn("Referral event number is null for referral id: ${referral.id}")
      throw BusinessException("Failure to retrieve offence history for crn: ${referral.crn} due to missing event number")
    }

    return getOffences(referral.crn, referral.eventNumber!!).toApi()
  }

  fun getOffences(crn: String, eventNumber: Int): Offences = when (val result = nDeliusIntegrationApiClient.getOffences(crn, eventNumber)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve offence details for crn : $crn and event number: $eventNumber")
      throw NotFoundException("No Offences found for crn: $crn and event number: $eventNumber")
    }
    is ClientResult.Success -> result.body
  }
}
