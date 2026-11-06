package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.AWAITING_ALLOCATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.BREACH
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.ON_HOLD
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.ON_PROGRAMME
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.PROGRAMME_COMPLETE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.RECALL
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.RETURN_TO_COURT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.SCHEDULED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.SUITABLE_BUT_NOT_READY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.type.ReferralStatusType.WITHDRAWN

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
    val result = repository.findByDescription(AWAITING_ASSESSMENT.description)!!
    assertThat(result.description).isEqualTo("Awaiting assessment")
  }

  @Test
  @Transactional
  fun `getAwaitingAllocationStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(AWAITING_ALLOCATION.description)!!
    assertThat(result.description).isEqualTo("Awaiting allocation")
  }

  @Test
  @Transactional
  fun `getSuitableButNotReadyStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(SUITABLE_BUT_NOT_READY.description)!!
    assertThat(result.description).isEqualTo("Suitable but not ready")
  }

  @Test
  @Transactional
  fun `getRecallStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(RECALL.description)!!
    assertThat(result.description).isEqualTo("Recall")
  }

  @Test
  @Transactional
  fun `getReturnToCourtStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(RETURN_TO_COURT.description)!!
    assertThat(result.description).isEqualTo("Return to court")
  }

  @Test
  @Transactional
  fun `getScheduledStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(SCHEDULED.description)!!
    assertThat(result.description).isEqualTo("Scheduled")
  }

  @Test
  @Transactional
  fun `getOnProgrammeStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(ON_PROGRAMME.description)!!
    assertThat(result.description).isEqualTo("On programme")
  }

  @Test
  @Transactional
  fun `getProgrammeCompleteStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(PROGRAMME_COMPLETE.description)!!
    assertThat(result.description).isEqualTo("Programme complete")
  }

  @Test
  @Transactional
  fun `getBreachStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(BREACH.description)!!
    assertThat(result.description).isEqualTo("Breach")
  }

  @Test
  @Transactional
  fun `getOnHoldStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(ON_HOLD.description)!!
    assertThat(result.description).isEqualTo("On hold")
  }

  @Test
  @Transactional
  fun `getWithdrawnStatusDescription returns a Referral Status Description`() {
    val result = repository.findByDescription(WITHDRAWN.description)!!
    assertThat(result.description).isEqualTo("Withdrawn")
  }

  @Test
  fun `findMostRecentStatusByReferralId returns most recent associated referral status history record`() {
    // Given
    val referral = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(
      referral,
      listOf(
        repository.findByDescription(AWAITING_ASSESSMENT.description)!!,
        repository.findByDescription(SUITABLE_BUT_NOT_READY.description)!!,
        repository.findByDescription(ON_HOLD.description)!!,
        repository.findByDescription(RECALL.description)!!,
        repository.findByDescription(ON_PROGRAMME.description)!!,
        repository.findByDescription(PROGRAMME_COMPLETE.description)!!,
      ),
    )

    // When
    val latestStatus = repository.findMostRecentStatusByReferralId(referral.id!!)

    // Then
    assertThat(latestStatus).isNotNull
    assertThat(latestStatus?.description).isEqualTo(PROGRAMME_COMPLETE.description)
  }
}
