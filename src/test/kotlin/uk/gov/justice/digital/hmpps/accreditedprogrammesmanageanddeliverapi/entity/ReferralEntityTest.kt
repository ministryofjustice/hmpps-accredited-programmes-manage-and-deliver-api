package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

class ReferralEntityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Test
  @Transactional
  fun `should save and retrieve referral with status`() {
    // Given
    val referral = ReferralEntityFactory()
      .withCrn("CRN123")
      .withPersonName("John Doe")
      .withSourcedFrom(ReferralEntitySourcedFrom.REQUIREMENT)
      .withEventId("REQ-1234-REFENTITYTEST")
      .produce()

    referralRepository.save(referral)
    // When
    val retrievedReferral = referralRepository.findById(referral.id!!).get()

    // Then
    assertThat(retrievedReferral).isNotNull
    assertThat(retrievedReferral.crn).isEqualTo("CRN123")
    assertThat(retrievedReferral.statusHistories).hasSize(0)
    assertThat(retrievedReferral.sourcedFrom).isEqualTo(ReferralEntitySourcedFrom.REQUIREMENT)
    assertThat(retrievedReferral.eventId).isEqualTo("REQ-1234-REFENTITYTEST")
  }
}
