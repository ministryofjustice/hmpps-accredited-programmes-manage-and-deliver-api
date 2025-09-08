package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.util.UUID

class DeliveryLocationPreferencesRepositoryIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var repository: DeliveryLocationPreferenceRepository

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  @WithMockAuthUser("PROB_PRACTITIONER_1")
  fun `should retrieve a delivery location preference for a referral`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val deliveryLocationPreference = DeliveryLocationPreferenceEntity(
      id = UUID.randomUUID(),
      referral = referralEntity,
      locationsCannotAttendText = "Alex cannot attend any locations in Postcode beginning NE1.",
    )

    testDataGenerator.createDeliveryLocationPreference(
      deliveryLocationPreference,
    )
    val result = repository.findByReferralId(referralEntity.id!!).first()

    assertThat(result.referral).isEqualTo(referralEntity)
    assertThat(result.createdAt).isNotNull
    assertThat(result.createdBy).isEqualTo("PROB_PRACTITIONER_1")
  }
}
