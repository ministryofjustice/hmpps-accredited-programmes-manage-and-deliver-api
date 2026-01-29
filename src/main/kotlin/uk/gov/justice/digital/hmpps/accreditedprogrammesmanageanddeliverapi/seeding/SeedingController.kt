package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * DANGER: This controller provides seeding endpoints for local development only.
 *
 * The 'seeding' profile must NEVER be enabled in production or pre-production environments.
 * The DELETE endpoint will destroy ALL referral data in the database.
 */
@RestController
@RequestMapping("/dev/seed")
@Profile("seeding")
class SeedingController(
  private val referralSeederService: ReferralSeederService,
) {

  @PostMapping("/referrals")
  fun seedReferrals(
    @RequestParam(defaultValue = "10") count: Int,
  ): ResponseEntity<SeedingResult> {
    val sanitisedCount = count.coerceIn(1, 500)
    val result = referralSeederService.seedReferrals(sanitisedCount)
    return ResponseEntity.ok(result)
  }

  /**
   * DANGER: Deletes ALL referrals from the database.
   *
   * This endpoint will remove all referral data, not just seeded data.
   * Only available when 'seeding' profile is active - must NEVER be used in production.
   */
  @DeleteMapping("/referrals")
  fun dangerouslyDeleteAllReferrals(): ResponseEntity<TeardownResult> {
    val result = referralSeederService.dangerouslyDeleteAllReferrals()
    return ResponseEntity.ok(result)
  }

  @GetMapping("/health")
  fun health(): ResponseEntity<Map<String, String>> = ResponseEntity.ok(mapOf("status" to "Seeding endpoints active"))
}
