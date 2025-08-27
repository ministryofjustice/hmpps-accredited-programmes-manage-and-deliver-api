package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class EmotionalWellbeing(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("currPsychologicalProblems") val currPsychologicalProblems: String? = null,

  @Schema(example = "0 - No")
  @get:JsonProperty("selfHarmSuicidal") val selfHarmSuicidal: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("currPsychiatricProblems") val currPsychiatricProblems: String? = null,
)
