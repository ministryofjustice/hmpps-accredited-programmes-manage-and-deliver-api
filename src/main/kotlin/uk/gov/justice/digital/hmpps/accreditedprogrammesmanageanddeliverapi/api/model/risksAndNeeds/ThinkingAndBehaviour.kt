package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ThinkingAndBehaviour(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "1-Some problems")
  @get:JsonProperty("temperControl") val temperControl: String? = null,

  @Schema(example = "2 - Significant problems")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("awarenessOfConsequences") val awarenessOfConsequences: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("understandsViewsOfOthers") val understandsViewsOfOthers: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("achieveGoals") val achieveGoals: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("concreteAbstractThinking") val concreteAbstractThinking: String? = null,
)
