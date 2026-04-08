package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort

@Schema(description = "Response model for the edit cohort page")
data class EditGroupCohort(
  @Schema(
    example = "Select a cohort",
    required = true,
    description = "Caption text displayed above the radio options",
  )
  @get:JsonProperty("captionText", required = true)
  val captionText: String,

  @Schema(
    example = "Edit the group cohort",
    required = true,
    description = "Text to display the page title",
  )
  val pageTitle: String,

  @Schema(
    example = "Submit",
    required = true,
    description = "Text to display on the submit button of the page",
  )
  val submitButtonText: String,

  @Schema(
    required = true,
    description = "List of radio button options for cohort selection",
  )
  @get:JsonProperty("radios", required = true)
  val radios: List<RadioOptions>,
) {

  @Schema(description = "A single radio button option")
  data class RadioOptions(
    @Schema(
      example = "Cohort A",
      required = true,
      description = "Display label for the radio option",
    )
    @get:JsonProperty("text", required = true)
    val text: String,

    @Schema(
      enumAsRef = true,
      description = "Cohort for the Programme Group.",
      implementation = ProgrammeGroupCohort::class,
    )
    @get:JsonProperty("value", required = true)
    val value: String,

    @Schema(
      example = "false",
      required = true,
      description = "Whether this option is currently selected",
    )
    @get:JsonProperty("selected", required = true)
    val selected: Boolean,
  )
}
