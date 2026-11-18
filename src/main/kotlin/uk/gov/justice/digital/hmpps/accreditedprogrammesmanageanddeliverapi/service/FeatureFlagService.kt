package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.flipt.client.FliptClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.FeatureFlag

@Service
class FeatureFlagService(
  private val fliptClient: FliptClient?,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * A flag that can't be evaluated is treated as disabled. That covers Flipt not being configured (locally and in
   * tests), Flipt being unreachable before we've fetched any flags, and the flag not existing in this environment.
   */
  fun isEnabled(flag: FeatureFlag): Boolean {
    if (fliptClient == null) {
      return false
    }

    return try {
      fliptClient.evaluateBoolean(flag.key, flag.key, emptyMap()).isEnabled
    } catch (ex: Exception) {
      log.warn("Unable to evaluate feature flag '${flag.key}', treating it as disabled: ${ex.message}")
      false
    }
  }
}
