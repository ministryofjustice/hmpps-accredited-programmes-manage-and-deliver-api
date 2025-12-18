package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.AssessmentStatus
import java.time.LocalDateTime

data class AllPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus? = null,
  @Schema(description = "Version of the output", allowableValues = ["1"], defaultValue = "1")
  override val outputVersion: String = "1",
  override val output: RiskScoresDto? = null,
) : AllPredictorVersioned<RiskScoresDto>
