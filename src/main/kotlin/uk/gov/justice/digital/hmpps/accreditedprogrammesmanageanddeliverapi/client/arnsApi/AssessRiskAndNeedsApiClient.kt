package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersioned

private const val ARNS_API = "ARNS API"

@Component
class AssessRiskAndNeedsApiClient(
  @Qualifier("arnsApiWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getRiskPredictors(assessmentId: Long) = getRequest<AllPredictorVersioned<*>>(ARNS_API) {
    path = "/assessments/id/$assessmentId/risk/predictors/all"
  }
}
