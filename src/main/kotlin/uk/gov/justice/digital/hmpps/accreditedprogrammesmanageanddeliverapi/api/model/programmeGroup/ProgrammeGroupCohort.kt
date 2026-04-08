package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.editGroup.EditGroupCohort

enum class ProgrammeGroupCohort(val label: String, val createAndEditDisplayOption: String) {
  GENERAL("General offence", "General offence"),
  GENERAL_LDC("General offence - LDC", "General offence, learning disabilities and challenges (LDC)"),
  SEXUAL("Sexual offence", "Sexual offence"),
  SEXUAL_LDC("Sexual offence - LDC", "Sexual offence, learning disabilities and challenges (LDC)"),
  ;

  companion object {
    fun from(cohort: OffenceCohort, hasLdc: Boolean): ProgrammeGroupCohort = when {
      cohort == OffenceCohort.GENERAL_OFFENCE && hasLdc -> GENERAL_LDC
      cohort == OffenceCohort.GENERAL_OFFENCE && !hasLdc -> GENERAL
      cohort == OffenceCohort.SEXUAL_OFFENCE && hasLdc -> SEXUAL_LDC
      cohort == OffenceCohort.SEXUAL_OFFENCE && !hasLdc -> SEXUAL
      else -> throw IllegalArgumentException("Invalid combination")
    }

    fun toOffenceTypeAndLdc(cohort: ProgrammeGroupCohort): Pair<OffenceCohort, Boolean> = when (cohort) {
      GENERAL -> OffenceCohort.GENERAL_OFFENCE to false
      GENERAL_LDC -> OffenceCohort.GENERAL_OFFENCE to true
      SEXUAL -> OffenceCohort.SEXUAL_OFFENCE to false
      SEXUAL_LDC -> OffenceCohort.SEXUAL_OFFENCE to true
    }

    fun fromString(label: String): ProgrammeGroupCohort = entries.find { it.label == label } ?: throw IllegalArgumentException("Unknown cohort: $label")

    fun toRadioOptions(selectedCohort: ProgrammeGroupCohort): List<EditGroupCohort.RadioOptions> = ProgrammeGroupCohort.entries.map { cohort ->
      EditGroupCohort.RadioOptions(
        text = cohort.createAndEditDisplayOption,
        value = cohort.name,
        selected = cohort == selectedCohort,
      )
    }
  }
}
