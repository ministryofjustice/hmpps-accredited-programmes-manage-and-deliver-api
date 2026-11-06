package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

enum class ReferralStatusType(val displayName: String, val description: String) {
  AWAITING_ASSESSMENT(
    "Awaiting assessment",
    "The person has a requirement to complete the programme. Their suitability and readiness will be assessed by the programme team.",
  ),
  AWAITING_ALLOCATION("Awaiting allocation", "The person is ready to be allocated to a programme group."),
  SUITABLE_BUT_NOT_READY(
    "Suitable but not ready",
    "The person is suitable for the programme but not ready to start it or continue with it.",
  ),
  DEPRIORITISED("Deprioritised", "The programme for the person has been de-prioritised"),
  RECALL("Recall", "The person has been recalled."),
  RETURN_TO_COURT(
    "Return to court",
    "The person is not suitable for the programme or cannot continue with it. The case will be returned to court.",
  ),
  SCHEDULED("Scheduled", "The person has been allocated to a group."),
  ON_HOLD("On hold", "The referral is paused, for example during a transfer or after the court has extended an order."),
  ON_PROGRAMME("On programme", "The person has started the programme. They have attended a pre-group one-to-one."),
  PROGRAMME_COMPLETE(
    "Programme complete",
    "The person has completed the programme and their post-programme review has taken place.",
  ),
  BREACH("Breach", "The person has breached their conditions through non-attendance of the programme."),
  DEFERRED("Deferred", "The programme for the person has been deferred"),
  WITHDRAWN("Withdrawn", "The referral has been closed because the person cannot complete the programme."),
  ;

  companion object {
    private val byDisplayName = entries.associateBy { it.displayName.lowercase() }

    fun fromDisplayName(displayName: String): ReferralStatusType = byDisplayName[displayName.lowercase()]
      ?: throw IllegalArgumentException("Unknown referral status display name: $displayName")
  }
}
