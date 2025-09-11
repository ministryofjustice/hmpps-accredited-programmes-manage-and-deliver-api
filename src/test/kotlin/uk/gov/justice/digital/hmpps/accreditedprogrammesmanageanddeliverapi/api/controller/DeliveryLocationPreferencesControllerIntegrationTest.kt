package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.CreateDeliveryLocationPreferencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.DeliveryLocationPreferenceEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationProbationDeliveryUnitEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences.PreferredDeliveryLocationsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.DeliveryLocationPreferenceRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.PreferredDeliveryLocationProbationDeliveryUnitRepository
import java.util.UUID

class DeliveryLocationPreferencesControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var deliveryLocationPreferenceRepository: DeliveryLocationPreferenceRepository

  @Autowired
  private lateinit var deliveryLocationProbationDeliveryUnitRepository: PreferredDeliveryLocationProbationDeliveryUnitRepository

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `create delivery preferences for referral that exists and does not have existing locations or pdus`() {
    val referralEntity = ReferralEntityFactory().produce()
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isZero

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isOne

    val submittedPdu = createDeliveryLocationPreferences.preferredDeliveryLocations.first()
    val savedPdu = deliveryLocationProbationDeliveryUnitRepository.findByDeliusCode(submittedPdu.pduCode)!!
    assertThat(savedPdu.deliusCode).isEqualTo(submittedPdu.pduCode)
    assertThat(savedPdu.deliusDescription).isEqualTo(submittedPdu.pduDescription)

    val savedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    val expectedDeliveryLocations = createDeliveryLocationPreferences.preferredDeliveryLocations
      .flatMap { pdu ->
        pdu.deliveryLocations.map { it.code to it.description }
      }.toSet()
    savedEntity.preferredDeliveryLocations.forEach { preferredDeliveryLocationEntity ->
      val actualPair = preferredDeliveryLocationEntity.deliusCode to preferredDeliveryLocationEntity.deliusDescription
      assertThat(actualPair).isIn(expectedDeliveryLocations)
    }
    assertThat(savedEntity.locationsCannotAttendText).isEqualTo(createDeliveryLocationPreferences.cannotAttendText)
  }

  @Test
  fun `return conflict when delivery preferences already exist`() {
    val referralEntity = ReferralEntityFactory()
      .produce()
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()
    val preferredDeliveryLocation = PreferredDeliveryLocationEntityFactory()
      .withPreferredDeliveryLocationProbationDeliveryUnit(pdu)
      .produce()
    val deliveryLocationPreference = DeliveryLocationPreferenceEntityFactory()
      .withReferral(referralEntity)
      .withPreferredDeliveryLocations(mutableSetOf(preferredDeliveryLocation))
      .produce()

    testDataGenerator.createReferralWithDeliveryLocationPreferences(
      referralEntity,
      pdu,
      preferredDeliveryLocation,
      deliveryLocationPreference,
    )
    assertThat(deliveryLocationPreferenceRepository.count()).isOne

    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.CONFLICT.value(),
    )

    assertThat(deliveryLocationPreferenceRepository.count()).isOne
  }

  @Test
  fun `create delivery preferences for referral that exists with existing pdu`() {
    val referralEntity = ReferralEntityFactory().produce()
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()

    testDataGenerator.createReferralWithDeliveryLocationPreferences(referralEntity, pdu)

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isOne

    val preferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory()
        .withPduCode(pdu.deliusCode)
        .withPduDescription(pdu.deliusDescription)
        .produce(),
    )
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(preferredDeliveryLocations)
      .produce()

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isOne

    val savedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(savedEntity.preferredDeliveryLocations).isNotEmpty()
    val expectedDeliveryLocations = createDeliveryLocationPreferences.preferredDeliveryLocations
      .flatMap { pdu ->
        pdu.deliveryLocations.map { it.code to it.description }
      }.toSet()
    savedEntity.preferredDeliveryLocations.forEach { preferredDeliveryLocationEntity ->
      val actualPair = preferredDeliveryLocationEntity.deliusCode to preferredDeliveryLocationEntity.deliusDescription
      assertThat(actualPair).isIn(expectedDeliveryLocations)
    }
    assertThat(savedEntity.locationsCannotAttendText).isEqualTo(createDeliveryLocationPreferences.cannotAttendText)
  }

  @Test
  fun `create delivery preferences for referral when no deliveryLocations are passed`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isZero

    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(mutableSetOf())
      .produce()

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isZero

    val savedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(savedEntity.preferredDeliveryLocations).isEmpty()
    assertThat(savedEntity.locationsCannotAttendText).isEqualTo(createDeliveryLocationPreferences.cannotAttendText)
  }

  @Test
  fun `when cannotAttendText is empty string convert to null and create Delivery Location Preferences`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withCannotAttendText("")
      .produce()

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isOne
    val savedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(savedEntity.locationsCannotAttendText).isNull()
  }

  @Test
  fun `should return bad request when preferredLocations - pduCode is null or blank`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val preferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory().withPduCode("").produce(),
    )
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(preferredDeliveryLocations)
      .produce()

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isZero

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  @Test
  fun `should return bad request when preferredLocations - pduDescription is null or blank`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val preferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory().withPduDescription("").produce(),
    )
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(preferredDeliveryLocations)
      .produce()

    assertThat(deliveryLocationProbationDeliveryUnitRepository.count()).isZero

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      body = createDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  @Test
  fun `should return 404 when referral does not exist`() {
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/delivery-location-preferences/referral/${UUID.randomUUID()}",
      body = createDeliveryLocationPreferences,
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return 401 when unauthorised request`() {
    val createDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    webTestClient
      .method(HttpMethod.POST)
      .uri("/delivery-location-preferences/referral/${UUID.randomUUID()}")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(createDeliveryLocationPreferences)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
