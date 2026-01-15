package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.AlcoholMisuseDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Attitude
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.DrugDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.EmotionalWellbeing
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LearningNeeds
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.LifestyleAndAssociates
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Relationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.RoshAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.ThinkingAndBehaviour
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.buildLearningNeeds
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.buildRiskModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.AssessRiskAndNeedsApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getLatestCompletedLayerThreeAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.toModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import java.time.LocalDateTime

@Service
class RisksAndNeedsService(
  private val oasysApiClient: OasysApiClient,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val assessRiskAndNeedsApiClient: AssessRiskAndNeedsApiClient,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getAssessments(crn: String): OasysAssessmentTimeline = when (val result = oasysApiClient.getAssessments(crn)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve Assessment for $crn reason ${result.toException().cause}")
      throw NotFoundException("No assessment found for crn: $crn")
    }

    is ClientResult.Success -> result.body
  }

  fun getLearningNeedsForCrn(crn: String): LearningNeeds {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")
    return buildLearningNeeds(
      assessmentCompletedDate?.toLocalDate(),
      getDetails(assessmentId, oasysApiClient::getLearning, "LearningNeeds"),
      getDetails(assessmentId, oasysApiClient::getAccommodation, "OasysAccommodation"),
    )
  }

  fun getHealth(crn: String): Health {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(assessmentId, oasysApiClient::getHealth, "Health").toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getLifestyleAndAssociates(crn: String): LifestyleAndAssociates {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(assessmentId, oasysApiClient::getLifestyleAndAssociates, "LifestyleAndAssociates").toModel(
      assessmentCompletedDate?.toLocalDate(),
    )
  }

  fun getRelationshipsForCrn(crn: String): Relationships {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getRelationships,
      "Relationships",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getDrugDetails(crn: String): DrugDetails {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getDrugDetail,
      "DrugDetail",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getAlcoholMisuseDetails(crn: String): AlcoholMisuseDetails {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getAlcoholMisuseDetails,
      "AlcoholMisuseDetails",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getEmotionalWellbeing(crn: String): EmotionalWellbeing {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getEmotionalWellbeing,
      "EmotionalWellbeing",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getThinkingAndBehaviour(crn: String): ThinkingAndBehaviour {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getThinkingAndBehaviourDetails,
      "ThinkingAndBehaviour",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getAttitude(crn: String): Attitude {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")

    return getDetails(
      assessmentId,
      oasysApiClient::getAttitude,
      "Attitude",
    ).toModel(assessmentCompletedDate?.toLocalDate())
  }

  fun getRisksByCrn(crn: String): Risks {
    val assessmentId = getAssessmentIdAndDate(crn)?.first
      ?: throw NotFoundException("No assessment found for crn: $crn")

    val oasysOffendingInfo: OasysOffendingInfo =
      getDetails(assessmentId, oasysApiClient::getOffendingInfo, "OffendingInfo")
    val oasysRelationships: OasysRelationships =
      getDetails(assessmentId, oasysApiClient::getRelationships, "Relationships")
    val oasysRoshSummary: OasysRoshSummary = getDetails(assessmentId, oasysApiClient::getRoshSummary, "RoshSummary")
    val riskPredictors: AllPredictorVersioned<Any> =
      getDetails(assessmentId, assessRiskAndNeedsApiClient::getRiskPredictors, "AllPredictorVersioned")
    val activeAlerts: NDeliusRegistrations? = getActiveAlerts(crn)

    return buildRiskModel(
      oasysOffendingInfo,
      oasysRelationships,
      oasysRoshSummary,
      riskPredictors,
      activeAlerts,
    )
  }

  fun getRoshFullForCrn(crn: String): RoshAnalysis {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")
    return getDetails(assessmentId, oasysApiClient::getRoshFull, "RoshFull").toModel(assessmentCompletedDate)
  }

  fun getActiveAlerts(crn: String): NDeliusRegistrations? = when (val response = nDeliusIntegrationApiClient.getRegistrations(crn)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve ActiveAlerts for crn: $crn reason ${response.toException().message}")
      throw NotFoundException("Failure to retrieve ActiveAlerts for crn: $crn, reason: '${response.toException().message}'")
    }

    is ClientResult.Success -> {
      response.body
    }
  }

  fun getOffenceAnalysis(crn: String): OffenceAnalysis {
    val (assessmentId, assessmentCompletedDate) = getAssessmentIdAndDate(crn)
      ?: throw NotFoundException("No assessment found for crn: $crn")
    return getDetails(assessmentId, oasysApiClient::getOffenceAnalysis, "OffenceAnalysis").toModel(
      assessmentCompletedDate,
    )
  }

  private inline fun <T> getDetails(
    assessmentId: Long,
    fetchFunction: (Long) -> ClientResult<T>,
    entityName: String,
  ): T = when (val response = fetchFunction(assessmentId)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve $entityName data for assessmentId $assessmentId reason ${response.toException().cause}")
      throw NotFoundException("Failure to retrieve $entityName data for assessmentId: $assessmentId, reason: '${response.toException().message}'")
    }

    is ClientResult.Success -> response.body
  }

  private fun getAssessmentIdAndDate(crn: String): Pair<Long, LocalDateTime?>? {
    val assessmentTimeline = getAssessments(crn)
    val assessment = assessmentTimeline.getLatestCompletedLayerThreeAssessment()

    return if (assessment == null) {
      log.warn("No completed assessment found for crn $crn")
      null
    } else {
      Pair(assessment.id, assessment.completedAt)
    }
  }
}
