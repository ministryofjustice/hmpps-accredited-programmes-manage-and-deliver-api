package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.RsrScore
import java.math.BigDecimal

data class RiskOfSeriousRecidivism(
  @Schema(example = "3.45", description = "Risk of Serious Recidivism score")
  val scoreLevel: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk of Serious Recidivism level",
    allowableValues = ["Low", "Medium", "High"],
  )
  val percentageScore: BigDecimal? = null,
  @Schema(example = "Low", description = "Other person(s) at risk - Children", required = false)
  @get:JsonProperty("ospcScore") val otherPersonAtRiskChildrenScore: String? = null,

  @Schema(example = "High", description = "Other person(s) at risk - Intimate", required = false)
  @get:JsonProperty("ospiScore") val otherPersonAtRiskIntimateScore: String? = null,
)

fun RsrScore.toModel(
  oasysOffendingInfo: OasysOffendingInfo?,
): RiskOfSeriousRecidivism = RiskOfSeriousRecidivism(
  scoreLevel = this.scoreLevel,
  percentageScore = this.percentageScore,
  otherPersonAtRiskChildrenScore = oasysOffendingInfo?.ospDCRisk ?: oasysOffendingInfo?.ospCRisk,
  otherPersonAtRiskIntimateScore = oasysOffendingInfo?.ospIICRisk ?: oasysOffendingInfo?.ospIRisk,
)
