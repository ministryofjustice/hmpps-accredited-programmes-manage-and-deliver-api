package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [toSurname]. Rows mirror §5 of
 * `docs/APG-2580-sar-recorded-by-surname-only.md`.
 */
class NameFormattingTest {

  @Test
  fun `returns last token for a standard forename plus surname`() {
    assertThat(toSurname("Joe Bloggs")).isEqualTo("Bloggs")
  }

  @Test
  fun `preserves casing`() {
    assertThat(toSurname("joe bloggs")).isEqualTo("bloggs")
  }

  @Test
  fun `treats hyphenated forename as a single token so drops off`() {
    assertThat(toSurname("Jean-Paul Sartre")).isEqualTo("Sartre")
  }

  @Test
  fun `preserves apostrophe surnames`() {
    assertThat(toSurname("Anne-Marie O'Brien")).isEqualTo("O'Brien")
  }

  @Test
  fun `preserves Dutch compound surname van der Berg`() {
    assertThat(toSurname("John van der Berg")).isEqualTo("van der Berg")
  }

  @Test
  fun `preserves Spanish compound surname de la Cruz`() {
    assertThat(toSurname("Maria de la Cruz")).isEqualTo("de la Cruz")
  }

  @Test
  fun `preserves German particle von Beethoven`() {
    assertThat(toSurname("Ludwig von Beethoven")).isEqualTo("von Beethoven")
  }

  @Test
  fun `drops uppercase title as it is not a particle`() {
    assertThat(toSurname("Dr Sarah Hughes")).isEqualTo("Hughes")
  }

  @Test
  fun `returns single-token input unchanged`() {
    assertThat(toSurname("Bloggs")).isEqualTo("Bloggs")
  }

  @Test
  fun `tolerates leading trailing and repeated whitespace`() {
    assertThat(toSurname("  Joe   Bloggs  ")).isEqualTo("Bloggs")
  }

  @Test
  fun `null input returns null`() {
    assertThat(toSurname(null)).isNull()
  }

  @Test
  fun `empty string returns null`() {
    assertThat(toSurname("")).isNull()
  }

  @Test
  fun `whitespace-only input returns null`() {
    assertThat(toSurname("   ")).isNull()
  }

  @Test
  fun `all-caps particles still match because comparison is case-insensitive`() {
    assertThat(toSurname("VAN DER BERG")).isEqualTo("VAN DER BERG")
  }

  @Test
  fun `preserves hyphenated surname when no particle is present`() {
    assertThat(toSurname("Anne Smith-Jones")).isEqualTo("Smith-Jones")
  }
}
