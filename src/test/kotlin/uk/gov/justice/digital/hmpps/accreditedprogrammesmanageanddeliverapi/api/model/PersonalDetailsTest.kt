package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PersonalDetailsFactory

class PersonalDetailsTest {

  @Test
  fun `name returns joined names when forename and surname are present`() {
    // Given
    val personalDetails = PersonalDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames("William")
      .produce()

    // When
    val result = personalDetails.name

    // Then
    assertThat(result).isEqualTo("John William Smith")
  }

  @Test
  fun `name returns joined names without middle name when middle name is null`() {
    // Given
    val personalDetails = PersonalDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames(null)
      .produce()

    // When
    val result = personalDetails.name

    // Then
    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `name returns joined names without middle name when middle name is blank`() {
    // Given
    val personalDetails = PersonalDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames(" ")
      .produce()

    // When
    val result = personalDetails.name

    assertThat(result).isEqualTo("John Smith")
  }
}
