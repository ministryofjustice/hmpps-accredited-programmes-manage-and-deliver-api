package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory

class PniResponseTest {

  @Test
  fun `hasLdc should return true if PNI assessment subTotal greater than 3`() {
    // Given
    val ldc = Ldc(score = 0, subTotal = 4)
    val pniAssessment = PniAssessmentFactory().withLdc(ldc).produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `hasLdc should return true if PNI assessment subTotal is equal to 3`() {
    // Given
    val ldc = Ldc(score = 0, subTotal = 3)
    val pniAssessment = PniAssessmentFactory().withLdc(ldc).produce()
    val response = PniResponseFactory().withAssessment(pniAssessment).produce()

    // When
    val result = response.hasLdc()

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `hasLdc should return false if PNI assessment subTotal is less than 3`() {
    // Given
    val ldc = Ldc(score = 5, subTotal = 2)
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
