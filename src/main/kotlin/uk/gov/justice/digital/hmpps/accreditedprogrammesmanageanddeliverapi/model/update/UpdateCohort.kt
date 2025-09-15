package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort

data class UpdateCohort(

  @get:JsonProperty("startDate")
  @Schema(example = "SEXUAL_OFFENCE", description = "Cohort of the referral")
  val cohort: OffenceCohort,
)
