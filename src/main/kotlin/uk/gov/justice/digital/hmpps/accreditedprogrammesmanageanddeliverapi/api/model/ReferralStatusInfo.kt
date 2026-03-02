package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom

data class ReferralStatusInfo(
  val newStatus: Status,
  val sourcedFromEntityType: ReferralEntitySourcedFrom,
  val sourcedFromEntityId: Long,
  val notes: String?,
) {
  enum class Status(
    // TODO is this needed?
    val contactTypeCode: String,
    val displayName: String,
    val description: String,
  ) {
    AWAITING_ALLOCATION(
      "EIBB",
      "Awaiting allocation",
      "The person is ready to be allocated to a programme group.",
    ),
    AWAITING_ASSESSMENT(
      "EIBA",
      "Awaiting assessment",
      "The person has a requirement to complete the programme. Their suitability and readiness will be assessed by the programme team.",
    ),
    BREACH(
      "EIBI",
      "Breach (non-attendance)",
      "The person has breached their conditions through non-attendance of the programme.",
    ),
    DEPRIORITISED(
      "EIBG",
      "Deprioritised",
      "The person is suitable but does not currently meet the prioritisation criteria. The referral will be paused.",
    ),
    DEFERRED(
      "EIBL",
      "Deferred",
      "The court has agreed that the programme requirement should be deferred until the person can continue.",
    ),
    ON_PROGRAMME(
      "EIBE",
      "On programme",
      "The person has started the programme. They have attended a pre-group one-to-one.",
    ),
    PROGRAMME_COMPLETE(
      "EIBF",
      "Programme complete",
      "The person has completed the programme and their post-programme review has taken place.",
    ),
    RECALL(
      "EIBJ",
      "Recall",
      "The person has been recalled.",
    ),
    RETURN_TO_COURT(
      "EIBH",
      "Return to court",
      "The person is not suitable for the programme or cannot continue with it. The case will be returned to court.",
    ),
    SCHEDULED(
      "EIBD",
      "Scheduled",
      "The person has been allocated to a group.",
    ),
    SUITABLE_BUT_NOT_READY(
      "EIBC",
      "Suitable but not ready",
      "The person is suitable for the programme but not ready to start it or continue with it.",
    ),
    WITHDRAWN(
      "EIBK",
      "Withdrawn",
      "The referral has been closed because the person cannot complete the programme.",
    ),
    ;

    companion object {
      private val byDisplayName =
        entries.associateBy { it.displayName.lowercase() }

      fun fromDisplayName(displayName: String): Status = byDisplayName[displayName.lowercase()]
        ?: throw IllegalArgumentException(
          "Unknown referral status description: $displayName",
        )
    }
  }
}
