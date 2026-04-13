package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupSex
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum

enum class ProgrammeGroupSex(val label: String, val createAndEditDisplayOption: String) {
  MALE("Male", "Male"),
  FEMALE("Female", "Female"),
  MIXED("Mixed", "Mixed"),
  ;

  companion object {
    fun from(sex: ProgrammeGroupSexEnum): ProgrammeGroupSex = when (sex) {
      ProgrammeGroupSexEnum.MALE -> MALE
      ProgrammeGroupSexEnum.FEMALE -> FEMALE
      ProgrammeGroupSexEnum.MIXED -> MIXED
    }

    fun fromString(label: String): ProgrammeGroupSex = entries.find { it.label == label } ?: throw IllegalArgumentException("Unknown sex: $label")

    fun toRadioOptions(selectedSex: ProgrammeGroupSex): List<EditGroupSex.RadioOptions> = ProgrammeGroupSex.entries.map { sex ->
      EditGroupSex.RadioOptions(
        text = sex.createAndEditDisplayOption,
        value = sex.name,
        selected = sex == selectedSex,
      )
    }
  }
}
