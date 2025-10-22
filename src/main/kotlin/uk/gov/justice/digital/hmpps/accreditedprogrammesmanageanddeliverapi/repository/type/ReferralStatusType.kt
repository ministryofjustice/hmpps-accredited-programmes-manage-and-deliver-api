package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type

enum class ReferralStatusType(val description: String) {
  AWAITING_ASSESSMENT("Awaiting assessment"),
  AWAITING_ALLOCATION("Awaiting allocation"),
  SUITABLE_BUT_NOT_READY("Suitable but not ready"),
  DEPRIORITISED("Deprioritised"),
  RECALL("Recall"),
  RETURN_TO_COURT("Return to court"),
  SCHEDULED("Scheduled"),
  ON_PROGRAMME("On programme"),
  PROGRAMME_COMPLETE("Programme complete"),
  BREACH_NON_ATTENDANCE("Breach (non-attendance)"),
  DEFERRED("Deferred"),
  WITHDRAWN("Withdrawn"),
}
