package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class LimitedAccessOffenderAuthorisationStrategyTest {

  private val strategy = object : LimitedAccessOffenderAuthorisationStrategy {
    override fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean = true
    override fun isAuthorised(httpRequestPath: String, username: String): Boolean = true
  }

  @Test
  fun `getId returns UUID when path matches and UUID is valid`() {
    // Given
    val referralId = UUID.randomUUID()
    val path = "/referrals/$referralId"
    val pattern = "/referrals/{referralId}"

    // When
    val result = strategy.getId(path, "referralId", pattern)

    // Then
    assertThat(result).isEqualTo(referralId)
  }

  @Test
  fun `getId returns null when UUID is invalid`() {
    // Given
    val path = "/referrals/not-a-uuid"
    val pattern = "/referrals/{referralId}"

    // When
    val result = strategy.getId(path, "referralId", pattern)

    // Then
    assertThat(result).isNull()
  }

  @Test
  fun `getId returns null when path variable name is missing in pattern`() {
    // Given
    val referralId = UUID.randomUUID()
    val path = "/referrals/$referralId"
    val pattern = "/referrals/{wrongId}"

    // When
    val result = strategy.getId(path, "referralId", pattern)

    // Then
    assertThat(result).isNull()
  }

  @Test
  fun `getId returns null when path does not match pattern`() {
    // Given
    val referralId = UUID.randomUUID()
    val path = "/wrong-path/$referralId"
    val pattern = "/referrals/{referralId}"

    // When
    val result = strategy.getId(path, "referralId", pattern)

    // Then
    assertThat(result).isNull()
  }
}
