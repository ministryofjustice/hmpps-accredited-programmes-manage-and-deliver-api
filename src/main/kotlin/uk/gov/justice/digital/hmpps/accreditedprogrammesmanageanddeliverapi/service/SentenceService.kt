package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_SENTENCE_DETAILS_N_DELIUS
import java.time.LocalDate

@Service
class SentenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val telemetryService: TelemetryService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getSentenceInformationByIdentifier(crn: String, eventNumber: Int?): NDeliusSentenceResponse? = when (val response = nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber)) {
    is ClientResult.Failure.StatusCode -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_SENTENCE_DETAILS_N_DELIUS.eventName}.failure",
        integrationActionType = GET_SENTENCE_DETAILS_N_DELIUS.name,
        outcome = "failure",
      )
      if (response.status.value() == 404) {
        log.warn("No Sentence information found for crn : $crn and event number: $eventNumber")
        throw NotFoundException("No Sentence information found for crn : $crn and event number: $eventNumber")
      } else {
        log.warn(
          "Failure to retrieve Sentence information for crn : $crn and event number: $eventNumber with reason ${response.toException().cause} and status code: ${response.status.value()}",
          response.toException(),
        )
        response.throwException()
      }
    }

    is ClientResult.Success -> {
      telemetryService.logToAppInsights(
        eventName = "${GET_SENTENCE_DETAILS_N_DELIUS.eventName}.success",
        integrationActionType = GET_SENTENCE_DETAILS_N_DELIUS.name,
        outcome = "success",
      )

      response.body
    }

    is ClientResult.Failure -> {
      log.error(
        "Failure to retrieve Sentence information for crn : $crn and event number: $eventNumber with reason ${response.toException().cause}",
        response.toException(),
      )
      telemetryService.logToAppInsights(
        eventName = "${GET_SENTENCE_DETAILS_N_DELIUS.eventName}.failure",
        integrationActionType = GET_SENTENCE_DETAILS_N_DELIUS.name,
        outcome = "failure",
      )

      response.throwException()
    }
  }

  fun getSentenceEndDate(crn: String, eventNumber: Int?, sentenceType: ReferralEntitySourcedFrom?): LocalDate? {
    val sentenceInfo = getSentenceInformationByIdentifier(crn, eventNumber)
    return when (sentenceType) {
      ReferralEntitySourcedFrom.REQUIREMENT -> sentenceInfo?.expectedEndDate
      ReferralEntitySourcedFrom.LICENCE_CONDITION -> sentenceInfo?.licenceExpiryDate
      else -> null
    }
  }
}
