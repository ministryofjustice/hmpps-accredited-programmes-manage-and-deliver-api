package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Information identifying the group.")
data class Group(
  @Schema(
    example = "1ff57cea-352c-4a99-8f66-3e626aac3265",
    required = true,
    description = "A unique id identifying the programme group.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID?,

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
  @JsonFormat(pattern = "d MMM yyyy")
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
    description = "The location description where the group programme will be delivered.",
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
)

fun ProgrammeGroupEntity.toApi(): Group = Group(
  id = this.id,
  code = this.code,
  regionName = this.regionName,
  earliestStartDate = this.earliestPossibleStartDate,
  startDate = startedAtDate,
  pduName = this.probationDeliveryUnitName,
  deliveryLocation = this.deliveryLocationName,
  cohort = this.let { entity ->
    ProgrammeGroupCohort.from(entity.cohort, entity.isLdc)
  },
  sex = this.sex,
)
