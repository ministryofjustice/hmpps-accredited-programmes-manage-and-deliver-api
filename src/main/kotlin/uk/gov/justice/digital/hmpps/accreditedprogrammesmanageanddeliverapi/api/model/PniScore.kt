package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity

@Schema(description = "Represents an individual's Programme Needs Identifier (PNI) score assessment")
data class PniScore(

  @Schema(description = "The overall intensity level derived from the PNI assessment", example = "HIGH")
  val overallIntensity: OverallIntensity,
  @Schema(description = "Detailed scores across different assessment domains")
  val domainScores: DomainScores,

  @Schema(
    example = """
      "riskScores": {
        "ogrs3": 15.0,
        "ovp": 15.0,
        "ospDc": 1.07,
        "ospIic": 0.11,
        "rsr": 1.46,
        "sara": "High"
      }
     """,
  )
  @get:JsonProperty("RiskScore") val riskScore: RiskScore,
  @Schema(example = "['impulsivity is missing ']", required = true)
  @get:JsonProperty("validationErrors") val validationErrors: List<String>,
)
