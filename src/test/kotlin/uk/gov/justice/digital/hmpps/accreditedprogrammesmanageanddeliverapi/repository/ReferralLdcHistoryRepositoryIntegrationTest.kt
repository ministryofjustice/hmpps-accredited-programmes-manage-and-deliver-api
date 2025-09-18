package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralLdcHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class ReferralLdcHistoryRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var repo: ReferralLdcHistoryRepository

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

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
  @WithMockAuthUser("SYSTEM")
  fun `retrieve the latest LDC status for a referral`() {
    val referralEntity = ReferralEntityFactory().produce()
    val ldcEntity1 = ReferralLdcHistoryFactory().withReferral(referralEntity).withHasLdc(true).produce()
    val ldcEntity2 = ReferralLdcHistoryFactory().withReferral(referralEntity).withHasLdc(false).produce()
    testDataGenerator.createReferral(referralEntity)
    testDataGenerator.createLdcHistoryForAReferral(ldcEntity1)
    testDataGenerator.createLdcHistoryForAReferral(ldcEntity2)

    val savedLdcEntity = repo.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)!!

    assertThat(savedLdcEntity).isNotNull
    assertThat(savedLdcEntity.hasLdc).isFalse
  }
}
