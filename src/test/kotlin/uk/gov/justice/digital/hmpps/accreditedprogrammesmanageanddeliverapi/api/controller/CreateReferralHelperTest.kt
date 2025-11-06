package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.CreateReferralHelper

class CreateReferralHelperTest : IntegrationTestBase() {
  @Autowired
  lateinit var createReferralHelper: CreateReferralHelper

  @Test
  fun `test referral creation helper`() {
    val referral = createReferralHelper.createReferral()
    assertThat(referral).isNotNull
  }

  @Test
  fun `test Referral creation with 'Awaiting allocation' status`() {
    val awaitingAllocationReferral = createReferralHelper.createReferralWithStatus()
    assertThat(awaitingAllocationReferral.statusHistories.first().referralStatusDescription.description).isEqualTo("Awaiting allocation")
  }
}
