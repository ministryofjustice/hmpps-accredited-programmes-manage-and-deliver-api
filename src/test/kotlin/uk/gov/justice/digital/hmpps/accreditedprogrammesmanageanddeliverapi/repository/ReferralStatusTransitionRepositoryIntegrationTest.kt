package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusDescriptionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusTransitionEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.util.*

class ReferralStatusTransitionRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var statusTransitionRepository: ReferralStatusTransitionRepository

  @Autowired
  private lateinit var statusDescriptionRepository: ReferralStatusDescriptionRepository

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
  fun `should save and retrieve a referral status transitions with associated status descriptions`() {
    // Given
    val fromStatusId = UUID.randomUUID()
    val fromStatus = ReferralStatusDescriptionEntityFactory()
      .withId(fromStatusId)
      .withDescription("Awaiting Assessment")
      .withIsClosed(false)
      .withLabelColour("grey")
      .produce()
    statusDescriptionRepository.save(fromStatus)

    // Create the to statuses
    val toStatusId1 = UUID.randomUUID()
    val toStatus1 = ReferralStatusDescriptionEntityFactory()
      .withId(toStatusId1)
      .withDescription("Assessment Started")
      .withIsClosed(false)
      .withLabelColour("light-blue")
      .produce()
    statusDescriptionRepository.save(toStatus1)
    val toStatusId2 = UUID.randomUUID()
    val toStatus2 = ReferralStatusDescriptionEntityFactory()
      .withId(toStatusId2)
      .withDescription("Withdrawn")
      .withIsClosed(true)
      .withLabelColour("yellow")
      .produce()
    statusDescriptionRepository.save(toStatus2)

    // Create transitions
    val transitionId1 = UUID.randomUUID()
    val transition1 = ReferralStatusTransitionEntityFactory()
      .withId(transitionId1)
      .withFromStatus(fromStatus)
      .withToStatus(toStatus1)
      .withDescription("Awaiting Assessment to Assessment Started")
      .produce()
    val transitionId2 = UUID.randomUUID()
    val transition2 = ReferralStatusTransitionEntityFactory()
      .withId(transitionId2)
      .withFromStatus(fromStatus)
      .withToStatus(toStatus2)
      .withDescription("Awaiting Assessment to Withdrawn")
      .produce()

    // When
    statusTransitionRepository.saveAll(listOf(transition1, transition2))

    // Then
    val availableTransitions = statusTransitionRepository.findAll()
    assertThat(availableTransitions.size).isEqualTo(2)

    val retrieved1 = statusTransitionRepository.findById(transitionId1)
    assertThat(retrieved1).isPresent

    val statusTransition1 = retrieved1.get()
    assertThat(statusTransition1.fromStatus.description).isEqualTo("Awaiting Assessment")
    assertThat(statusTransition1.fromStatus.isClosed).isFalse()
    assertThat(statusTransition1.toStatus.description).isEqualTo("Assessment Started")
    assertThat(statusTransition1.toStatus.isClosed).isFalse()
    assertThat(statusTransition1.toStatus.labelColour).isEqualTo("light-blue")
    assertThat(statusTransition1.description).isEqualTo("Awaiting Assessment to Assessment Started")

    val retrieved2 = statusTransitionRepository.findById(transitionId2)
    assertThat(retrieved2).isPresent

    val statusTransition2 = retrieved2.get()
    assertThat(statusTransition2.fromStatus.description).isEqualTo("Awaiting Assessment")
    assertThat(statusTransition2.fromStatus.isClosed).isFalse()
    assertThat(statusTransition2.toStatus.description).isEqualTo("Withdrawn")
    assertThat(statusTransition2.toStatus.isClosed).isTrue()
    assertThat(statusTransition2.toStatus.labelColour).isEqualTo("yellow")
    assertThat(statusTransition2.description).isEqualTo("Awaiting Assessment to Withdrawn")
  }
}
