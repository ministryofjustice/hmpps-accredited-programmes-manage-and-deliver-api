package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.arns.AllPredictorVersionedDtoFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.arns.AllPredictorVersionedLegacyDtoFactory

@TestComponent
class ArnsApiStubs {

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  fun stubSuccessfulLegacyRiskPredictorsResponse(
    assessmentId: Long,
    allPredictorVersionedLegacyDto: AllPredictorVersionedLegacyDto = AllPredictorVersionedLegacyDtoFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/id/$assessmentId/risk/predictors/all"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(allPredictorVersionedLegacyDto)),
        ),
    )
  }

  fun stubSuccessfulRiskPredictorsResponse(
    assessmentId: Long,
    allPredictorVersionedDto: AllPredictorVersionedDto = AllPredictorVersionedDtoFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/id/$assessmentId/risk/predictors/all"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(allPredictorVersionedDto)),
        ),
    )
  }

  fun stubNotFoundRiskPredictorsResponse(assessmentId: Long) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/id/$assessmentId/risk/predictors/all"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }
}
