package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class ReferralStatusTransitionRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusTransitionRepository: ReferralStatusTransitionRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `transitions that represent the happy path have is_continuing set to true`() {
    // Awaiting assessment --> Awaiting allocation
    val awaitingAssessmentToAwaitingAllocation = getTransition("Awaiting assessment", "Awaiting allocation")
    assertThat(awaitingAssessmentToAwaitingAllocation).isNotNull
    assertThat(awaitingAssessmentToAwaitingAllocation!!.isContinuing).isTrue()

    // Awaiting allocation --> Scheduled
    val awaitingAllocationToScheduled = getTransition("Awaiting allocation", "Scheduled")
    assertThat(awaitingAllocationToScheduled).isNotNull
    assertThat(awaitingAllocationToScheduled!!.isContinuing).isTrue()

    // Scheduled --> On programme
    val scheduledToOnProgramme = getTransition("Scheduled", "On programme")
    assertThat(scheduledToOnProgramme).isNotNull
    assertThat(scheduledToOnProgramme!!.isContinuing).isTrue()

    // On programme --> Programme complete
    val onProgrammeToProgrammeComplete = getTransition("On programme", "Programme complete")
    assertThat(onProgrammeToProgrammeComplete).isNotNull
    assertThat(onProgrammeToProgrammeComplete!!.isContinuing).isTrue()

    // Suitable but not ready --> Awaiting assessment
    val suitableButNotReadyToAwaitingAssessment = getTransition("Suitable but not ready", "Awaiting assessment")
    assertThat(suitableButNotReadyToAwaitingAssessment).isNotNull
    assertThat(suitableButNotReadyToAwaitingAssessment!!.isContinuing).isTrue()

    // Deprioritised --> Awaiting assessment
    val deprioritisedToAwaitingAssessment = getTransition("Deprioritised", "Awaiting assessment")
    assertThat(deprioritisedToAwaitingAssessment).isNotNull
    assertThat(deprioritisedToAwaitingAssessment!!.isContinuing).isTrue()

    // Breach (non-attendance) --> Awaiting assessment
    val breachToAwaitingAssessment = getTransition("Breach (non-attendance)", "Awaiting assessment")
    assertThat(breachToAwaitingAssessment).isNotNull
    assertThat(breachToAwaitingAssessment!!.isContinuing).isTrue()

    // Recall --> Awaiting assessment
    val recallToAwaitingAssessment = getTransition("Recall", "Awaiting assessment")
    assertThat(recallToAwaitingAssessment).isNotNull
    assertThat(recallToAwaitingAssessment!!.isContinuing).isTrue()

    // Deferred --> Awaiting assessment
    val deferredToAwaitingAssessment = getTransition("Deferred", "Awaiting assessment")
    assertThat(deferredToAwaitingAssessment).isNotNull
    assertThat(deferredToAwaitingAssessment!!.isContinuing).isTrue()
  }

  @Test
  @Transactional
  fun `transitions that represent backwards or sideways movement have is_continuing set to false`() {
    // Awaiting assessment --> Suitable but not ready
    val awaitingAssessmentToSuitableButNotReady = getTransition("Awaiting assessment", "Suitable but not ready")
    assertThat(awaitingAssessmentToSuitableButNotReady).isNotNull
    assertThat(awaitingAssessmentToSuitableButNotReady!!.isContinuing).isFalse()

    // Awaiting assessment --> Deprioritised
    val awaitingAssessmentToDeprioritised = getTransition("Awaiting assessment", "Deprioritised")
    assertThat(awaitingAssessmentToDeprioritised).isNotNull
    assertThat(awaitingAssessmentToDeprioritised!!.isContinuing).isFalse()

    // Scheduled --> Deprioritised
    val scheduledToDeprioritised = getTransition("Scheduled", "Deprioritised")
    assertThat(scheduledToDeprioritised).isNotNull
    assertThat(scheduledToDeprioritised!!.isContinuing).isFalse()

    // On programme --> Breach (non-attendance)
    val onProgrammeToBreach = getTransition("On programme", "Breach (non-attendance)")
    assertThat(onProgrammeToBreach).isNotNull
    assertThat(onProgrammeToBreach!!.isContinuing).isFalse()

    // Awaiting allocation --> Return to court
    val awaitingAllocationToReturnToCourt = getTransition("Awaiting allocation", "Return to court")
    assertThat(awaitingAllocationToReturnToCourt).isNotNull
    assertThat(awaitingAllocationToReturnToCourt!!.isContinuing).isFalse()

    // Return to court --> Withdrawn
    val returnToCourtToWithdrawn = getTransition("Return to court", "Withdrawn")
    assertThat(returnToCourtToWithdrawn).isNotNull
    assertThat(returnToCourtToWithdrawn!!.isContinuing).isFalse()
  }

  @Test
  @Transactional
  fun `findByFromStatusId returns all transitions for a given status`() {
    val awaitingAssessment = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
    val transitions = referralStatusTransitionRepository.findByFromStatusId(awaitingAssessment.id)

    assertThat(transitions).isNotEmpty
    assertThat(transitions).allMatch { it.fromStatus.id == awaitingAssessment.id }

    // Should include transitions to: Awaiting allocation, Suitable but not ready, Deprioritised, Recall, Return to court
    assertThat(transitions).hasSizeGreaterThanOrEqualTo(5)
  }

  private fun getTransition(fromStatusDescription: String, toStatusDescription: String) = referralStatusTransitionRepository.findAll().find {
    it.fromStatus.description == fromStatusDescription && it.toStatus.description == toStatusDescription
  }

  @Test
  @Transactional
  fun `should return statuses ordered by priority`() {

    val onProgramme = referralStatusDescriptionRepository.getOnProgrammeStatusDescription()
    val transitions = referralStatusTransitionRepository.findByFromStatusIdAndIsVisibleTrueOrderByPriorityAsc(onProgramme.id)

    // Assert: Verify the order
    assertThat(transitions[0].description).isEqualTo("Awaiting Assessment")
    assertThat(transitions[1].description).isEqualTo("Awaiting Allocation")
    assertThat(transitions[2].description).isEqualTo("Breach (non-attendance)")
    assertThat(transitions[3].description).isEqualTo("Recall")
    assertThat(transitions[4].description).isEqualTo("Return to court")
  }

  @Test
  @Transactional
  fun `ensure there is exactly one initial status and all others are reachable`() {
    referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
    val allStatuses = referralStatusDescriptionRepository.findAll()

    val unreachable = allStatuses.filter {
      referralStatusTransitionRepository.findByToStatus(it).isEmpty()
    }

    assertThat(unreachable).hasSize(0)
  }
}
