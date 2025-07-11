package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaselistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryMappingRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import java.util.UUID

class ReferralCaselistItemViewEntityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralCaselistItemViewRepository: ReferralCaselistItemViewRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  private lateinit var referralStatusHistoryMappingRepository: ReferralStatusHistoryMappingRepository

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `should save and retrieve referral case list item`() {
    // Given
    val referral = ReferralEntityFactory()
      .withCrn("CRN123")
      .withPersonName("John Doe")
      .produce()

    val savedReferral = referralRepository.save(referral)

    val referralStatusHistory = ReferralStatusHistoryEntityFactory()
      .withStatus("Assessment started")
      .produce()

    val savedStatusHistory = referralStatusHistoryRepository.save(referralStatusHistory)

    val referralStatusHistoryMapping = ReferralStatusHistoryMappingEntity(
      UUID.randomUUID(),
      referral = savedReferral,
      referralStatusHistory = savedStatusHistory,
    )
    referralStatusHistoryMappingRepository.save(referralStatusHistoryMapping)

    // Force flush to ensure all data is persisted
    referralRepository.flush()
    referralStatusHistoryRepository.flush()
    referralStatusHistoryMappingRepository.flush()

    // Clear the persistence context to ensure fresh data retrieval
    entityManager.clear()

    // When
    val retrievedReferral = referralRepository.findById(savedReferral.id!!).get()
    val viewItems = referralCaselistItemViewRepository.findAll()

    // Then
    assertThat(retrievedReferral).isNotNull
    assertThat(retrievedReferral.crn).isEqualTo("CRN123")
    assertThat(retrievedReferral.statusHistories).hasSize(1)
    assertThat(retrievedReferral.statusHistories[0].status).isEqualTo("Assessment started")

    // Additional assertions for the view
    assertThat(viewItems).isNotEmpty
    val viewItem = viewItems.find { it.crn == retrievedReferral.crn }
    assertThat(viewItem).isNotNull
    assertThat(viewItem?.crn).isEqualTo("CRN123")
    assertThat(viewItem?.personName).isEqualTo("John Doe")
  }
}
