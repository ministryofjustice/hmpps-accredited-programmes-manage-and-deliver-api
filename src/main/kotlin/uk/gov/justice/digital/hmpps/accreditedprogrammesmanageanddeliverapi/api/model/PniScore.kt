package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.OverallIntensity

@Schema(description = "Represents an individual's Programme Needs Identifier (PNI) score assessment")
data class PniScore(

  @Schema(description = "The overall intensity level derived from the PNI assessment", example = "HIGH")
  val overallIntensity: OverallIntensity,
  @Schema(description = "Detailed scores across different assessment domains")
  val domainScores: DomainScores,
)
