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
  @NotBlank(message = "groupCode must not be null")
  @get:JsonProperty("groupCode", required = true)
  @Schema(description = "The code for the group")
  var groupCode: String,

  @NotNull(message = "cohort must not be null")
  @get:JsonProperty("cohort", required = true)
  @Schema(
    enumAsRef = true,
    description = "Cohort for the Programme Group.",
    implementation = ProgrammeGroupCohort::class,
  )
  var cohort: ProgrammeGroupCohort,

  @NotNull(message = "sex must not be null")
  @get:JsonProperty("sex", required = true)
  @Schema(
    enumAsRef = true,
    description = "Sex that the group is being run for",
    implementation = ProgrammeGroupSexEnum::class,
  )
  var sex: ProgrammeGroupSexEnum,

  @NotNull(message = "startedAt must not be null")
  @get:JsonProperty("startedAtDate", required = true)
  @Schema(description = "The date the group started")
  @JsonFormat(pattern = "d/M/yyyy")
  var startedAtDate: LocalDate,

  @NotNull(message = "createGroupSessionSlot must not be null")
  @get:JsonProperty("createGroupSessionSlot", required = true)
  @Schema(description = "A list of session slots for the group")
  var createGroupSessionSlot: Set<CreateGroupSessionSlot>,

  @NotBlank(message = "pduName must not be null")
  @get:JsonProperty("pduName", required = true)
  @Schema(description = "The name of the PDU that the group will take place in")
  var pduName: String,

  @NotBlank(message = "pduCode must not be null")
  @get:JsonProperty("pduCode", required = true)
  @Schema(description = "The code of the PDU that the group will take place in")
  var pduCode: String,

  @NotBlank(message = "deliveryLocationName must not be null")
  @get:JsonProperty("deliveryLocationName", required = true)
  @Schema(description = "The name of the location that the group will be delivered at")
  var deliveryLocationName: String,

  @NotBlank(message = "deliveryLocationCode must not be null")
  @get:JsonProperty("deliveryLocationCode", required = true)
  @Schema(description = "The code of the location that the group will be delivered at")
  var deliveryLocationCode: String,

  @Valid
  @NotNull(message = "teamMembers must not be null")
  @NotEmpty(message = "teamMembers must not be empty")
  @get:JsonProperty("teamMembers", required = true)
  @Schema(description = "The person code and name and type of the teamMembers of the group")
  var teamMembers: List<CreateGroupTeamMember>,
)

data class CreateGroupTeamMember(
  @NotNull(message = "facilitator must not be null")
  @get:JsonProperty("facilitator", required = true)
  @Schema(description = "The full name of the facilitator for the group")
  var facilitator: String,

  @NotNull(message = "facilitatorCode must not be null")
  @get:JsonProperty("facilitatorCode", required = true)
  @Schema(description = "The code of the facilitator for the group")
  var facilitatorCode: String,

  @NotNull
  @get:JsonProperty("teamName", required = true)
  @Schema(description = "The name of the team that the member belongs to")
  var teamName: String,

  @NotNull
  @get:JsonProperty("ndeliusTeamCode", required = true)
  @Schema(description = "The code of the team that the member belongs to")
  var ndeliusTeamCode: String,

  @NotNull(message = "teamMemberType must not be null")
  @get:JsonProperty("teamMemberType", required = true)
  @Schema(description = "The type of the facilitator for the group")
  var teamMemberType: CreateGroupTeamMemberType,
)

fun CreateGroupRequest.toEntity(region: String): ProgrammeGroupEntity {
  val (cohort, isLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
  return ProgrammeGroupEntity(
    code = groupCode,
    cohort = cohort,
    sex = sex,
    isLdc = isLdc,
    regionName = region,
    startedAtDate = startedAtDate,
    deliveryLocationCode = deliveryLocationCode,
    deliveryLocationName = deliveryLocationName,
    probationDeliveryUnitCode = pduCode,
    probationDeliveryUnitName = pduName,
  )
}
