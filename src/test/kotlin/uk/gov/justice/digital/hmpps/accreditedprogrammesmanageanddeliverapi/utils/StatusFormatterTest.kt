package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatusFormatterTest {

  @Test
  fun `formatStatus returns Breach for Breach (non-attendance)`() {
    assertThat(StatusFormatter.formatStatus("Breach (non-attendance)")).isEqualTo("Breach")
  }

  @Test
  fun `formatStatus returns description unchanged for all other statuses`() {
    assertThat(StatusFormatter.formatStatus("Awaiting allocation")).isEqualTo("Awaiting allocation")
    assertThat(StatusFormatter.formatStatus("On programme")).isEqualTo("On programme")
    assertThat(StatusFormatter.formatStatus("Scheduled")).isEqualTo("Scheduled")
    assertThat(StatusFormatter.formatStatus("Deferred")).isEqualTo("Deferred")
    assertThat(StatusFormatter.formatStatus("Deprioritised")).isEqualTo("Deprioritised")
    assertThat(StatusFormatter.formatStatus("Suitable but not ready")).isEqualTo("Suitable but not ready")
    assertThat(StatusFormatter.formatStatus("Recall")).isEqualTo("Recall")
    assertThat(StatusFormatter.formatStatus("Return to court")).isEqualTo("Return to court")
    assertThat(StatusFormatter.formatStatus("Programme complete")).isEqualTo("Programme complete")
    assertThat(StatusFormatter.formatStatus("Withdrawn")).isEqualTo("Withdrawn")
  }

  @Test
  fun `unformatStatus returns Breach (non-attendance) for Breach`() {
    assertThat(StatusFormatter.unformatStatus("Breach")).isEqualTo("Breach (non-attendance)")
  }

  @Test
  fun `unformatStatus returns description unchanged for all other statuses`() {
    assertThat(StatusFormatter.unformatStatus("Awaiting allocation")).isEqualTo("Awaiting allocation")
    assertThat(StatusFormatter.unformatStatus("On programme")).isEqualTo("On programme")
  }

  @Test
  fun `unformatStatus returns null when input is null`() {
    assertThat(StatusFormatter.unformatStatus(null)).isNull()
  }

  @Test
  fun `sortStatuses returns all statuses in correct full order`() {
    val input = listOf(
      "Withdrawn", "Return to court", "Recall", "Breach", "Deprioritised",
      "Deferred", "Suitable but not ready", "On programme", "Scheduled",
      "Awaiting allocation", "Awaiting assessment", "Programme complete",
    )
    val expected = listOf(
      "Awaiting assessment", "Awaiting allocation", "Scheduled", "On programme",
      "Suitable but not ready", "Deferred", "Deprioritised", "Breach",
      "Recall", "Return to court", "Programme complete", "Withdrawn",
    )
    assertThat(StatusFormatter.sortStatuses(input)).isEqualTo(expected)
  }
}
