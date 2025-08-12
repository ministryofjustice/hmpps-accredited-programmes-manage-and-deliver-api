package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

enum class SaraRisk(private val score: Int, val description: String? = null) {
  NOT_APPLICABLE(0, "Not Applicable"),
  LOW(10, "Low"),
  MEDIUM(20, "Medium"),
  HIGH(30, "High"),
  VERY_HIGH(40, "Very High"),
  ;

  companion object {
    fun fromString(value: String?): SaraRisk = entries.find { it.description.equals(value?.replace('_', ' '), ignoreCase = true) }
      ?: NOT_APPLICABLE

    fun highestRisk(risk1: SaraRisk, risk2: SaraRisk): SaraRisk = if (risk1.score > risk2.score) risk1 else risk2
  }
}
