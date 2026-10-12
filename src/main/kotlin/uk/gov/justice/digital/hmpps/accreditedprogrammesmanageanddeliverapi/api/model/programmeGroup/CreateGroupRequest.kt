package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate

data class CreateGroupRequest(
  @field:NotBlank(message = "groupCode must not be null")
  @get:JsonProperty("groupCode", required = true)
  @field:Schema(description = "The code for the group")
  var groupCode: String,

  @field:NotNull(message = "cohort must not be null")
  @get:JsonProperty("cohort", required = true)
  @field:Schema(
    enumAsRef = true,
    description = "Cohort for the Programme Group.",
    implementation = ProgrammeGroupCohort::class,
  )
  var cohort: ProgrammeGroupCohort,

  @field:NotNull(message = "sex must not be null")
  @get:JsonProperty("sex", required = true)
  @field:Schema(
    enumAsRef = true,
    description = "Sex that the group is being run for",
    implementation = ProgrammeGroupSexEnum::class,
  )
  var sex: ProgrammeGroupSexEnum,

  @field:NotNull(message = "earliestStartDate must not be null")
  @get:JsonProperty("earliestStartDate", required = true)
  @field:Schema(description = "The earliest date the group can start")
  @get:JsonFormat(pattern = "d/M/yyyy")
  var earliestStartDate: LocalDate,

  @field:NotNull(message = "createGroupSessionSlot must not be null")
  @get:JsonProperty("createGroupSessionSlot", required = true)
  @field:Schema(description = "A list of session slots for the group")
  var createGroupSessionSlot: Set<CreateGroupSessionSlot>,

  @field:NotBlank(message = "pduName must not be null")
  @get:JsonProperty("pduName", required = true)
  @field:Schema(description = "The name of the PDU that the group will take place in")
  var pduName: String,

  @field:NotBlank(message = "pduCode must not be null")
  @get:JsonProperty("pduCode", required = true)
  @field:Schema(description = "The code of the PDU that the group will take place in")
  var pduCode: String,

  @field:NotBlank(message = "deliveryLocationName must not be null")
  @get:JsonProperty("deliveryLocationName", required = true)
  @field:Schema(description = "The name of the location that the group will be delivered at")
  var deliveryLocationName: String,

  @field:NotBlank(message = "deliveryLocationCode must not be null")
  @get:JsonProperty("deliveryLocationCode", required = true)
  @field:Schema(description = "The code of the location that the group will be delivered at")
  var deliveryLocationCode: String,

  @field:Valid
  @field:NotNull(message = "teamMembers must not be null")
  @field:NotEmpty(message = "teamMembers must not be empty")
  @get:JsonProperty("teamMembers", required = true)
  @field:Schema(description = "The person code and name and type of the teamMembers of the group")
  var teamMembers: List<CreateGroupTeamMember>,
)

data class CreateGroupTeamMember(
  @field:NotNull(message = "facilitator must not be null")
  @get:JsonProperty("_facilitator")
  @field:Schema(description = "The full name of the facilitator for the group")
  var _facilitator: String,

  @field:Schema(
    example = "John",
    required = false,
    description = "The forename of the facilitator for the group.",
  )
  @get:JsonProperty("facilitatorForename", required = false)
  val facilitatorForename: String? = null,

  @field:Schema(
    example = "Smith",
    required = false,
    description = "The surname of the facilitator for the group.",
  )
  @get:JsonProperty("facilitatorSurname", required = false)
  val facilitatorSurname: String? = null,

  @field:Schema(
    example = "William",
    required = false,
    description = "The middle names of the facilitator for the group.",
  )
  @get:JsonProperty("facilitatorMiddleNames", required = false)
  val facilitatorMiddleNames: String? = null,

  @field:NotNull(message = "facilitatorCode must not be null")
  @get:JsonProperty("facilitatorCode", required = true)
  @field:Schema(description = "The code of the facilitator for the group")
  var facilitatorCode: String,

  @field:NotNull
  @get:JsonProperty("teamName", required = true)
  @field:Schema(description = "The name of the team that the member belongs to")
  var teamName: String,

  @field:NotNull
  @get:JsonProperty("teamCode", required = true)
  @field:Schema(description = "The code of the team that the member belongs to")
  var teamCode: String,

  @field:NotNull(message = "teamMemberType must not be null")
  @get:JsonProperty("teamMemberType", required = true)
  @field:Schema(description = "The type of the facilitator for the group")
  var teamMemberType: CreateGroupTeamMemberType,
) {
  @get:Schema(
    example = "John Smith",
    required = true,
    description = "The full name of the facilitator.",
  )
  @get:JsonProperty("facilitator", required = true)
  val facilitator: String
    get() = if (facilitatorForename != null && facilitatorSurname != null) {
      listOfNotNull(facilitatorForename, facilitatorMiddleNames, facilitatorSurname).filter { it.isNotBlank() }
        .joinToString(" ")
    } else {
      _facilitator
    }
}

fun CreateGroupRequest.toEntity(
  region: String,
): ProgrammeGroupEntity {
  val (cohort, isLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
  return ProgrammeGroupEntity(
    code = groupCode,
    cohort = cohort,
    sex = sex,
    isLdc = isLdc,
    regionName = region,
    earliestPossibleStartDate = earliestStartDate,
    deliveryLocationCode = deliveryLocationCode,
    deliveryLocationName = deliveryLocationName,
    probationDeliveryUnitCode = pduCode,
    probationDeliveryUnitName = pduName,
  )
}
