package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

object StatusFormatter {
  fun formatStatus(description: String): String =
    if (description == "Breach (non-attendance)") "Breach" else description

  fun unformatStatus(description: String?): String? =
    if (description == "Breach") "Breach (non-attendance)" else description
}

