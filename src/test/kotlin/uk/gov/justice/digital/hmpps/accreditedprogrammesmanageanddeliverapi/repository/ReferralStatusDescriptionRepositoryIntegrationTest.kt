package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.util.*

class ReferralStatusDescriptionRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var statusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

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
  fun `should find all referral status descriptions`() {
    // When
    val allStatuses = statusDescriptionRepository.findAll()

    // Then
    assertThat(allStatuses.map { it.description }).containsExactlyInAnyOrder("Awaiting assessment")
  }

  @Test
  @Transactional
  fun `should retrieve the associated referral status description for a status history entity`() {
    // Given
    val awaitingAssessmentStatusDescription = statusDescriptionRepository.getAwaitingAssessmentStatusDescription()
    val referral = referralRepository.save(ReferralEntityFactory().produce())

    val statusHistory = ReferralStatusHistoryEntityFactory()
      .produce(referral, awaitingAssessmentStatusDescription)

    statusHistoryRepository.save(statusHistory)

    // When
    val retrievedStatusHistory = statusHistoryRepository.findById(statusHistory.id!!)

    // Then
    assertThat(retrievedStatusHistory).isPresent
    assertThat(retrievedStatusHistory.get().referralStatusDescription?.description).isEqualTo("Awaiting assessment")
    assertThat(retrievedStatusHistory.get().referralStatusDescription?.isClosed).isFalse
  }

  @Test
  fun `should retrieve awaiting assessment status description`() {
    // When
    val awaitingAssessmentStatusDescription = statusDescriptionRepository.getAwaitingAssessmentStatusDescription()

    // Then
    assertThat(awaitingAssessmentStatusDescription).isNotNull
    assertThat(awaitingAssessmentStatusDescription.description).isEqualTo("Awaiting assessment")
  }

  @Test
  fun `should not let you save a ReferralStatusDescriptionEntity`() {
    assertThrows<NotImplementedError> {
      statusDescriptionRepository.save(ReferralStatusDescriptionEntityFactory().produce())
    }
  }
}
