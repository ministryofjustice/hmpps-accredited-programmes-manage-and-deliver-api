package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.flipt.client.FliptClient
import io.flipt.client.FliptException
import io.flipt.client.models.BooleanEvaluationResponse
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.FeatureFlag.REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER

class FeatureFlagServiceTest {
  private val fliptClient: FliptClient = mockk()
  private val featureFlagService = FeatureFlagService(fliptClient)

  @Test
  fun `isEnabled should return true when the flag is enabled in Flipt`() {
    // Given
    every {
      fliptClient.evaluateBoolean("referral-location-use-requirement-manager", "referral-location-use-requirement-manager", emptyMap())
    } returns BooleanEvaluationResponse.builder().enabled(true).flagKey("referral-location-use-requirement-manager").build()

    // When
    val result = featureFlagService.isEnabled(REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `isEnabled should return false when the flag is disabled in Flipt`() {
    // Given
    every {
      fliptClient.evaluateBoolean("referral-location-use-requirement-manager", "referral-location-use-requirement-manager", emptyMap())
    } returns BooleanEvaluationResponse.builder().enabled(false).flagKey("referral-location-use-requirement-manager").build()

    // When
    val result = featureFlagService.isEnabled(REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `isEnabled should return false when the flag can't be evaluated`() {
    // Given this is what the client throws when the flag doesn't exist, or before it has fetched any flags
    every {
      fliptClient.evaluateBoolean(any(), any(), any())
    } throws FliptException.EvaluationException("invalid request: failed to get flag information")

    // When
    val result = featureFlagService.isEnabled(REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `isEnabled should return false when Flipt isn't configured`() {
    // When
    val result = FeatureFlagService(fliptClient = null).isEnabled(REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER)

    // Then
    assertThat(result).isFalse()
  }
}
