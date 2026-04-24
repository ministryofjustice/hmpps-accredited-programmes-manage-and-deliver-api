package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import java.time.LocalDate
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Information identifying the group.")
data class GroupDetailsResponse(
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
    example = "West Midlands",
    required = true,
    description = "The region name the group belongs to.",
  )
  @get:JsonProperty("regionName", required = true)
  val regionName: String,

  @Schema(
    example = "Thursday 23 April 2026",
    description = "The actual start date initiated by the facilitator",
  )
  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  @get:JsonProperty("startDate")
  val startDate: LocalDate? = null,

  @Schema(
    example = "County Durham and Darlington",
    description = "The Probation Delivery Unit (PDU) name.",
  )
  @get:JsonProperty("pduName", required = true)
  val pduName: String,

  @Schema(
    example = "N02CLE",
    description = "The Probation Delivery Unit (PDU) code.",
  )
  @get:JsonProperty("pduCode", required = true)
  val pduCode: String,

  @Schema(
    example = "County Durham Probation Office",
    description = "The location description where the group programme will be delivered.",
  )
  @get:JsonProperty("deliveryLocation", required = true)
  val deliveryLocation: String,

  @Schema(
    example = "DTVBOR1",
    description = "The location code for where the group programme will be delivered.",
  )
  @get:JsonProperty("deliveryLocationCode", required = true)
  val deliveryLocationCode: String,

  @Schema(
    enumAsRef = true,
    description = "Cohort for the Programme Group.",
    implementation = ProgrammeGroupCohort::class,
  )
  @get:JsonProperty("cohort", required = true)
  val cohort: String,

  @Schema(
    enumAsRef = true,
    description = "Sex that the group is being run for.",
    implementation = ProgrammeGroupSexEnum::class,
  )
  @get:JsonProperty("sex", required = true)
  val sex: String,

  @Schema(
    example = "[Mondays, 11am to 1:30pm, Thursdays, 11am to 1:30pm]",
    description = "The days and times that group sessions will be delivered.",
  )
  @get:JsonProperty("daysAndTimes", required = true)
  val daysAndTimes: List<String>,

  @Schema(
    example = "9",
    description = "The number of referrals currently allocated to this group.",
  )
  @get:JsonProperty("currentlyAllocatedNumber", required = true)
  val currentlyAllocatedNumber: Int,

  @Schema(
    example = "{\"personCode\": \"CP001\", \"personName\": \"Chloe Pascal\", \"teamName\": \"Management Team\", \"teamCode\": \"TEAM001\"}",
    description = "The treatment manager for this group.",
  )
  @get:JsonProperty("treatmentManager", required = true)
  val treatmentManager: UserTeamMember,

  @Schema(
    example = "[{\"personCode\": \"HS001\", \"personName\": \"Harpreet Singh\", \"teamName\": \"Facilitator Team\", \"teamCode\": \"TEAM002\"}, {\"personCode\": \"TB001\", \"personName\": \"Tom Bassett\", \"teamName\": \"Facilitator Team\", \"teamCode\": \"TEAM002\"}]",
    description = "The list of facilitators for this group.",
  )
  @get:JsonProperty("facilitators", required = true)
  val facilitators: List<UserTeamMember>,

  @Schema(
    example = "[{\"personCode\": \"TS001\", \"personName\": \"Tom Saunders\", \"teamName\": \"Cover Team\", \"teamCode\": \"TEAM003\"}]",
    description = "The list of coverFacilitators for this group.",
  )
  @get:JsonProperty("coverFacilitators")
  val coverFacilitators: List<UserTeamMember>? = null,
) {
  companion object {
    fun from(
      programmeGroup: ProgrammeGroupEntity,
      daysAndTimes: List<String>,
      earliestPreGroupSessionDate: LocalDate?,
    ): GroupDetailsResponse = GroupDetailsResponse(
      id = programmeGroup.id!!,
      code = programmeGroup.code,
      regionName = programmeGroup.regionName,
      startDate = earliestPreGroupSessionDate,
      pduName = programmeGroup.probationDeliveryUnitName,
      pduCode = programmeGroup.probationDeliveryUnitCode,
      deliveryLocation = programmeGroup.deliveryLocationName,
      deliveryLocationCode = programmeGroup.deliveryLocationCode,
      cohort = ProgrammeGroupCohort.from(programmeGroup.cohort, programmeGroup.isLdc).label,
      sex = programmeGroup.sex.label,
      daysAndTimes = daysAndTimes,
      currentlyAllocatedNumber = programmeGroup.programmeGroupMemberships.count { it.deletedAt == null },
      treatmentManager = programmeGroup.treatmentManager!!.let {
        UserTeamMember(personCode = it.ndeliusPersonCode, personName = it.personName, teamName = it.ndeliusTeamName, teamCode = it.ndeliusTeamCode)
      },
      facilitators = programmeGroup.groupFacilitators
        .filter { it.facilitatorType == FacilitatorType.REGULAR_FACILITATOR }
        .map { UserTeamMember(personCode = it.facilitatorCode, personName = it.facilitatorName, teamName = it.teamName, teamCode = it.teamCode) },
      coverFacilitators = programmeGroup.groupFacilitators
        .filter { it.facilitatorType == FacilitatorType.COVER_FACILITATOR }
        .map { UserTeamMember(personCode = it.facilitatorCode, personName = it.facilitatorName, teamName = it.teamName, teamCode = it.teamCode) },
    )
  }
}
