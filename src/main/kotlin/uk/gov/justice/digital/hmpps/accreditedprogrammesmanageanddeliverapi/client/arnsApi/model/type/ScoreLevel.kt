package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type

enum class ScoreLevel(val type: String) {
  LOW("Low"),
  MEDIUM("Medium"),
  HIGH("High"),
  VERY_HIGH("Very High"),
  NOT_APPLICABLE("Not Applicable"),
  ;

  companion object {
    fun findByType(type: String?): ScoreLevel? = entries.firstOrNull { value -> value.type == type }
  }
}
