package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import java.util.UUID

@Schema(description = "Response model for the edit cohort page")
data class EditGroupDaysAndTimes(

  @Schema(
    example = "1ff57cea-352c-4a99-8f66-3e626aac3265",
    required = true,
    description = "A unique id identifying the programme group.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val code: String,

  @Schema(
    example = "[{\"dayOfWeek\": \"Monday\", \"hour\": 9, \"minutes\": 30, \"amOrPm\": \"AM\"}]",
    required = true,
    description = "The days and times that group sessions will be delivered.",
  )
  @get:JsonProperty("programmeGroupSessionSlots", required = true)
  var programmeGroupSessionSlots: List<CreateGroupSessionSlot> = emptyList(),

  )