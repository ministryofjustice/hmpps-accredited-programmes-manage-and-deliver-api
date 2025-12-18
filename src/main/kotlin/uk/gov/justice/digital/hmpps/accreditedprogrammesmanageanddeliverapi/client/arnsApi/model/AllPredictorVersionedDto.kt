package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.AssessmentStatus
import java.time.LocalDateTime

data class AllPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus? = null,
  @Schema(description = "Version of the output", allowableValues = ["2"], defaultValue = "2")
  override val outputVersion: String = "2",
  override val output: AllPredictorDto? = null,
) : AllPredictorVersioned<AllPredictorDto>
