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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysSara
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Score
import java.time.LocalDate

data class Risks(

  @get:JsonProperty("assessmentCompleted", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val assessmentCompleted: LocalDate? = null,

  @Schema(description = "Offender Group Reconviction scale", required = false)
  @get:JsonProperty("offenderGroupReconviction") val offenderGroupReconviction: Score? = null,

  @Schema(description = "Offender Violence Predictor", required = false)
  @get:JsonProperty("offenderViolencePredictor") val offenderViolencePredictor: Score? = null,

  @Schema(description = "Spousal Assault Risk Assessment", required = false)
  @get:JsonProperty("sara") val sara: OasysSara? = null,

  @Schema(description = "Risk of Serious Recidivism", required = false)
  @get:JsonProperty("riskOfSeriousRecidivism") val riskOfSeriousRecidivism: RiskOfSeriousRecidivism? = null,

  @Schema(description = "Risk of Serious Harm", required = false)
  @get:JsonProperty("riskOfSeriousHarm") val riskOfSeriousHarm: RoshSummary? = null,

  @Schema(
    example = "[\"Domestic Abuse History\",\"MAPPA\",\"Domestic Abuse Victim\",]",
    description = "Active alerts for a person",
    required = false,
  )
  @get:JsonProperty("alerts") val alerts: List<String>? = null,

  @Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("dateRetrieved", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val dateRetrieved: LocalDate? = LocalDate.now(),

  @Schema(
    example = "1 August 2025",
    required = true,
    description = "The date this data was fetched from nDelius.",
  )
  @get:JsonProperty("lastUpdated", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val lastUpdated: LocalDate? = LocalDate.now(),
)

fun buildRiskModel(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  oasysRiskPredictorScores: OasysRiskPredictorScores?,
  alerts: NDeliusRegistrations?,
  now: LocalDate? = LocalDate.now(),
) = Risks(
  assessmentCompleted = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
  // If these properties are null create an object with null values so the UI can still deserialise
  offenderGroupReconviction = oasysRiskPredictorScores?.groupReconvictionScore ?: Score(null, null, null),
  offenderViolencePredictor = oasysRiskPredictorScores?.violencePredictorScore ?: Score(null, null, null),
  sara = oasysRelationships?.sara,
  riskOfSeriousRecidivism = oasysRiskPredictorScores?.riskOfSeriousRecidivismScore?.toModel(oasysOffendingInfo),
  riskOfSeriousHarm = oasysRoshSummary?.toModel(),
  alerts = alerts?.listOfActiveRegistrations(),
  dateRetrieved = now,
  lastUpdated = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
)
