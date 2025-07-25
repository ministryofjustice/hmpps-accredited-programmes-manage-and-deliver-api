package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository

class ReferralEntityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Test
  @Transactional
  fun `should save and retrieve referral with status`() {
    // Given
    val referral = ReferralEntityFactory()
      .withCrn("CRN123")
      .withPersonName("John Doe")
      .produce()

    referralRepository.save(referral)

    val referralStatusHistory = ReferralStatusHistoryEntityFactory()
      .withStatus("Assessment started").produce()

    referralStatusHistoryRepository.save(referralStatusHistory)

    referral.statusHistories.add(referralStatusHistory)

    // When
    val retrievedReferral = referralRepository.findById(referral.id!!).get()

    // Then
    assertThat(retrievedReferral).isNotNull
    assertThat(retrievedReferral.crn).isEqualTo("CRN123")
    assertThat(retrievedReferral.statusHistories).hasSize(1)
    assertThat(retrievedReferral.statusHistories[0].status).isEqualTo("Assessment started")
  }
}
