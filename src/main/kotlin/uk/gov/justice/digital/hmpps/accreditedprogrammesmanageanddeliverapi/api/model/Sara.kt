package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SaraRisk
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SaraRiskLevel

data class Sara(
  @Schema(example = "LOW", description = "The overall SARA risk score")
  @get:JsonProperty("sara") val overallResult: SaraRisk? = null,

  @Schema(example = "LOW", description = "Risk of violence towards partner")
  @get:JsonProperty("saraRiskOfViolenceTowardsPartner") val saraRiskOfViolenceTowardsPartner: String? = null,

  @Schema(example = "LOW", description = "Risk of violence towards others")
  @get:JsonProperty("saraRiskOfViolenceTowardsOthers") val saraRiskOfViolenceTowardsOthers: String? = null,

  @Schema(example = "2512235167", description = "Assessment ID relevant to the SARA version of the assessment")
  @get:JsonProperty("saraAssessmentId") val saraAssessmentId: Long? = null,
) {
  companion object {
    fun from(pniResponse: PniResponse): Sara {
      val saraRiskOfViolenceTowardsPartner = SaraRiskLevel.getRiskForPartner(pniResponse.pniCalculation?.saraRiskLevel?.toPartner)
      val saraRiskOfViolenceTowardsOthers = SaraRiskLevel.getRiskToOthers(pniResponse.pniCalculation?.saraRiskLevel?.toOther)
      return Sara(
        overallResult = SaraRisk.highestRisk(saraRiskOfViolenceTowardsPartner, saraRiskOfViolenceTowardsOthers),
        saraRiskOfViolenceTowardsPartner = saraRiskOfViolenceTowardsPartner.description,
        saraRiskOfViolenceTowardsOthers = saraRiskOfViolenceTowardsOthers.description,
        saraAssessmentId = pniResponse.assessment?.id,
      )
    }
  }
}
