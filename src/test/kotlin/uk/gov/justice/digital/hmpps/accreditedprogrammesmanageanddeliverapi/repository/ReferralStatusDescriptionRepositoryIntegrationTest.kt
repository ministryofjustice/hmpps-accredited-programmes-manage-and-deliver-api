package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ALLOCATION
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.BREACH
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.ON_HOLD
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.ON_PROGRAMME
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.PROGRAMME_COMPLETE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.RECALL
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.RETURN_TO_COURT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.SCHEDULED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.SUITABLE_BUT_NOT_READY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.WITHDRAWN

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
    val result = repository.findByReferralStatusType(AWAITING_ASSESSMENT)!!
    assertThat(result.referralStatusType).isEqualTo(AWAITING_ASSESSMENT)
  }

  @Test
  @Transactional
  fun `getAwaitingAllocationStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(AWAITING_ALLOCATION)!!
    assertThat(result.referralStatusType).isEqualTo(AWAITING_ALLOCATION)
  }

  @Test
  @Transactional
  fun `getSuitableButNotReadyStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(SUITABLE_BUT_NOT_READY)!!
    assertThat(result.referralStatusType).isEqualTo(SUITABLE_BUT_NOT_READY)
  }

  @Test
  @Transactional
  fun `getRecallStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(RECALL)!!
    assertThat(result.referralStatusType).isEqualTo(RECALL)
  }

  @Test
  @Transactional
  fun `getReturnToCourtStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(RETURN_TO_COURT)!!
    assertThat(result.referralStatusType).isEqualTo(RETURN_TO_COURT)
  }

  @Test
  @Transactional
  fun `getScheduledStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(SCHEDULED)!!
    assertThat(result.referralStatusType).isEqualTo(SCHEDULED)
  }

  @Test
  @Transactional
  fun `getOnProgrammeStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(ON_PROGRAMME)!!
    assertThat(result.referralStatusType).isEqualTo(ON_PROGRAMME)
  }

  @Test
  @Transactional
  fun `getProgrammeCompleteStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(PROGRAMME_COMPLETE)!!
    assertThat(result.referralStatusType).isEqualTo(PROGRAMME_COMPLETE)
  }

  @Test
  @Transactional
  fun `getBreachStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(BREACH)!!
    assertThat(result.referralStatusType).isEqualTo(BREACH)
  }

  @Test
  @Transactional
  fun `getOnHoldStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(ON_HOLD)!!
    assertThat(result.referralStatusType).isEqualTo(ON_HOLD)
  }

  @Test
  @Transactional
  fun `getWithdrawnStatusDescription returns a Referral Status Description`() {
    val result = repository.findByReferralStatusType(WITHDRAWN)!!
    assertThat(result.referralStatusType).isEqualTo(WITHDRAWN)
  }

  @Test
  fun `findMostRecentStatusByReferralId returns most recent associated referral status history record`() {
    // Given
    val referral = ReferralEntityFactory().produce()
    testDataGenerator.createReferralWithStatusHistory(
      referral,
      listOf(
        repository.findByReferralStatusType(AWAITING_ASSESSMENT)!!,
        repository.findByReferralStatusType(SUITABLE_BUT_NOT_READY)!!,
        repository.findByReferralStatusType(ON_HOLD)!!,
        repository.findByReferralStatusType(RECALL)!!,
        repository.findByReferralStatusType(ON_PROGRAMME)!!,
        repository.findByReferralStatusType(PROGRAMME_COMPLETE)!!,
      ),
    )

    // When
    val latestStatus = repository.findMostRecentStatusByReferralId(referral.id!!)

    // Then
    assertThat(latestStatus).isNotNull
    assertThat(latestStatus?.referralStatusType).isEqualTo(PROGRAMME_COMPLETE)
  }
}
