package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class LifestyleAndAssociates(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "0 - No problems", description = "Does the person have any regular activities that encourage reoffending")
  @get:JsonProperty("regActivitiesEncourageOffending") val regActivitiesEncourageOffending: String? = null,

  @Schema(example = "There are issues around involvement with drugs", description = "Description of lifestyle issues affecting risk of offending or harm")
  @get:JsonProperty("lifestyleIssuesDetails") val lifestyleIssuesDetails: String? = null,
)
