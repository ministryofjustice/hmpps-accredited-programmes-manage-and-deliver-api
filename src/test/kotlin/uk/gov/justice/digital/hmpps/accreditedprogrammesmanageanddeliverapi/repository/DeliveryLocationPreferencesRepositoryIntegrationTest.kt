package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

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

  @AfterEach
  fun afterEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  @WithMockAuthUser("PROB_PRACTITIONER_1")
  fun `should retrieve a delivery location preference for a referral`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntity(
      deliusCode = "THE-PDU-CODE",
      deliusDescription = "The PDU Description",
    )
    testDataGenerator.createPreferredDeliveryLocationProbationDeliveryUnit(
      pdu,
    )

    val preferredDeliveryLocation = PreferredDeliveryLocationEntity(
      deliusCode = "THE-PDL-CODE",
      deliusDescription = "The PreferredDeliveryLocation Description",
      preferredDeliveryLocationProbationDeliveryUnit = pdu,
    )
    testDataGenerator.createPreferredDeliveryLocation(preferredDeliveryLocation)

    val deliveryLocationPreference = DeliveryLocationPreferenceEntity(
      referral = referralEntity,
      locationsCannotAttendText = "The DeliveryLocationPreferences Cannot Attend Text",
      preferredDeliveryLocations = mutableSetOf(preferredDeliveryLocation),
    )

    testDataGenerator.createDeliveryLocationPreference(deliveryLocationPreference)

    val foundDeliveryLocationPreferences = repository.findByReferralId(referralEntity.id!!)!!

    assertThat(foundDeliveryLocationPreferences.referral).isEqualTo(referralEntity)
    assertThat(foundDeliveryLocationPreferences.createdAt).isNotNull
    assertThat(foundDeliveryLocationPreferences.createdBy).isEqualTo("PROB_PRACTITIONER_1")

    assertThat(foundDeliveryLocationPreferences.preferredDeliveryLocations.size).isEqualTo(1)

    val firstDeliveryLocation = foundDeliveryLocationPreferences.preferredDeliveryLocations.first()
    assertThat(firstDeliveryLocation).isNotNull()
    assertThat(firstDeliveryLocation.deliusCode).isEqualTo("THE-PDL-CODE")
    assertThat(firstDeliveryLocation.deliusDescription).isEqualTo("The PreferredDeliveryLocation Description")

    assertThat(firstDeliveryLocation.preferredDeliveryLocationProbationDeliveryUnit).isNotNull()

    val pduForDeliveryLocation = firstDeliveryLocation.preferredDeliveryLocationProbationDeliveryUnit
    assertThat(pduForDeliveryLocation).isNotNull
    assertThat(pduForDeliveryLocation.deliusCode).isEqualTo("THE-PDU-CODE")
    assertThat(pduForDeliveryLocation.deliusDescription).isEqualTo("The PDU Description")
  }
}
