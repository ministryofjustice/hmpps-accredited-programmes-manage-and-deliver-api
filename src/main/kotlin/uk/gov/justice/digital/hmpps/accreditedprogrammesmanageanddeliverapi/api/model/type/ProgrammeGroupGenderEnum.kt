package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupGender

enum class ProgrammeGroupGenderEnum(val label: String) {
  MALE("Male"),
  FEMALE("Female"),
  MIXED("Mixed"),
  ;

  companion object {
    fun fromLabel(label: String): ProgrammeGroupGenderEnum = entries.find { it.label == label }
      ?: throw IllegalArgumentException("Unknown Programme Group Gender: $label")

    fun toRadioOptions(selected: ProgrammeGroupGenderEnum): List<EditGroupGender.RadioOptions> = entries.map { gender ->
      EditGroupGender.RadioOptions(
        text = gender.label,
        value = gender.name,
        selected = gender == selected,
      )
    }
  }
}
