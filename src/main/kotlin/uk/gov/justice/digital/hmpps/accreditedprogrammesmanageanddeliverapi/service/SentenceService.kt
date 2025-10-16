package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate

@Service
class SentenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {

  fun getSentenceInformationByIdentifier(crn: String, eventNumber: Int?): NDeliusSentenceResponse = when (val result = nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber)) {
    is ClientResult.Success -> result.body
    is ClientResult.Failure -> result.throwException()
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
