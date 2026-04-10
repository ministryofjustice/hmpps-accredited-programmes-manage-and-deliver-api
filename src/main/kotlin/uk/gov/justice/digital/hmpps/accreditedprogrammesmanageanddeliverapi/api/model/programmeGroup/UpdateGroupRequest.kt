package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupGenderEnum
import java.time.LocalDate

data class UpdateGroupRequest(
  @get:JsonProperty("groupCode")
  @Schema(description = "The code for the group")
  var groupCode: String? = null,

  @get:JsonProperty("cohort")
  @Schema(
    enumAsRef = true,
    description = "Cohort for the Programme Group.",
    implementation = ProgrammeGroupCohort::class,
  )
  var cohort: ProgrammeGroupCohort? = null,

  @get:JsonProperty("sex")
  @Schema(
    enumAsRef = true,
    description = "Sex that the group is being run for",
    implementation = ProgrammeGroupGenderEnum::class,
  )
  var sex: ProgrammeGroupGenderEnum? = null,

  @get:JsonProperty("earliestStartDate")
  @Schema(description = "The earliest date the group can start")
  @JsonFormat(pattern = "d/M/yyyy")
  var earliestStartDate: LocalDate? = null,

  @get:JsonProperty("createGroupSessionSlot")
  @Schema(description = "A list of session slots for the group")
  var createGroupSessionSlot: Set<CreateGroupSessionSlot>? = null,

  @get:JsonProperty("automaticallyRescheduleOtherSessions")
  @Schema(description = "Boolean value to determine")
  var automaticallyRescheduleOtherSessions: Boolean? = null,

  @get:JsonProperty("pduName")
  @Schema(description = "The name of the PDU that the group will take place in")
  var pduName: String? = null,

  @get:JsonProperty("pduCode")
  @Schema(description = "The code of the PDU that the group will take place in")
  var pduCode: String? = null,

  @get:JsonProperty("deliveryLocationName")
  @Schema(description = "The name of the location that the group will be delivered at")
  var deliveryLocationName: String? = null,

  @get:JsonProperty("deliveryLocationCode")
  @Schema(description = "The code of the location that the group will be delivered at")
  var deliveryLocationCode: String? = null,

  @get:JsonProperty("teamMembers")
  @Schema(description = "The person code and name and type of the teamMembers of the group")
  var teamMembers: List<CreateGroupTeamMember>? = null,
)
