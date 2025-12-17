package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing session templates for scheduling")
data class ScheduleSessionTypeResponse(
  @Schema(description = "List of available session templates", required = true)
  val sessionTemplates: List<SessionTemplateItem>,
)
