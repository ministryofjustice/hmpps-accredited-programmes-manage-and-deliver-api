package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

class FormatTimeForUiDisplayTest {

  @Test
  fun `formatTimeForUiDisplay returns correct format for various times`() {
    // Midnight
    assertThat(formatTimeForUiDisplay(LocalTime.of(0, 0))).isEqualTo("midnight")
    assertThat(formatTimeForUiDisplay(LocalTime.of(0, 30))).isEqualTo("12:30am")
    assertThat(formatTimeForUiDisplay(LocalTime.of(0, 5))).isEqualTo("12:05am")

    // Morning times
    assertThat(formatTimeForUiDisplay(LocalTime.of(1, 0))).isEqualTo("1am")
    assertThat(formatTimeForUiDisplay(LocalTime.of(9, 5))).isEqualTo("9:05am")
    assertThat(formatTimeForUiDisplay(LocalTime.of(10, 0))).isEqualTo("10am")
    assertThat(formatTimeForUiDisplay(LocalTime.of(11, 59))).isEqualTo("11:59am")

    // Midday
    assertThat(formatTimeForUiDisplay(LocalTime.of(12, 0))).isEqualTo("midday")
    assertThat(formatTimeForUiDisplay(LocalTime.of(12, 30))).isEqualTo("12:30pm")
    assertThat(formatTimeForUiDisplay(LocalTime.of(12, 1))).isEqualTo("12:01pm")

    // Afternoon/Evening times
    assertThat(formatTimeForUiDisplay(LocalTime.of(13, 0))).isEqualTo("1pm")
    assertThat(formatTimeForUiDisplay(LocalTime.of(15, 30))).isEqualTo("3:30pm")
    assertThat(formatTimeForUiDisplay(LocalTime.of(18, 45))).isEqualTo("6:45pm")
    assertThat(formatTimeForUiDisplay(LocalTime.of(23, 59))).isEqualTo("11:59pm")
  }
}
