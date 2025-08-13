package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.listOfActiveRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getHighestPriorityScore
import java.math.BigDecimal
import java.time.LocalDate

/**
 *
 * @param ogrsYear1
 * @param ogrsYear2
 * @param ogrsRisk
 * @param ovpYear1
 * @param ovpYear2
 * @param ovpRisk
 * @param rsrScore
 * @param rsrRisk
 * @param ospcScore
 * @param ospiScore
 * @param overallRoshLevel
 * @param riskPrisonersCustody
 * @param riskStaffCustody
 * @param riskKnownAdultCustody
 * @param riskPublicCustody
 * @param riskChildrenCustody
 * @param riskStaffCommunity
 * @param riskKnownAdultCommunity
 * @param riskPublicCommunity
 * @param riskChildrenCommunity
 * @param saraScorePartner
 * @param saraScoreOthers
 * @param alerts
 * @param dateRetrieved
 * @param lastUpdated
 */
data class Risks(

  @get:JsonProperty("assessmentCompleted", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val assessmentCompleted: LocalDate? = null,

  @Schema(example = "45", description = "Offender Group Reconviction score year 1", required = false)
  @get:JsonProperty("ogrsYear1") val ogrsYear1: BigDecimal? = null,

  @Schema(example = "65", description = "Offender Group Reconviction score year 1", required = false)
  @get:JsonProperty("ogrsYear2") val ogrsYear2: BigDecimal? = null,

  @Schema(example = "High", description = "Offender Group Reconviction risk level", required = false)
  @get:JsonProperty("ogrsRisk") val ogrsRisk: String? = null,

  @Schema(example = "23", description = "Offender Violence Predictor year 1", required = false)
  @get:JsonProperty("ovpYear1") val ovpYear1: BigDecimal? = null,

  @Schema(example = "32", description = "Offender Violence Predictor year 2", required = false)
  @get:JsonProperty("ovpYear2") val ovpYear2: BigDecimal? = null,

  @Schema(example = "Medium", description = "Offender Violence Predictor risk level", required = false)
  @get:JsonProperty("ovpRisk") val ovpRisk: String? = null,

  @Schema(example = "Low", description = "Risk of violence towards partner", required = false)
  @get:JsonProperty("saraScorePartner") val saraScorePartner: String? = null,

  @Schema(example = "Low", description = "Risk of violence towards others", required = false)
  @get:JsonProperty("saraScoreOthers") val saraScoreOthers: String? = null,

  @Schema(example = "3.45", description = "Risk of Serious Recidivism score", required = false)
  @get:JsonProperty("rsrScore") val rsrScore: BigDecimal? = null,

  @Schema(example = "Medium", description = "Risk of Serious Recidivism level", required = false)
  @get:JsonProperty("rsrRisk") val rsrRisk: String? = null,

  @Schema(example = "Low", description = "Other person(s) at risk - Children", required = false)
  @get:JsonProperty("ospcScore") val ospcScore: String? = null,

  @Schema(example = "High", description = "Other person(s) at risk - Intimate", required = false)
  @get:JsonProperty("ospiScore") val ospiScore: String? = null,

  @Schema(example = "Low", description = "Risk of Serious Harm level", required = false)
  @get:JsonProperty("overallRoshLevel") val overallRoshLevel: String? = null,

  @Schema(example = "Medium", description = "Risk towards prisoners in Custody", required = false)
  @get:JsonProperty("riskPrisonersCustody") val riskPrisonersCustody: String? = null,

  @Schema(example = "Medium", description = "Risk towards staff in Custody", required = false)
  @get:JsonProperty("riskStaffCustody") val riskStaffCustody: String? = null,

  @Schema(example = "Medium", description = "Risk towards known adult in Custody", required = false)
  @get:JsonProperty("riskKnownAdultCustody") val riskKnownAdultCustody: String? = null,

  @Schema(example = "Medium", description = "Risk towards public in Custody", required = false)
  @get:JsonProperty("riskPublicCustody") val riskPublicCustody: String? = null,

  @Schema(example = "Medium", description = "Risk towards children in Custody", required = false)
  @get:JsonProperty("riskChildrenCustody") val riskChildrenCustody: String? = null,

  @Schema(example = "Medium", description = "Risk towards staff in Community", required = false)
  @get:JsonProperty("riskStaffCommunity") val riskStaffCommunity: String? = null,

  @Schema(example = "Medium", description = "Risk towards known adults in Community", required = false)
  @get:JsonProperty("riskKnownAdultCommunity") val riskKnownAdultCommunity: String? = null,

  @Schema(example = "Medium", description = "Risk towards the public in Community", required = false)
  @get:JsonProperty("riskPublicCommunity") val riskPublicCommunity: String? = null,

  @Schema(example = "Medium", description = "Risk towards children in Community", required = false)
  @get:JsonProperty("riskChildrenCommunity") val riskChildrenCommunity: String? = null,

  @Schema(example = "null", description = "Active alerts for a person", required = false)
  @get:JsonProperty("alerts") val alerts: List<String>? = null,

  @Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("dateRetrieved", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val dateRetrieved: LocalDate,

  @Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("lastUpdated", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val lastUpdated: LocalDate? = null,
)

fun buildRiskModel(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  oasysRiskPredictorScores: OasysRiskPredictorScores?,
  alerts: NDeliusRegistrations?,
) = Risks(
  assessmentCompleted = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
  ogrsYear1 = oasysRiskPredictorScores?.groupReconvictionScore?.oneYear,
  ogrsYear2 = oasysRiskPredictorScores?.groupReconvictionScore?.twoYears,
  ogrsRisk = oasysRiskPredictorScores?.groupReconvictionScore?.scoreLevel,

  ovpYear1 = oasysRiskPredictorScores?.violencePredictorScore?.oneYear,
  ovpYear2 = oasysRiskPredictorScores?.violencePredictorScore?.twoYears,
  ovpRisk = oasysRiskPredictorScores?.violencePredictorScore?.scoreLevel,

  saraScoreOthers = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsOthers,
  saraScorePartner = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsPartner,

  rsrScore = oasysRiskPredictorScores?.riskOfSeriousRecidivismScore?.percentageScore,
  rsrRisk = oasysRiskPredictorScores?.riskOfSeriousRecidivismScore?.scoreLevel,

  ospcScore = oasysOffendingInfo?.ospDCRisk ?: oasysOffendingInfo?.ospCRisk,
  ospiScore = oasysOffendingInfo?.ospIICRisk ?: oasysOffendingInfo?.ospIRisk,

  riskPrisonersCustody = oasysRoshSummary?.riskPrisonersCustody?.type,
  riskStaffCustody = oasysRoshSummary?.riskStaffCustody?.type,
  riskStaffCommunity = oasysRoshSummary?.riskStaffCommunity?.type,
  riskKnownAdultCustody = oasysRoshSummary?.riskKnownAdultCustody?.type,
  riskKnownAdultCommunity = oasysRoshSummary?.riskKnownAdultCommunity?.type,
  riskPublicCustody = oasysRoshSummary?.riskPublicCustody?.type,
  riskPublicCommunity = oasysRoshSummary?.riskPublicCommunity?.type,
  riskChildrenCustody = oasysRoshSummary?.riskChildrenCustody?.type,
  riskChildrenCommunity = oasysRoshSummary?.riskChildrenCommunity?.type,

  overallRoshLevel = oasysRoshSummary?.getHighestPriorityScore()?.type,
  alerts = alerts?.listOfActiveRegistrations(),
  dateRetrieved = LocalDate.now(),
  lastUpdated = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
)
