package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskResponse

private const val OASYS_API = "Oasys API"

@Component
class OasysApiClient(
  @Qualifier("oasysApiWebClient")
  webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getPniCalculation(nomisIdOrCrn: String, withinCommunity: Boolean = true) = getRequest<PniResponse>(OASYS_API) {
    path = "/assessments/pni/$nomisIdOrCrn?community=$withinCommunity"
  }

  fun getRiskPredictors(nomsIdOrCrn: String) = getRequest<RiskResponse>(OASYS_API) {
    path = "/assessments/$nomsIdOrCrn/risk-predictors"
  }
}
