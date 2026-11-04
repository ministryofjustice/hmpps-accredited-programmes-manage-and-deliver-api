package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class ReferralRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  override fun beforeEach() {
    super.beforeEach()
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("findMostRecentByCrn")
  inner class FindMostRecentByCrn {
    @Test
    fun `should return the most recent referral when multiple referrals exist for the same crn`() {
      // Given
      val crn = randomCrn()
      val earlierReferral = ReferralEntityFactory()
        .withCrn(crn)
        .produce()
      val latestReferral = ReferralEntityFactory()
        .withCrn(crn)
        .produce()

      val earlierStatusHistory = ReferralStatusHistoryEntityFactory()
        .produce(
          earlierReferral,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val latestStatusHistory = ReferralStatusHistoryEntityFactory()
        .produce(
          latestReferral,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val earlierCohort = ReferralCohortHistoryFactory().withReferral(earlierReferral).produce()
      val latestCohort = ReferralCohortHistoryFactory().withReferral(latestReferral).produce()

      testDataGenerator.createReferralWithFields(earlierReferral, listOf(earlierStatusHistory, earlierCohort))
      testDataGenerator.createReferralWithFields(latestReferral, listOf(latestStatusHistory, latestCohort))

      // When
      val result = referralRepository.findMostRecentByCrn(crn)

      // Then - should get the one created more recently
      assertThat(result).isNotNull
      assertThat(result!!.crn).isEqualTo(crn)
      assertThat(result.id).isEqualTo(latestReferral.id)
    }

    @Test
    fun `should return null when no referral exists for the crn`() {
      // Given
      val crn = randomCrn()

      // When
      val result = referralRepository.findMostRecentByCrn(crn)

      // Then
      assertThat(result).isNull()
    }

    @Test
    fun `should return the only referral when only one exists for the crn`() {
      // Given
      val crn = randomCrn()
      val referral = ReferralEntityFactory()
        .withCrn(crn)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .produce(
          referral,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val cohort = ReferralCohortHistoryFactory().withReferral(referral).produce()

      testDataGenerator.createReferralWithFields(referral, listOf(statusHistory, cohort))

      // When
      val result = referralRepository.findMostRecentByCrn(crn)

      // Then
      assertThat(result).isNotNull
      assertThat(result!!.id).isEqualTo(referral.id)
      assertThat(result.crn).isEqualTo(crn)
    }

    @Test
    fun `should return single referral with correct data`() {
      // Given
      val crn = randomCrn()
      val referral = ReferralEntityFactory()
        .withCrn(crn)
        .produce()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .produce(
          referral,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val cohort = ReferralCohortHistoryFactory().withReferral(referral).produce()

      testDataGenerator.createReferralWithFields(referral, listOf(statusHistory, cohort))

      // When
      val result = referralRepository.findMostRecentByCrn(crn)

      // Then - verify all key properties are present
      assertThat(result).isNotNull
      assertThat(result!!.crn).isEqualTo(crn)
      assertThat(result.id).isEqualTo(referral.id)
    }
  }
}
