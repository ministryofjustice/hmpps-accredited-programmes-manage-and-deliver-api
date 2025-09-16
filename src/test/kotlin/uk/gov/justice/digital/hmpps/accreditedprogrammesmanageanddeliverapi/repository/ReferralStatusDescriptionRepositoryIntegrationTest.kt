package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.util.*

class ReferralStatusDescriptionRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var statusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var statusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `should save and retrieve a referral status description`() {
    // Given
    val statusId = UUID.randomUUID()
    val statusDescription = ReferralStatusDescriptionEntityFactory()
      .withId(statusId)
      .withDescription("Test Status Description")
      .withIsClosed(false)
      .withLabelColour("Grey")
      .produce()

    statusDescriptionRepository.save(statusDescription)

    // When
    val retrieved = statusDescriptionRepository.findById(statusId)

    // Then
    assertThat(retrieved).isPresent
    assertThat(retrieved.get().description).isEqualTo("Test Status Description")
    assertThat(retrieved.get().isClosed).isFalse()
    assertThat(retrieved.get().labelColour).isEqualTo("Grey")
  }

  @Test
  @Transactional
  fun `should find all referral status descriptions`() {
    // Given
    val status1 = ReferralStatusDescriptionEntityFactory()
      .withId(UUID.randomUUID())
      .withDescription("Status 1")
      .withIsClosed(false)
      .produce()
    val status2 = ReferralStatusDescriptionEntityFactory()
      .withId(UUID.randomUUID())
      .withDescription("Status 2")
      .withIsClosed(true)
      .produce()

    statusDescriptionRepository.saveAll(listOf(status1, status2))

    // When
    val allStatuses = statusDescriptionRepository.findAll()

    // Then
    assertThat(allStatuses).hasSize(2)
    assertThat(allStatuses.map { it.description }).containsExactlyInAnyOrder("Status 1", "Status 2")
  }

  @Test
  @Transactional
  fun `should retrieve the associated referral status description for a status history entity`() {
    // Given
    val status = ReferralStatusDescriptionEntityFactory()
      .withId(UUID.randomUUID())
      .withDescription("Awaiting Assessment")
      .withIsClosed(false)
      .produce()

    val savedStatusDescription = statusDescriptionRepository.save(status)

    val statusHistory = ReferralStatusHistoryEntityFactory()
      .withReferralStatusDescription(savedStatusDescription)
      .produce()
    statusHistoryRepository.save(statusHistory)

    // When
    val retrievedStatusHistory = statusHistoryRepository.findById(statusHistory.id!!)

    // Then
    assertThat(retrievedStatusHistory).isPresent
    assertThat(retrievedStatusHistory.get().referralStatusDescription?.description).isEqualTo("Awaiting Assessment")
    assertThat(retrievedStatusHistory.get().referralStatusDescription?.isClosed).isFalse
  }
}
