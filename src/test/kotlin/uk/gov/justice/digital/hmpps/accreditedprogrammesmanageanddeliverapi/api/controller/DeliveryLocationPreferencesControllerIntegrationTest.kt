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

  @Test
  fun `should update delivery preferences for referral that exists with existing preferences`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()
    val preferredDeliveryLocation = PreferredDeliveryLocationEntityFactory()
      .withPreferredDeliveryLocationProbationDeliveryUnit(pdu)
      .produce()
    val deliveryLocationPreference = DeliveryLocationPreferenceEntityFactory()
      .withReferral(referralEntity)
      .withPreferredDeliveryLocations(mutableSetOf(preferredDeliveryLocation))
      .withLocationsCannotAttendText("Original text")
      .produce()

    testDataGenerator.createReferralWithDeliveryLocationPreferences(
      referralEntity,
      pdu,
      preferredDeliveryLocation,
      deliveryLocationPreference,
    )
    assertThat(deliveryLocationPreferenceRepository.count()).isOne

    val updatedDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withCannotAttendText("Updated text")
      .produce()

    // When
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = updatedDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    assertThat(deliveryLocationPreferenceRepository.count()).isOne

    val updatedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(updatedEntity.locationsCannotAttendText).isEqualTo("Updated text")

    val expectedDeliveryLocations = updatedDeliveryLocationPreferences.preferredDeliveryLocations
      .flatMap { pdu ->
        pdu.deliveryLocations.map { it.code to it.description }
      }.toSet()
    updatedEntity.preferredDeliveryLocations.forEach { preferredDeliveryLocationEntity ->
      val actualPair = preferredDeliveryLocationEntity.deliusCode to preferredDeliveryLocationEntity.deliusDescription
      assertThat(actualPair).isIn(expectedDeliveryLocations)
    }
  }

  @Test
  fun `should update delivery preferences with different delivery locations and PDUs`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    val originalPdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory()
      .withDeliusCode("ORIGINAL_PDU")
      .withDeliusDescription("Original PDU")
      .produce()
    val originalPreferredDeliveryLocation = PreferredDeliveryLocationEntityFactory()
      .withPreferredDeliveryLocationProbationDeliveryUnit(originalPdu)
      .withDeliusCode("ORIG_LOC")
      .withDeliusDescription("Original Location")
      .produce()
    val originalDeliveryLocationPreference = DeliveryLocationPreferenceEntityFactory()
      .withReferral(referralEntity)
      .withPreferredDeliveryLocations(mutableSetOf(originalPreferredDeliveryLocation))
      .produce()

    testDataGenerator.createReferralWithDeliveryLocationPreferences(
      referralEntity,
      originalPdu,
      originalPreferredDeliveryLocation,
      originalDeliveryLocationPreference,
    )

    val newPreferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory()
        .withPduCode("NEW_PDU")
        .withPduDescription("New PDU")
        .produce(),
    )
    val updatedDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(newPreferredDeliveryLocations)
      .produce()

    // When
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = updatedDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    val updatedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    val expectedDeliveryLocations = updatedDeliveryLocationPreferences.preferredDeliveryLocations
      .flatMap { pdu ->
        pdu.deliveryLocations.map { it.code to it.description }
      }.toSet()
    updatedEntity.preferredDeliveryLocations.forEach { preferredDeliveryLocationEntity ->
      val actualPair = preferredDeliveryLocationEntity.deliusCode to preferredDeliveryLocationEntity.deliusDescription
      assertThat(actualPair).isIn(expectedDeliveryLocations)
    }

    val newPdu = deliveryLocationProbationDeliveryUnitRepository.findByDeliusCode("NEW_PDU")!!
    assertThat(newPdu.deliusDescription).isEqualTo("New PDU")
  }

  @Test
  fun `should update delivery preferences with empty cannotAttendText`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    val pdu = PreferredDeliveryLocationProbationDeliveryUnitEntityFactory().produce()
    val preferredDeliveryLocation = PreferredDeliveryLocationEntityFactory()
      .withPreferredDeliveryLocationProbationDeliveryUnit(pdu)
      .produce()
    val deliveryLocationPreference = DeliveryLocationPreferenceEntityFactory()
      .withReferral(referralEntity)
      .withPreferredDeliveryLocations(mutableSetOf(preferredDeliveryLocation))
      .withLocationsCannotAttendText("Some original text")
      .produce()

    testDataGenerator.createReferralWithDeliveryLocationPreferences(
      referralEntity,
      pdu,
      preferredDeliveryLocation,
      deliveryLocationPreference,
    )

    val updatedDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withCannotAttendText(null)
      .produce()

    // When
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = updatedDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    val updatedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(updatedEntity.locationsCannotAttendText).isNull()
  }

  @Test
  fun `should update delivery preferences with no delivery locations`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
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

    val updatedDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(mutableSetOf())
      .produce()

    // When
    performRequestAndExpectStatus(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = updatedDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    val updatedEntity = deliveryLocationPreferenceRepository.findByReferralId(referralEntity.id!!)!!
    assertThat(updatedEntity.preferredDeliveryLocations).isEmpty()
  }

  @Test
  fun `should return 404 when updating delivery preferences for non-existing referral`() {
    // Given
    val updateDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${UUID.randomUUID()}",
      body = updateDeliveryLocationPreferences,
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return 404 when updating delivery preferences that do not exist for referral`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val updateDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      body = updateDeliveryLocationPreferences,
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return bad request when updating with blank pduCode`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
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

    val preferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory().withPduCode("").produce(),
    )
    val updateDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(preferredDeliveryLocations)
      .produce()

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      body = updateDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  @Test
  fun `should return bad request when updating with blank pduDescription`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()
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

    val preferredDeliveryLocations = mutableSetOf(
      PreferredDeliveryLocationsFactory().withPduDescription("").produce(),
    )
    val updateDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory()
      .withPreferredDeliveryLocations(preferredDeliveryLocations)
      .produce()

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/delivery-location-preferences/referral/${referralEntity.id}",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      body = updateDeliveryLocationPreferences,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  @Test
  fun `should return 401 when unauthorised update request`() {
    // Given
    val updateDeliveryLocationPreferences = CreateDeliveryLocationPreferencesFactory().produce()

    // When & Then
    webTestClient
      .method(HttpMethod.PUT)
      .uri("/delivery-location-preferences/referral/${UUID.randomUUID()}")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(updateDeliveryLocationPreferences)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
