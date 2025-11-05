package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type

enum class ProgrammeGroupSexEnum(val label: String) {
  MALE("Male"),
  FEMALE("Female"),
  MIXED("Mixed"),
  ;

  companion object {
    fun fromLabel(label: String): ProgrammeGroupSexEnum = entries.find { it.label == label }
      ?: throw IllegalArgumentException("Unknown Programme Group Sex: $label")
  }
}
