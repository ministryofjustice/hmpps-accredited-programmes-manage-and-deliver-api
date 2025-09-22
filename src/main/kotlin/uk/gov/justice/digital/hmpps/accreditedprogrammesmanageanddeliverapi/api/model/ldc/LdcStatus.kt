package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc

enum class LdcStatus(val value: Boolean, val displayText: String) {
  HAS_LDC(value = true, displayText = "May need an LDC-adapted programme(Building Choices Plus)"),
  NO_LDC(value = false, displayText = "Does not need an LDC-adapted programme"),
  ;

  companion object {
    fun fromBoolean(value: Boolean?): LdcStatus? = entries.find { it.value == value }
    fun getDisplayText(value: Boolean?): String = fromBoolean(value)?.displayText ?: NO_LDC.displayText
  }
}
