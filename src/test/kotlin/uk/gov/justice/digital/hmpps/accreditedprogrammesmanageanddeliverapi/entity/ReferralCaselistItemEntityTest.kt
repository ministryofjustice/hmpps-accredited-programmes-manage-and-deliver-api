package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCaselistItemFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.produce
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaselistItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

class ReferralCaselistItemEntityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralCaselistItemRepository: ReferralCaselistItemRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  private val referralCaselistItemFactory = ReferralCaselistItemFactory()

  @Test
  @Transactional
  fun `should save and retrieve referral case list item`() {
    // Given
    val referralCaselistItem = referralCaselistItemFactory.produce(crn = "ABC123")
    referralRepository.save(referralCaselistItem.referral)
    val savedReferral = referralCaselistItemRepository.save(referralCaselistItem)

    referralCaselistItemRepository.findAll()
    // When
    val retrievedReferralCaseListItem = referralCaselistItemRepository.findById(savedReferral.id).get()

    // Then
    assertThat(retrievedReferralCaseListItem).isNotNull
    assertThat(retrievedReferralCaseListItem.crn).isEqualTo("ABC123")
  }
}
