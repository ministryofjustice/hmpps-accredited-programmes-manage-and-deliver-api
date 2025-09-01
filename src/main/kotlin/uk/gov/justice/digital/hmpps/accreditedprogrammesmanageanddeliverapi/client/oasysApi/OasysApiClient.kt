package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAccommodation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAlcoholMisuseDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAttitude
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysDrugDetail
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysEmotionalWellbeing
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysHealth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLearning
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysLifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshFull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysThinkingAndBehaviour

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

  fun getAccommodation(assessmentPk: Long) = getRequest<OasysAccommodation>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section3"
  }

  fun getLearning(assessmentPk: Long) = getRequest<OasysLearning>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section4"
  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section6"
  }

  fun getDrugDetail(assessmentPk: Long) = getRequest<OasysDrugDetail>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section8"
  }

  fun getEmotionalWellbeing(assessmentPk: Long) = getRequest<OasysEmotionalWellbeing>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section10"
  }

  fun getThinkingAndBehaviourDetails(assessmentPk: Long) = getRequest<OasysThinkingAndBehaviour>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section11"
  }

  fun getAttitude(assessmentPk: Long) = getRequest<OasysAttitude>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section12"
  }


  fun getAlcoholMisuseDetails(assessmentPk: Long) = getRequest<OasysAlcoholMisuseDetails>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section9"
  }

  fun getHealth(assessmentPk: Long) = getRequest<OasysHealth>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section13"
  }

  fun getLifestyleAndAssociates(assessmentPk: Long) = getRequest<OasysLifestyleAndAssociates>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section7"
  }

  fun getRoshSummary(assessmentPk: Long) = getRequest<OasysRoshSummary>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshsumm"
  }

  fun getRoshFull(assessmentPk: Long) = getRequest<OasysRoshFull>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshfull"
  }

  fun getRiskPredictors(assessmentPk: Long) = getRequest<OasysRiskPredictorScores>(OASYS_API) {
    path = "/assessments/$assessmentPk/risk-predictors"
  }
}
