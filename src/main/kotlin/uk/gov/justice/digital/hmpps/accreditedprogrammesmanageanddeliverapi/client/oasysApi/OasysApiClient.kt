package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAlcoholDetail
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

  // TODO implement
//  fun getOffenceDetail(assessmentPk: Long) = getRequest<OasysOffenceDetail>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section2"
//  }

  // TODO implement
//  fun getAccommodation(assessmentPk: Long) = getRequest<OasysAccommodation>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section3"
//  }

  // TODO implement
//  fun getLearning(assessmentPk: Long) = getRequest<OasysLearning>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section4"
//  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section6"
  }

  // TODO implement
//  fun getLifestyle(assessmentPk: Long) = getRequest<OasysLifestyle>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section7"
//  }

  // TODO implement
//  fun getDrugDetail(assessmentPk: Long) = getRequest<OasysDrugDetail>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section8"
//  }

  // TODO implement
  fun getAlcoholDetail(assessmentPk: Long) = getRequest<OasysAlcoholDetail>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section9"
  }

  // TODO implement
//  fun getPsychiatric(assessmentPk: Long) = getRequest<OasysPsychiatric>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section10"
//  }

  // TODO implement
//  fun getBehaviour(assessmentPk: Long) = getRequest<OasysBehaviour>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section11"
//  }

  // TODO implement
//  fun getAttitude(assessmentPk: Long) = getRequest<OasysAttitude>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section12"
//  }

  // TODO implement
//  fun getHealth(assessmentPk: Long) = getRequest<OasysHealth>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/section13"
//  }

//  fun getRoshFull(assessmentPk: Long) = getRequest<OasysRoshFull>(OASYS_API) {
//    path = "/assessments/$assessmentPk/section/sectionroshfull"
//  }

  fun getRoshSummary(assessmentPk: Long) = getRequest<OasysRoshSummary>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshsumm"
  }

  fun getRiskPredictors(assessmentPk: Long) = getRequest<OasysRiskPredictorScores>(OASYS_API) {
    path = "/assessments/$assessmentPk/risk-predictors"
  }
}
