package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferralStatusUtilityTest {

  @Test
  fun `formatStatus returns Breach for Breach (non-attendance)`() {
    assertThat(ReferralStatusUtils.formatStatus("Breach (non-attendance)")).isEqualTo("Breach")
  }

  @Test
  fun `formatStatus returns description unchanged for all other statuses`() {
    assertThat(ReferralStatusUtils.formatStatus("Awaiting allocation")).isEqualTo("Awaiting allocation")
    assertThat(ReferralStatusUtils.formatStatus("On programme")).isEqualTo("On programme")
    assertThat(ReferralStatusUtils.formatStatus("Scheduled")).isEqualTo("Scheduled")
    assertThat(ReferralStatusUtils.formatStatus("Deferred")).isEqualTo("Deferred")
    assertThat(ReferralStatusUtils.formatStatus("Deprioritised")).isEqualTo("Deprioritised")
    assertThat(ReferralStatusUtils.formatStatus("Suitable but not ready")).isEqualTo("Suitable but not ready")
    assertThat(ReferralStatusUtils.formatStatus("Recall")).isEqualTo("Recall")
    assertThat(ReferralStatusUtils.formatStatus("Return to court")).isEqualTo("Return to court")
    assertThat(ReferralStatusUtils.formatStatus("Programme complete")).isEqualTo("Programme complete")
    assertThat(ReferralStatusUtils.formatStatus("Withdrawn")).isEqualTo("Withdrawn")
  }

  @Test
  fun `unformatStatus returns Breach (non-attendance) for Breach`() {
    assertThat(ReferralStatusUtils.unformatStatus("Breach")).isEqualTo("Breach (non-attendance)")
  }

  @Test
  fun `unformatStatus returns description unchanged for all other statuses`() {
    assertThat(ReferralStatusUtils.unformatStatus("Awaiting allocation")).isEqualTo("Awaiting allocation")
    assertThat(ReferralStatusUtils.unformatStatus("On programme")).isEqualTo("On programme")
  }

  @Test
  fun `unformatStatus returns null when input is null`() {
    assertThat(ReferralStatusUtils.unformatStatus(null)).isNull()
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
    assertThat(ReferralStatusUtils.sortStatuses(input)).isEqualTo(expected)
  }
}
