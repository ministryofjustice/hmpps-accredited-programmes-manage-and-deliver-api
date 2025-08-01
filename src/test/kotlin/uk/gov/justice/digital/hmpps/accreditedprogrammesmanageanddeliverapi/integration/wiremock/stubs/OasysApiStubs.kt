package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.RiskResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.RiskResponseFactory

class OasysApiStubs(
  val wiremock: WireMockExtension,
  val objectMapper: ObjectMapper,
) {

  fun stubSuccessfulPniResponse(crn: String, pniResponse: PniResponse = PniResponseFactory().produce()) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/pni/$crn?community=true"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(pniResponse)),
        ),
    )
  }

  fun stubNotFoundPniResponse(crn: String) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/pni/$crn?community=true"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulRiskPredictorsResponse(crn: String, riskResponse: RiskResponse = RiskResponseFactory().produce()) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$crn/risk-predictors"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(riskResponse)),
        ),
    )
  }

  fun stubNotFoundRiskPredictorsResponse(crn: String) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$crn/risk-predictors"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }
}
