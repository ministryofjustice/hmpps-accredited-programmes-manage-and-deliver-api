package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSex
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity

data class CreateGroup(
  @NotBlank(message = "groupCode must not be null")
  @get:JsonProperty("groupCode", required = true)
  var groupCode: String,
  @NotNull(message = "cohort must not be null")
  @get:JsonProperty("cohort", required = true)
  var cohort: ProgrammeGroupCohort,
  @NotNull(message = "sex must not be null")
  @get:JsonProperty("sex", required = true)
  var sex: ProgrammeGroupSex,
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
