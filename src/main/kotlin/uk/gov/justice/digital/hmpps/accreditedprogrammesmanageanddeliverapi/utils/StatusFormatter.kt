package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

object StatusFormatter {

  private val STATUS_ORDER = listOf(
    "Awaiting assessment",
    "Awaiting allocation",
    "Scheduled",
    "On programme",
    "Suitable but not ready",
    "Deferred",
    "Deprioritised",
    "Breach",
    "Recall",
    "Return to court",
    "Programme complete",
    "Withdrawn",
  )

  private val STATUS_ORDER_INDEX: Map<String, Int> = STATUS_ORDER
    .mapIndexed { index, status -> status to index }
    .toMap()

  fun sortStatuses(statuses: List<String>): List<String> = statuses.sortedBy { STATUS_ORDER_INDEX.getOrDefault(it, Int.MAX_VALUE) }

  fun formatStatus(description: String): String = if (description == "Breach (non-attendance)") "Breach" else description

  fun unformatStatus(description: String?): String? = if (description == "Breach") "Breach (non-attendance)" else description
}
