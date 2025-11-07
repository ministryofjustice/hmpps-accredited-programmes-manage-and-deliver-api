package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository

class TestReferralHelperTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Test
  fun `test referral creation helper`() {
    val referral = testReferralHelper.createReferral()
    Assertions.assertThat(referral).isNotNull
  }

  @Test
  fun `test Referral creation with default 'Awaiting allocation' status`() {
    val referral = testReferralHelper.createReferralWithStatus()
    Assertions.assertThat(referral.statusHistories.first().referralStatusDescription.description)
      .isEqualTo("Awaiting allocation")
  }

  @Test
  fun `test Referral creation with 'Awaiting allocation' status`() {
    val referralStatusDescription =
      referralStatusDescriptionRepository.getProgrammeCompleteStatusDescription()
    val referral = testReferralHelper.createReferralWithStatus(referralStatusDescription)
    Assertions.assertThat(referral.statusHistories.first().referralStatusDescription.description)
      .isEqualTo("Programme complete")
  }
}
