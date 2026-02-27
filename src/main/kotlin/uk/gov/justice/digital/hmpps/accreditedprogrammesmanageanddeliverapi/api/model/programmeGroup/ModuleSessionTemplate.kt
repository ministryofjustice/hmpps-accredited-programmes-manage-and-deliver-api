package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import java.util.UUID

@Schema(description = "A session template item with basic information")
data class ModuleSessionTemplate(
  @Schema(
    description = "The unique identifier of the session template",
    required = true,
    example = "123e4567-e89b-12d3-a456-426614174000",
  )
  var id: UUID,

  @Schema(description = "The sequential number of the session within its module", required = true, example = "1")
  var number: Int,

  @Schema(description = "The display name of the session", required = true, example = "Getting started one-to-one")
  var name: String,

  @Schema(description = "The type of session schedule", required = true, example = "SCHEDULED, or CATCH_UP")
  var sessionScheduleType: SessionScheduleType = SessionScheduleType.SCHEDULED,
)

fun ModuleSessionTemplateEntity.toApi(): ModuleSessionTemplate {
  assert(this.id != null)
  return ModuleSessionTemplate(
    id = this.id!!,
    number = this.sessionNumber,
    name = this.name,
  )
}
