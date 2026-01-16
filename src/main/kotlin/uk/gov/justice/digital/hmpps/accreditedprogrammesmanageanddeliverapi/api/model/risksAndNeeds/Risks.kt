package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.toModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.listOfActiveRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
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

  @Schema(
    example = "true",
    required = true,
    description = "Whether the data represents an OGRS3 legacy assessment.",
  )
  var isLegacy: Boolean = true,

  @Schema(
    required = false,
    description = "The OGRS4 Risk predictors for this person.",
  )
  val ogrS4Risks: OGRS4Risks? = null,
)

fun buildRiskModel(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  riskPredictors: AllPredictorVersioned<*>?,
  alerts: NDeliusRegistrations?,
  now: LocalDate? = LocalDate.now(),
): Risks {
  // Map common fields to Risks model
  val risks = Risks(
    // SARA
    sara = oasysRelationships?.sara,

    // ROSH
    riskOfSeriousHarm = oasysRoshSummary?.toModel() ?: RoshSummary(),

    // Alerts
    alerts = alerts?.listOfActiveRegistrations(),

    // Relevant dates
    assessmentCompleted = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
    dateRetrieved = now,
    lastUpdated = oasysOffendingInfo?.latestCompleteDate?.toLocalDate(),
  )
  // For pre-OGRS4 assessments add the "legacy" fields
  return if (riskPredictors is AllPredictorVersionedLegacyDto) {
    risks.copy(
      isLegacy = true,
      offenderGroupReconviction = riskPredictors.output?.groupReconvictionScore?.toModel() ?: Score(),
      offenderViolencePredictor = riskPredictors.output?.violencePredictorScore?.toModel() ?: Score(),
      riskOfSeriousRecidivism = riskPredictors.output?.riskOfSeriousRecidivismScore?.toModel(oasysOffendingInfo) ?: RiskOfSeriousRecidivism(),
    )
  } else { // For post-OGRS4 assessments add the new fields
    val allPredictorVersionedDto = riskPredictors as AllPredictorVersionedDto
    val output = allPredictorVersionedDto.output
    risks.copy(
      isLegacy = false,
      ogrS4Risks = OGRS4Risks(
        // All reoffending
        allReoffendingScoreType = output?.allReoffendingPredictor?.staticOrDynamic?.type,
        allReoffendingScore = output?.allReoffendingPredictor?.score,
        allReoffendingBand = output?.allReoffendingPredictor?.band?.type,

        // Violent reoffending
        violentReoffendingScoreType = output?.violentReoffendingPredictor?.staticOrDynamic?.type,
        violentReoffendingScore = output?.violentReoffendingPredictor?.score,
        violentReoffendingBand = output?.violentReoffendingPredictor?.band?.type,

        // Serious violent reoffending
        seriousViolentReoffendingScoreType = output?.seriousViolentReoffendingPredictor?.staticOrDynamic?.type,
        seriousViolentReoffendingScore = output?.seriousViolentReoffendingPredictor?.score,
        seriousViolentReoffendingBand = output?.seriousViolentReoffendingPredictor?.band?.type,

        // Sexual reoffending predictors
        directContactSexualReoffendingScore = output?.directContactSexualReoffendingPredictor?.score,
        directContactSexualReoffendingBand = output?.directContactSexualReoffendingPredictor?.band?.type,

        indirectImageContactSexualReoffendingScore = output?.indirectImageContactSexualReoffendingPredictor?.score,
        indirectImageContactSexualReoffendingBand = output?.indirectImageContactSexualReoffendingPredictor?.band?.type,

        // Combined serious reoffending
        combinedSeriousReoffendingScoreType = output?.combinedSeriousReoffendingPredictor?.staticOrDynamic?.type,
        combinedSeriousReoffendingScore = output?.combinedSeriousReoffendingPredictor?.score,
        combinedSeriousReoffendingBand = output?.combinedSeriousReoffendingPredictor?.band?.type,
      ),
    )
  }
}
