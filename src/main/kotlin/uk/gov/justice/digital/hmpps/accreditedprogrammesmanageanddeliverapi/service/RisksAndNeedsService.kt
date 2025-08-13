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

  fun getAssessments(nomisIdOrCrn: String): OasysAssessmentTimeline = when (val result = oasysApiClient.getAssessments(nomisIdOrCrn)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve Assessment for $nomisIdOrCrn reason ${result.toException().cause}")
      throw NotFoundException("No assessment found for nomisIdOrCrn: $nomisIdOrCrn")
    }

    is ClientResult.Success -> result.body
  }

  fun getOffendingInfo(nomisIdOrCrn: String): OasysOffendingInfo? = getDetails(nomisIdOrCrn, oasysApiClient::getOffendingInfo, "OffendingInfo")

  fun getRelationships(nomisIdOrCrn: String): OasysRelationships? = getDetails(nomisIdOrCrn, oasysApiClient::getRelationships, "Relationships")

  fun getRoshSummary(nomisIdOrCrn: String): OasysRoshSummary? = getDetails(nomisIdOrCrn, oasysApiClient::getRoshSummary, "RoshSummary")

  fun getRiskPredictors(nomisIdOrCrn: String): OasysRiskPredictorScores? = getDetails(nomisIdOrCrn, oasysApiClient::getRiskPredictors, "RiskPredictors")

  fun getRisksByNomisIdOrCrn(nomisIdOrCrn: String): Risks {
    val oasysOffendingInfo: OasysOffendingInfo? = getOffendingInfo(nomisIdOrCrn)
    val oasysRelationships: OasysRelationships? = getRelationships(nomisIdOrCrn)
    val oasysRoshSummary: OasysRoshSummary? = getRoshSummary(nomisIdOrCrn)
    val oasysRiskPredictorScores: OasysRiskPredictorScores? = getRiskPredictors(nomisIdOrCrn)
    val activeAlerts: NDeliusRegistrations? = getActiveAlerts(nomisIdOrCrn)

    return buildRiskModel(
      oasysOffendingInfo,
      oasysRelationships,
      oasysRoshSummary,
      oasysRiskPredictorScores,
      activeAlerts,
    )
  }

  fun getActiveAlerts(nomisIdOrCrn: String): NDeliusRegistrations? = when (val response = nDeliusIntegrationApiClient.getRegistrations(nomisIdOrCrn)) {
    is ClientResult.Failure -> {
      log.warn("Failure to retrieve ActiveAlerts for crn: $nomisIdOrCrn reason ${response.toException().message}")
      throw NotFoundException("Failure to retrieve ActiveAlerts for crn: $nomisIdOrCrn, reason: '${response.toException().message}'")
    }

    is ClientResult.Success -> {
      response.body
    }
  }

  private inline fun <T> getDetails(
    nomisIdOrCrn: String,
    fetchFunction: (Long) -> ClientResult<T>,
    entityName: String,
  ): T {
    val assessmentId = getAssessmentIdAndDate(nomisIdOrCrn)?.first
      ?: throw NotFoundException("No assessment found for prison number or crn: $nomisIdOrCrn")
    return when (val response = fetchFunction(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve $entityName data for assessmentId $assessmentId reason ${response.toException().cause}")
        throw NotFoundException("Failure to retrieve $entityName data for assessmentId: $assessmentId, reason: '${response.toException().message}'")
      }

      is ClientResult.Success -> response.body
    }
  }

  private fun getAssessmentIdAndDate(nomisIdOrCrn: String): Pair<Long, LocalDateTime?>? {
    val assessmentTimeline = getAssessments(nomisIdOrCrn)

    val assessment = assessmentTimeline.getLatestCompletedLayerThreeAssessment()

    return if (assessment == null) {
      log.warn("No completed assessment found for prison number $nomisIdOrCrn")
      null
    } else {
      Pair(assessment.id, assessment.completedAt)
    }
  }
}
