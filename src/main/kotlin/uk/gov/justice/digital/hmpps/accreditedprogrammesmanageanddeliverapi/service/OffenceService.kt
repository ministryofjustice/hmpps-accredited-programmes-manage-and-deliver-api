package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_OFFENCE_N_DELIUS

@Service
class OffenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val telemetryClient: TelemetryClient,
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
      telemetryClient.logToAppInsights(
        "${GET_OFFENCE_N_DELIUS.eventName}.failure",
        mapOf(
          "integrationActionType" to GET_OFFENCE_N_DELIUS.name,
          "outcome" to "failure",
        ),
      )
      log.warn("Failure to retrieve offence details for crn : $crn and event number: $eventNumber")
      throw NotFoundException("No Offences found for crn: $crn and event number: $eventNumber")
    }

    is ClientResult.Success -> {
      telemetryClient.logToAppInsights(
        "${GET_OFFENCE_N_DELIUS.eventName}.success",
        mapOf(
          "integrationActionType" to GET_OFFENCE_N_DELIUS.name,
          "outcome" to "success",
        ),
      )

      result.body
    }
  }
}
