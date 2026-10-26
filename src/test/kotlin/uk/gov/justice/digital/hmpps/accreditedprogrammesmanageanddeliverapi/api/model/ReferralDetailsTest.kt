package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralDetailsFactory

class ReferralDetailsTest {

  @Test
  fun `personName returns joined names when forename and surname are present`() {
    // Given
    val referralDetails = ReferralDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames("William")
      .produce()

    // When
    val result = referralDetails.personName

    // Then
    assertThat(result).isEqualTo("John William Smith")
  }

  @Test
  fun `personName returns joined names without middle name when middle name is null`() {
    // Given
    val referralDetails = ReferralDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames(null)
      .produce()

    // When
    val result = referralDetails.personName

    // Then
    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `personName returns joined names without middle name when middle name is blank`() {
    // Given
    val referralDetails = ReferralDetailsFactory()
      .withPersonForename("John")
      .withPersonSurname("Smith")
      .withPersonMiddleNames(" ")
      .produce()

    // When
    val result = referralDetails.personName

    // Then
    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `personName returns original name when forename is null`() {
    // Given
    val referralDetails = ReferralDetailsFactory()
      .withPersonName("Original Name")
      .withPersonForename(null)
      .withPersonSurname("Smith")
      .withPersonMiddleNames("William")
      .produce()

    // When
    val result = referralDetails.personName

    // Then
    assertThat(result).isEqualTo("Original Name")
  }

  @Test
  fun `personName returns original name when surname is null`() {
    // Given
    val referralDetails = ReferralDetailsFactory()
      .withPersonName("Original Name")
      .withPersonForename("John")
      .withPersonSurname(null)
      .withPersonMiddleNames("William")
      .produce()

    // When
    val result = referralDetails.personName

    // Then
    assertThat(result).isEqualTo("Original Name")
  }
}
