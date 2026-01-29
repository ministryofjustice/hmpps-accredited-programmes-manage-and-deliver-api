package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.seeding

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

  @DeleteMapping("/referrals")
  fun teardownSeededData(): ResponseEntity<TeardownResult> {
    val result = referralSeederService.teardownSeededData()
    return ResponseEntity.ok(result)
  }

  @GetMapping("/health")
  fun health(): ResponseEntity<Map<String, String>> = ResponseEntity.ok(mapOf("status" to "Seeding endpoints active"))
}
