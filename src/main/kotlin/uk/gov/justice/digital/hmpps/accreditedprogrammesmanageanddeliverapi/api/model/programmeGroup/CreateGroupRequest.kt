package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
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

  @NotBlank(message = "personName must not be null")
  @get:JsonProperty("personName", required = true)
  @Schema(description = "The name of the facilitator")
  val personName: String,


  @NotBlank(message = "pduName must not be null")
  @get:JsonProperty("pduName", required = true)
  @Schema(description = "The name of the PDU that the group will take place in")
  val pduName: String,

  @NotBlank(message = "pduCode must not be null")
  @get:JsonProperty("pduCode", required = true)
  @Schema(description = "The code of the PDU that the group will take place in")
  val pduCode: String,

  @NotBlank(message = "deliveryLocationName must not be null")
  @get:JsonProperty("deliveryLocationName", required = true)
  @Schema(description = "The name of the location that the group will be delivered at")
  val deliveryLocationName: String,

  @NotBlank(message = "deliveryLocationCode must not be null")
  @get:JsonProperty("deliveryLocationCode", required = true)
  @Schema(description = "The code of the location that the group will be delivered at")
  val deliveryLocationCode: String,

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
    probationDeliveryUnitCode = pduCode,
    probationDeliveryUnitName = pduName,
    deliveryLocationCode = deliveryLocationCode,
    deliveryLocationName = deliveryLocationName,
  )
}
