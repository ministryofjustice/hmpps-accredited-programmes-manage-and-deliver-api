package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

enum class ReferralStatusType(val description: String) {
  AWAITING_ASSESSMENT("Awaiting assessment"),
  AWAITING_ALLOCATION("Awaiting allocation"),
  SUITABLE_BUT_NOT_READY("Suitable but not ready"),
  DEPRIORITISED("Deprioritised"),
  RECALL("Recall"),
  RETURN_TO_COURT("Return to court"),
  SCHEDULED("Scheduled"),
  ON_HOLD("On hold"),
  ON_PROGRAMME("On programme"),
  PROGRAMME_COMPLETE("Programme complete"),
  BREACH("Breach"),
  DEFERRED("Deferred"),
  WITHDRAWN("Withdrawn"),
}
