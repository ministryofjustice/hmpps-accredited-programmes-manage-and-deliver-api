package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "A session template item with basic information")
data class SessionTemplateItem(
  @Schema(description = "The unique identifier of the session template", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
  val id: UUID,

  @Schema(description = "The sequential number of the session within its module", required = true, example = "1")
  val number: Int,

  @Schema(description = "The display name of the session", required = true, example = "Getting started one-to-one")
  val name: String,
)
