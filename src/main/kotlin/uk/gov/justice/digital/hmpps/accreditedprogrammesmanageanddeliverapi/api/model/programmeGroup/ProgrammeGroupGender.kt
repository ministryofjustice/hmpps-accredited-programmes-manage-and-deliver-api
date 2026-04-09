package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupGender
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum

enum class ProgrammeGroupGender(val label: String, val createAndEditDisplayOption: String) {
  MALE("Male", "Male"),
  FEMALE("Female", "Female"),
  MIXED("Mixed", "Mixed"),
  ;

  companion object {
    fun from(value: ProgrammeGroupSexEnum): ProgrammeGroupGender = when (value) {
      ProgrammeGroupSexEnum.MALE -> MALE
      ProgrammeGroupSexEnum.FEMALE -> FEMALE
      ProgrammeGroupSexEnum.MIXED -> MIXED
    }

    fun fromString(label: String): ProgrammeGroupGender = entries.find { it.label == label } ?: throw IllegalArgumentException("Unknown gender : $label")

    fun toRadioOptions(selectedGender: ProgrammeGroupGender): List<EditGroupGender.RadioOptions> = ProgrammeGroupGender.entries.map { gender ->
      EditGroupGender.RadioOptions(
        text = gender.createAndEditDisplayOption,
        value = gender.name,
        selected = gender == selectedGender,
      )
    }
  }
}
