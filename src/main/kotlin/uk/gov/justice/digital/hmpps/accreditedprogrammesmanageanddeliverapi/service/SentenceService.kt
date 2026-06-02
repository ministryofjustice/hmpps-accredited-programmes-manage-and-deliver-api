package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_SENTENCE_DETAILS_N_DELIUS
import java.time.LocalDate

@Service
class SentenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val telemetryClient: TelemetryClient,
) {

  fun getSentenceInformationByIdentifier(crn: String, eventNumber: Int?): NDeliusSentenceResponse = when (val result = nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber)) {
    is ClientResult.Success -> {
      telemetryClient.logToAppInsights(
        "${GET_SENTENCE_DETAILS_N_DELIUS.eventName}.success",
        mapOf(
          "integrationActionType" to GET_SENTENCE_DETAILS_N_DELIUS.name,
          "outcome" to "success",
        ),
      )

      result.body
    }

    is ClientResult.Failure -> {
      telemetryClient.logToAppInsights(
        "${GET_SENTENCE_DETAILS_N_DELIUS.eventName}.failure",
        mapOf(
          "integrationActionType" to GET_SENTENCE_DETAILS_N_DELIUS.name,
          "outcome" to "failure",
        ),
      )

      result.throwException()
    }
  }

  fun getSentenceEndDate(crn: String, eventNumber: Int?, sentenceType: ReferralEntitySourcedFrom?): LocalDate? {
    val sentenceInfo = getSentenceInformationByIdentifier(crn, eventNumber)
    return when (sentenceType) {
      ReferralEntitySourcedFrom.REQUIREMENT -> sentenceInfo.expectedEndDate
      ReferralEntitySourcedFrom.LICENCE_CONDITION -> sentenceInfo.licenceExpiryDate
      else -> null
    }
  }
}
