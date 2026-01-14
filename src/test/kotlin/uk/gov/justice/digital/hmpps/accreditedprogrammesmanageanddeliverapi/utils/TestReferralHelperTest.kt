package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import kotlin.time.measureTimedValue

class TestReferralHelperTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Test
  fun `test referral creation helper`() {
    val referral = testReferralHelper.createReferral()
    assertThat(referral).isNotNull
  }

  @Test
  fun `test Referral creation with default 'Awaiting allocation' status`() {
    val referral = testReferralHelper.createReferralWithStatus()
    assertThat(referral.statusHistories.first().referralStatusDescription.description)
      .isEqualTo("Awaiting allocation")
  }

  @Test
  fun `test Referral creation with 'Awaiting allocation' status`() {
    val referralStatusDescription =
      referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
    val referral = testReferralHelper.createReferralWithStatus(referralStatusDescription)
    assertThat(referral.statusHistories.first().referralStatusDescription.description)
      .isEqualTo("Programme complete")
  }

  @Test
  fun `create multiple referrals`() {
    val numReferrals = 100

    val (referrals, timeTaken) = measureTimedValue {
      testReferralHelper.createReferrals(numReferrals)
    }

    val avgTime = timeTaken / numReferrals
    val throughput = numReferrals / timeTaken.inWholeSeconds.toDouble()

    println(
      """
        |===============================
        | Performance Test Results
        |===============================
        | Referrals Created : $numReferrals
        | Total Time        : $timeTaken
        | Average per Item  : $avgTime
        | Throughput        : ${"%.2f".format(throughput)} referrals/sec
        |===============================
      """.trimMargin(),
    )

    assertThat(referrals).hasSize(numReferrals)
  }
}
