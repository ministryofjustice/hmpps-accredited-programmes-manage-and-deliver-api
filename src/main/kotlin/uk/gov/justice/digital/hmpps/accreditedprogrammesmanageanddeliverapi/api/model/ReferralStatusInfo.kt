package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom

@Schema(description = "Details of a referral status change event")
data class ReferralStatusInfo(
  @Schema(description = "The new status of the referral", required = true)
  val newStatus: Status,
  @Schema(description = "The type of entity from which this status change was sourced", required = true)
  val sourcedFromEntityType: ReferralEntitySourcedFrom,
  @Schema(description = "The ID of the entity from which this status change was sourced", required = true)
  val sourcedFromEntityId: Long,
  @Schema(description = "Optional notes associated with the status change")
  val notes: String?,
  @Schema(description = "A human-readable description of the status change", required = true)
  val description: String,
) {
  @Schema(description = "The status of a referral")
  enum class Status(
    @Schema(description = "The human-readable display name of the status")
    val displayName: String,
    @Schema(description = "A description of what the status means")
    val description: String,
  ) {
    @Schema(description = "The person is ready to be allocated to a programme group.")
    AWAITING_ALLOCATION("Awaiting allocation", "The person is ready to be allocated to a programme group."),

    @Schema(description = "The person has a requirement to complete the programme. Their suitability and readiness will be assessed by the programme team.")
    AWAITING_ASSESSMENT(
      "Awaiting assessment",
      "The person has a requirement to complete the programme. Their suitability and readiness will be assessed by the programme team.",
    ),

    @Schema(description = "The person has breached their conditions through non-attendance of the programme.")
    BREACH(
      "Breach (non-attendance)",
      "The person has breached their conditions through non-attendance of the programme.",
    ),

    @Schema(description = "The person is suitable but does not currently meet the prioritisation criteria. The referral will be paused.")
    DEPRIORITISED(
      "Deprioritised",
      "The person is suitable but does not currently meet the prioritisation criteria. The referral will be paused.",
    ),

    @Schema(description = "The court has agreed that the programme requirement should be deferred until the person can continue.")
    DEFERRED(
      "Deferred",
      "The court has agreed that the programme requirement should be deferred until the person can continue.",
    ),

    @Schema(description = "The person has started the programme. They have attended a pre-group one-to-one.")
    ON_PROGRAMME("On programme", "The person has started the programme. They have attended a pre-group one-to-one."),

    @Schema(description = "The person has completed the programme and their post-programme review has taken place.")
    PROGRAMME_COMPLETE(
      "Programme complete",
      "The person has completed the programme and their post-programme review has taken place.",
    ),

    @Schema(description = "The person has been recalled.")
    RECALL("Recall", "The person has been recalled."),

    @Schema(description = "The person is not suitable for the programme or cannot continue with it. The case will be returned to court.")
    RETURN_TO_COURT(
      "Return to court",
      "The person is not suitable for the programme or cannot continue with it. The case will be returned to court.",
    ),

    @Schema(description = "The person has been allocated to a group.")
    SCHEDULED("Scheduled", "The person has been allocated to a group."),

    @Schema(description = "The person is suitable for the programme but not ready to start it or continue with it.")
    SUITABLE_BUT_NOT_READY(
      "Suitable but not ready",
      "The person is suitable for the programme but not ready to start it or continue with it.",
    ),

    @Schema(description = "The referral has been closed because the person cannot complete the programme.")
    WITHDRAWN("Withdrawn", "The referral has been closed because the person cannot complete the programme."),
    ;

    companion object {
      private val byDisplayName = entries.associateBy { it.displayName.lowercase() }

      fun fromDisplayName(displayName: String): Status = byDisplayName[displayName.lowercase()]
        ?: throw IllegalArgumentException("Unknown referral status description: $displayName")
    }
  }
}
