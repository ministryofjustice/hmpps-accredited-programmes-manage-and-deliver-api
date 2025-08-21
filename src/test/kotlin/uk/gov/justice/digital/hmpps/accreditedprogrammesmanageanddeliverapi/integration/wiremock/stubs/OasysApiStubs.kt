package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAccommodation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysHealth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshFull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAccommodationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysAssessmentTimelineFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysHealthFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysLearningFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysOffendingInfoFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRelationshipsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRiskPredictorScoresFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRoshFullFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys.OasysRoshSummaryFactory

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

  fun stubSuccessfulAssessmentsResponse(
    nomisIdOrCrn: String,
    oasysAssessmentTimeline: OasysAssessmentTimeline = OasysAssessmentTimelineFactory()
      .produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/timeline/$nomisIdOrCrn"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysAssessmentTimeline)),
        ),
    )
  }

  fun stubNotFoundAssessmentsResponse(
    nomisIdOrCrn: String,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/timeline/$nomisIdOrCrn"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysOffendingInfoResponse(
    assessmentId: Long,
    oasysOffendingInfo: OasysOffendingInfo = OasysOffendingInfoFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section1"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysOffendingInfo)),
        ),
    )
  }

  fun stubNotFoundOasysOffendingInfoResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section1"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysRelationshipsResponse(
    assessmentId: Long,
    oasysRelationships: OasysRelationships = OasysRelationshipsFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section6"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysRelationships)),
        ),
    )
  }

  fun stubNotFoundOasysRelationshipsResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section6"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysRoshSummaryResponse(
    assessmentId: Long,
    oasysRoshSummary: OasysRoshSummary = OasysRoshSummaryFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/sectionroshsumm"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysRoshSummary)),
        ),
    )
  }

  fun stubNotFoundOasysOasysRoshSummaryResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/sectionroshsumm"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysRiskPredictorScores(
    assessmentId: Long,
    oasysRiskPredictorScores: OasysRiskPredictorScores = OasysRiskPredictorScoresFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/risk-predictors"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysRiskPredictorScores)),
        ),
    )
  }

  fun stubNotFoundOasysRiskPredictorScoresResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/risk-predictors"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysLearningResponse(
    assessmentId: Long,
    oasysLearning: OasysLearning = OasysLearningFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section4"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysLearning)),
        ),
    )
  }

  fun stubNotFoundOasysLearningResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section4"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysHealthResponse(
    assessmentId: Long,
    oasysHealth: OasysHealth = OasysHealthFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section13"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysHealth)),
        ),
    )
  }

  fun stubNotFoundOasysHealthResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section13"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysRoshFullResponse(
    assessmentId: Long,
    oasysRoshFull: OasysRoshFull = OasysRoshFullFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/sectionroshfull"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysRoshFull)),
        ),
    )
  }

  fun stubNotFoundOasysRoshFullResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/sectionroshfull"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubSuccessfulOasysAccommodationResponse(
    assessmentId: Long,
    oasysAccommodation: OasysAccommodation = OasysAccommodationFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section3"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(oasysAccommodation)),
        ),
    )
  }

  fun stubNotFoundOasysAccommodationResponse(
    assessmentId: Long,
  ) {
    wiremock.stubFor(
      get(urlEqualTo("/assessments/$assessmentId/section/section3"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }
}
