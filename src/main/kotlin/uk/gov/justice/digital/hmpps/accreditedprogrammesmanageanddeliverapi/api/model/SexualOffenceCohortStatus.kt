package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents whether an individual is in the sexual offence cohort")
data class SexualOffenceCohortStatus(
  @Schema(description = "True if the individual is in the sexual offence cohort")
  val inSexualOffenceCohort: Boolean
)
