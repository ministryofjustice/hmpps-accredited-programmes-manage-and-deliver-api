package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory

class PniResponseTest {

  @Test
  fun `hasLdc should return true if PNI assessment score greater than 3`() {
    // Given
    val ldc = Ldc(score = 4, subTotal = 5)
    val pniAssessment = PniAssessmentFactory().withLdc(ldc).produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `hasLdc should return true if PNI assessment score is equal to 3`() {
    // Given
    val ldc = Ldc(score = 4, subTotal = 5)
    val pniAssessment = PniAssessmentFactory().withLdc(ldc).produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `hasLdc should return false if PNI assessment score is less than 3`() {
    // Given
    val ldc = Ldc(score = 2, subTotal = 5)
    val pniAssessment = PniAssessmentFactory().withLdc(ldc).produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `hasLdc should return false if PNI assessment doesn't contain LDC`() {
    // Given
    val pniAssessment = PniAssessmentFactory().produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isFalse()
  }
}
