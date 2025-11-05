package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity

data class CreateGroup(
  @NotBlank(message = "groupCode must not be null")
  @get:JsonProperty("groupCode", required = true)
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
)

fun CreateGroup.toEntity(): ProgrammeGroupEntity {
  val (cohort, isLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
  return ProgrammeGroupEntity(
    code = groupCode,
    cohort = cohort,
    sex = sex,
    isLdc = isLdc,
  )
}
