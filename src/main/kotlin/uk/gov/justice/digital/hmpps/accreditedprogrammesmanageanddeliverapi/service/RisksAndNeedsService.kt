package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Risks
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.buildRiskModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getLatestCompletedLayerThreeAssessment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import java.time.LocalDateTime

@Service
class RisksAndNeedsService(
  private val oasysApiClient: OasysApiClient,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getAssessments(crn: String): OasysAssessmentTimeline = when (val result = oasysApiClient.getAssessments(crn)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve Assessment for $crn reason ${result.toException().cause}")
      throw NotFoundException("No assessment found for crn: $crn")
    }

    is ClientResult.Success -> result.body
  }

  fun getOffendingInfo(crn: String): OasysOffendingInfo? = getDetails(crn, oasysApiClient::getOffendingInfo, "OffendingInfo")

  fun getRelationships(crn: String): OasysRelationships? = getDetails(crn, oasysApiClient::getRelationships, "Relationships")

  fun getRoshSummary(crn: String): OasysRoshSummary? = getDetails(crn, oasysApiClient::getRoshSummary, "RoshSummary")

  fun getRiskPredictors(crn: String): OasysRiskPredictorScores? = getDetails(crn, oasysApiClient::getRiskPredictors, "RiskPredictors")

  fun getRisksByCrn(crn: String): Risks {
    val oasysOffendingInfo: OasysOffendingInfo? = getOffendingInfo(crn)
    val oasysRelationships: OasysRelationships? = getRelationships(crn)
    val oasysRoshSummary: OasysRoshSummary? = getRoshSummary(crn)
    val oasysRiskPredictorScores: OasysRiskPredictorScores? = getRiskPredictors(crn)
    val activeAlerts: NDeliusRegistrations? = getActiveAlerts(crn)

    return buildRiskModel(
      oasysOffendingInfo,
      oasysRelationships,
      oasysRoshSummary,
      oasysRiskPredictorScores,
      activeAlerts,
    )
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

  private inline fun <T> getDetails(
    crn: String,
    fetchFunction: (Long) -> ClientResult<T>,
    entityName: String,
  ): T {
    val assessmentId = getAssessmentIdAndDate(crn)?.first
      ?: throw NotFoundException("No assessment found for crn: $crn")
    return when (val response = fetchFunction(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve $entityName data for assessmentId $assessmentId reason ${response.toException().cause}")
        throw NotFoundException("Failure to retrieve $entityName data for assessmentId: $assessmentId, reason: '${response.toException().message}'")
      }

      is ClientResult.Success -> response.body
    }
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
