package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralReportingLocationFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class ReferralReportingLocationRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repo: ReferralReportingLocationRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

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
  fun `retrieve reporting locations for a referral`() {
    val referralEntity = ReferralEntityFactory().produce()
    val referralReportingLocationEntity = ReferralReportingLocationFactory()
      .withPduName("PDU_1")
      .withReportingTeam("REPORTING_TEAM_1")
      .withReferral(referralEntity).produce()
    val statusHistory = ReferralStatusHistoryEntityFactory()
      .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
      .produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
    testDataGenerator.createReferralWithStatusHistory(referralEntity, statusHistory)
    testDataGenerator.createReferralWithReportingLocation(referralReportingLocationEntity)

    val savedEntity = repo.findFirstByReferralId(referralEntity.id!!)!!

    assertThat(savedEntity).isNotNull
    assertThat(savedEntity.pduName).isEqualTo("PDU_1")
    assertThat(savedEntity.reportingTeam).isEqualTo("REPORTING_TEAM_1")
  }
}
