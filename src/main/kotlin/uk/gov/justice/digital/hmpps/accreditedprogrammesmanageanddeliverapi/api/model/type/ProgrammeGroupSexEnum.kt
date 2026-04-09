package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupGender

enum class ProgrammeGroupSexEnum(val label: String) {
  MALE("Male"),
  FEMALE("Female"),
  MIXED("Mixed"),
  ;

  companion object {
    fun fromLabel(label: String): ProgrammeGroupSexEnum = entries.find { it.label == label }
      ?: throw IllegalArgumentException("Unknown Programme Group Sex: $label")

    fun toRadioOptions(selected: ProgrammeGroupSexEnum): List<EditGroupGender.RadioOptions> = entries.map { sex ->
      EditGroupGender.RadioOptions(
        text = sex.label,
        value = sex.name,
        selected = sex == selected,
      )
    }
  }
}
