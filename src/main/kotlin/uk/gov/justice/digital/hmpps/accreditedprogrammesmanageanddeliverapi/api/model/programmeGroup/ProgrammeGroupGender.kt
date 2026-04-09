package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceGender
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupGender

enum class ProgrammeGroupGender(val label: String, val createAndEditDisplayOption: String) {
  MALE("Male", "Male"),
  FEMALE("Female", "Female"),
  MIXED("Mixed", "Mixed"),
  ;

  companion object {
    fun from(gender: OffenceGender): ProgrammeGroupGender = when (gender) {
      OffenceGender.MALE -> MALE
      OffenceGender.FEMALE -> FEMALE
      OffenceGender.MIXED -> MIXED
      else -> throw IllegalArgumentException("Invalid combination")
    }

    fun toOffenceType(gender: ProgrammeGroupGender): Pair<OffenceGender, Boolean> = when (gender) {
      MALE -> OffenceGender.MALE to false
      FEMALE -> OffenceGender.FEMALE to true
      MIXED -> OffenceGender.MIXED to false
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
