package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType

class ReferralStatusConverterTest {

  private val converter = ReferralStatusConverter()

  @Test
  fun `convertToDatabaseColumn should return null when referralStatusType is null`() {
    // When
    val result = converter.convertToDatabaseColumn(null)

    // Then
    assertThat(result).isNull()
  }

  @ParameterizedTest
  @EnumSource(ReferralStatusType::class)
  fun `convertToDatabaseColumn should return description for each ReferralStatusType`(statusType: ReferralStatusType) {
    // When
    val result = converter.convertToDatabaseColumn(statusType)

    // Then
    assertThat(result).isEqualTo(statusType.description)
  }

  @Test
  fun `convertToEntityAttribute should return null when description is null`() {
    // When
    val result = converter.convertToEntityAttribute(null)

    // Then
    assertThat(result).isNull()
  }

  @ParameterizedTest
  @EnumSource(ReferralStatusType::class)
  fun `convertToEntityAttribute should return ReferralStatusType for each valid description`(statusType: ReferralStatusType) {
    // When
    val result = converter.convertToEntityAttribute(statusType.description)

    // Then
    assertThat(result).isEqualTo(statusType)
  }

  @ParameterizedTest
  @ValueSource(strings = ["Unknown status", "INVALID", "", " "])
  fun `convertToEntityAttribute should throw IllegalArgumentException when description is invalid`(invalidDescription: String) {
    // When/Then
    assertThrows<IllegalArgumentException> {
      converter.convertToEntityAttribute(invalidDescription)
    }
  }
}
