package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary

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

  fun getAssessments(nomisIdOrCrn: String) = getRequest<OasysAssessmentTimeline>(OASYS_API) {
    path = "/assessments/timeline/$nomisIdOrCrn"
  }

  fun getOffendingInfo(assessmentPk: Long) = getRequest<OasysOffendingInfo>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section1"
  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section6"
  }

  fun getRoshSummary(assessmentPk: Long) = getRequest<OasysRoshSummary>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshsumm"
  }

  fun getRiskPredictors(assessmentPk: Long) = getRequest<OasysRiskPredictorScores>(OASYS_API) {
    path = "/assessments/$assessmentPk/risk-predictors"
  }
}
