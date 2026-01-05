package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort

enum class ProgrammeGroupCohort(val label: String) {
  GENERAL("General offence"),
  GENERAL_LDC("General offence - LDC"),
  SEXUAL("Sexual offence"),
  SEXUAL_LDC("Sexual offence - LDC"),
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
  }
}
