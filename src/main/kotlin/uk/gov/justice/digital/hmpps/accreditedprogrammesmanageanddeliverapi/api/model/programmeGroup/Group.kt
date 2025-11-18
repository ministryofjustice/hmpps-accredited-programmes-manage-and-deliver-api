package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Information identifying the group.")
data class Group(
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
    example = "02-12-2025",
    description = "The earliest possible start date",
  )
  @get:JsonProperty("earliestStartDate")
  val earliestStartDate: LocalDate? = null,

  @Schema(
    example = "23-10-2025",
    description = "The actual start date initiated by the facilitator",
  )
  @get:JsonProperty("startDate")
  val startDate: LocalDate? = null,

  @Schema(
    example = "County Durham and Darlington",
    description = "The Probation Delivery Unit (PDU) name.",
  )
  @get:JsonProperty("pduName")
  val pduName: String? = null,

  @Schema(
    example = "County Durham Probation Office",
    description = "The location Where the group programme will be delivered.",
  )
  @get:JsonProperty("deliveryLocation")
  val deliveryLocation: String? = null,

  @Schema(
    enumAsRef = true,
    description = "Cohort for the Programme Group.",
    implementation = ProgrammeGroupCohort::class,
  )
  @get:JsonProperty("cohort")
  val cohort: ProgrammeGroupCohort? = null,

  @Schema(
    enumAsRef = true,
    description = "Sex that the group is being run for.",
    implementation = ProgrammeGroupSexEnum::class,
  )
  @get:JsonProperty("sex")
  val sex: ProgrammeGroupSexEnum? = null,

  @Schema(
    example = "10",
    description = "The total group capacity.",
  )
  @get:JsonProperty("capacity")
  val capacity: Int? = null,

  @Schema(
    example = "8",
    description = "The number of people currently allocated to the group.",
  )
  @get:JsonProperty("allocated")
  val allocated: Int? = null,
)

fun ProgrammeGroupEntity.toApi(): Group = Group(
  code = this.code,
  regionName = this.regionName,
  earliestStartDate = this.earliestPossibleStartDate,
  startDate = startedAtDate,
  pduName = this.probationDeliveryUnit,
  deliveryLocation = this.deliveryLocation,
  cohort = this.let { entity ->
    ProgrammeGroupCohort.from(entity.cohort, entity.isLdc)
  },
  sex = this.sex,
  capacity = null,
  allocated = null,
)
