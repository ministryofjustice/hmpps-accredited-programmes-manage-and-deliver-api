package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response model for the edit treatment managers and facilitators page")
data class GroupTreatmentManagerAndFacilitatorDetails(
  @Schema(
    example = "Edit group BC123",
    required = true,
    description = "Caption text displayed above the form",
  )
  @get:JsonProperty("captionText", required = true)
  val captionText: String,

  @Schema(
    example = "Edit who is responsible for the group",
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
    example = "Archibald Queenie",
    required = true,
    description = "Treatment manager for the group",
  )
  @get:JsonProperty("treatmentManager", required = true)
  val treatmentManager: String,

  @Schema(
    example = "[\"Chloe Ransom\", \"Jordan Lee\"]",
    required = true,
    description = "List of regular facilitators assigned to the group",
  )
  @get:JsonProperty("regularFacilitators", required = true)
  val regularFacilitators: List<String>,

  @Schema(
    example = "[\"James Samhim\"]",
    description = "List of cover facilitators assigned to the group",
  )
  @get:JsonProperty("coverFacilitators")
  val coverFacilitators: List<String>,
)
