package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType

class ReferralStatusDescriptionRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var repository: ReferralStatusDescriptionRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `getAwaitingAssessmentStatusDescription returns a Referral Status Description`() {
    val result = repository.getAwaitingAssessmentStatusDescription()
    assertThat(result.description).isEqualTo("Awaiting assessment")
  }

  @Test
  @Transactional
  fun `getAwaitingAllocationStatusDescription returns a Referral Status Description`() {
    val result = repository.getAwaitingAllocationStatusDescription()
    assertThat(result.description).isEqualTo("Awaiting allocation")
  }

  @Test
  @Transactional
  fun `getSuitableButNotReadyStatusDescription returns a Referral Status Description`() {
    val result = repository.getSuitableButNotReadyStatusDescription()
    assertThat(result.description).isEqualTo("Suitable but not ready")
  }

  @Test
  @Transactional
  fun `getDeprioritisedStatusDescription returns a Referral Status Description`() {
    val result = repository.getDeprioritisedStatusDescription()
    assertThat(result.description).isEqualTo("Deprioritised")
  }

  @Test
  @Transactional
  fun `getRecallStatusDescription returns a Referral Status Description`() {
    val result = repository.getRecallStatusDescription()
    assertThat(result.description).isEqualTo("Recall")
  }

  @Test
  @Transactional
  fun `getReturnToCourtStatusDescription returns a Referral Status Description`() {
    val result = repository.getReturnToCourtStatusDescription()
    assertThat(result.description).isEqualTo("Return to court")
  }

  @Test
  @Transactional
  fun `getScheduledStatusDescription returns a Referral Status Description`() {
    val result = repository.getScheduledStatusDescription()
    assertThat(result.description).isEqualTo("Scheduled")
  }

  @Test
  @Transactional
  fun `getOnProgrammeStatusDescription returns a Referral Status Description`() {
    val result = repository.getOnProgrammeStatusDescription()
    assertThat(result.description).isEqualTo("On programme")
  }

  @Test
  @Transactional
  fun `getProgrammeCompleteStatusDescription returns a Referral Status Description`() {
    val result = repository.getProgrammeCompleteStatusDescription()
    assertThat(result.description).isEqualTo("Programme complete")
  }

  @Test
  @Transactional
  fun `getBreachNonAttendanceStatusDescription returns a Referral Status Description`() {
    val result = repository.getBreachNonAttendanceStatusDescription()
    assertThat(result.description).isEqualTo("Breach (non-attendance)")
  }

  @Test
  @Transactional
  fun `getDeferredStatusDescription returns a Referral Status Description`() {
    val result = repository.getDeferredStatusDescription()
    assertThat(result.description).isEqualTo("Deferred")
  }

  @Test
  @Transactional
  fun `getWithdrawnStatusDescription returns a Referral Status Description`() {
    val result = repository.getWithdrawnStatusDescription()
    assertThat(result.description).isEqualTo("Withdrawn")
  }

  @Test
  fun `findMostRecentStatusByReferralId returns most recent associated referral status history record`() {
    // Given
    val referral = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(
      referral,
      listOf(
        repository.getAwaitingAssessmentStatusDescription(),
        repository.getSuitableButNotReadyStatusDescription(),
        repository.getDeprioritisedStatusDescription(),
        repository.getRecallStatusDescription(),
        repository.getOnProgrammeStatusDescription(),
        repository.getProgrammeCompleteStatusDescription(),
      ),
    )

    // When
    val latestStatus = repository.findMostRecentStatusByReferralId(referral.id!!)

    // Then
    assertThat(latestStatus).isNotNull
    assertThat(latestStatus?.description).isEqualTo(ReferralStatusType.PROGRAMME_COMPLETE.description)
  }
}
