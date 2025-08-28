package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class AlcoholMisuseDetails(
  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("currentUse") val currentUse: String?,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("bingeDrinking") val bingeDrinking: String?,

  @Schema(example = "2-Significant problems")
  @get:JsonProperty("frequencyAndLevel") val frequencyAndLevel: String?,

  @Schema(example = "Alcohol dependency affecting employment and relationships")
  @get:JsonProperty("alcoholIssuesDetails") val alcoholIssuesDetails: String?,
)
