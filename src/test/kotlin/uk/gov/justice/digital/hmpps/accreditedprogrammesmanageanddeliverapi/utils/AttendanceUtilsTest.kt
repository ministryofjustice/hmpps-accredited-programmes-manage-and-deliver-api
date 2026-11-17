package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode

class AttendanceUtilsTest {

  @Nested
  inner class GetAttendanceTextFromOutcome {

    @Test
    fun `returns 'Did not attend' when attendance outcome code is UAAB and description is not null`() {
      // When
      val result = AttendanceUtils.getAttendanceTextFromOutcome(
        attendanceOutcomeCode = SessionAttendanceNDeliusCode.UAAB,
        attendanceOutcomeDescription = "Unacceptable Absence",
      )

      // Then
      assertThat(result).isEqualTo("Did not attend")
    }

    @Test
    fun `returns 'Did not attend' when attendance outcome code is UAAB and description is null`() {
      // When
      val result = AttendanceUtils.getAttendanceTextFromOutcome(
        attendanceOutcomeCode = SessionAttendanceNDeliusCode.UAAB,
        attendanceOutcomeDescription = null,
      )

      // Then
      assertThat(result).isEqualTo("Did not attend")
    }

    @Test
    fun `returns 'To be confirmed' when attendance outcome code is null and description is null`() {
      // When
      val result = AttendanceUtils.getAttendanceTextFromOutcome(
        attendanceOutcomeCode = null,
        attendanceOutcomeDescription = null,
      )

      // Then
      assertThat(result).isEqualTo("To be confirmed")
    }

    @Test
    fun `returns 'To be confirmed' when attendance outcome code is null and description is not null`() {
      // When
      val result = AttendanceUtils.getAttendanceTextFromOutcome(
        attendanceOutcomeCode = null,
        attendanceOutcomeDescription = "Some description",
      )

      // Then
      assertThat(result).isEqualTo("To be confirmed")
    }

    @ParameterizedTest
    @EnumSource(
      value = SessionAttendanceNDeliusCode::class,
      names = ["UAAB"],
      mode = EnumSource.Mode.EXCLUDE,
    )
    fun `returns outcome description when attendance outcome code is other non-null code`(code: SessionAttendanceNDeliusCode) {
      // Given
      val description = "Attended - Complied"

      // When
      val result = AttendanceUtils.getAttendanceTextFromOutcome(
        attendanceOutcomeCode = code,
        attendanceOutcomeDescription = description,
      )

      // Then
      assertThat(result).isEqualTo(description)
    }

    @Test
    fun `throws NullPointerException when attendance outcome code is not UAAB or null and description is null`() {
      // When/Then
      assertThatThrownBy {
        AttendanceUtils.getAttendanceTextFromOutcome(
          attendanceOutcomeCode = SessionAttendanceNDeliusCode.ATTC,
          attendanceOutcomeDescription = null,
        )
      }.isInstanceOf(NullPointerException::class.java)
    }
  }
}
