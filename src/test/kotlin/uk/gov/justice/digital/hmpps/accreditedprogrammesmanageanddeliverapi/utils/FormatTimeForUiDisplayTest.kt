package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

class FormatTimeForUiDisplayTest {

  @Test
  fun `formatTimeOfSession returns correct format for various times`() {
    // Midnight
    assertThat(formatTimeOfSession(LocalTime.of(0, 0), LocalTime.of(1, 0))).isEqualTo("midnight to 1am")
    assertThat(formatTimeOfSession(LocalTime.of(0, 30), LocalTime.of(1, 0))).isEqualTo("12:30am to 1am")
    assertThat(formatTimeOfSession(LocalTime.of(0, 5), LocalTime.of(1, 0))).isEqualTo("12:05am to 1am")

    // Morning times
    assertThat(formatTimeOfSession(LocalTime.of(1, 0), LocalTime.of(2, 0))).isEqualTo("1am to 2am")
    assertThat(formatTimeOfSession(LocalTime.of(9, 5), LocalTime.of(10, 0))).isEqualTo("9:05am to 10am")
    assertThat(formatTimeOfSession(LocalTime.of(10, 0), LocalTime.of(11, 0))).isEqualTo("10am to 11am")
    assertThat(formatTimeOfSession(LocalTime.of(11, 59), LocalTime.of(12, 0))).isEqualTo("11:59am to midday")

    // Midday
    assertThat(formatTimeOfSession(LocalTime.of(12, 0), LocalTime.of(13, 0))).isEqualTo("midday to 1pm")
    assertThat(formatTimeOfSession(LocalTime.of(12, 30), LocalTime.of(13, 0))).isEqualTo("12:30pm to 1pm")
    assertThat(formatTimeOfSession(LocalTime.of(12, 1), LocalTime.of(13, 0))).isEqualTo("12:01pm to 1pm")

    // Afternoon/Evening times
    assertThat(formatTimeOfSession(LocalTime.of(13, 0), LocalTime.of(14, 0))).isEqualTo("1pm to 2pm")
    assertThat(formatTimeOfSession(LocalTime.of(15, 30), LocalTime.of(17, 0))).isEqualTo("3:30pm to 5pm")
    assertThat(formatTimeOfSession(LocalTime.of(18, 45), LocalTime.of(20, 0))).isEqualTo("6:45pm to 8pm")
    assertThat(formatTimeOfSession(LocalTime.of(23, 59), LocalTime.of(0, 0))).isEqualTo("11:59pm to midnight")
  }

  @Test
  fun `formatTimeOfSession can capitalise midday and midnight`() {
    assertThat(formatTimeOfSession(LocalTime.of(0, 0), LocalTime.of(1, 0), capitaliseMidday = true)).isEqualTo("Midnight to 1am")
    assertThat(formatTimeOfSession(LocalTime.of(11, 0), LocalTime.of(12, 0), capitaliseMidday = true)).isEqualTo("11am to Midday")

    // Other times should not be affected.
    assertThat(formatTimeOfSession(LocalTime.of(9, 30), LocalTime.of(11, 0), capitaliseMidday = true)).isEqualTo("9:30am to 11am")
    assertThat(formatTimeOfSession(LocalTime.of(13, 15), LocalTime.of(15, 0), capitaliseMidday = true)).isEqualTo("1:15pm to 3pm")
  }
}
