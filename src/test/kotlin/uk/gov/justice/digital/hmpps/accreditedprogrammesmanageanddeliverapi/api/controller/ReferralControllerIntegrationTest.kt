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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Referral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.*

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should successfully retrieve a referral for a known referral id`() {
    // Given
    val referralEntity = ReferralEntityFactory().produce()

    val referralStatusHistoryEntity = ReferralStatusHistoryEntityFactory()
      .withStatus("Created")
      .withEndDate(null)
      .produce()

    testDataGenerator.createReferralWithStatusHistory(referralEntity, referralStatusHistoryEntity)

    val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

    // When
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/referral/${savedReferral.id}",
      object : ParameterizedTypeReference<Referral>() {},
    )

    // Then
    assertThat(response).isNotNull
    assertThat(response.crn).isEqualTo(referralEntity.crn)
    assertThat(response.id).isEqualTo(referralEntity.id)
    assertThat(response.personName).isEqualTo(referralEntity.personName)
    assertThat(response.status).isEqualTo("Created")
  }

  @Test
  fun `should return HTTP 404 not found when retrieving a referral for an unknown referral id`() {
    // Given
    val referralId = UUID.randomUUID()

    // When & Then
    performRequestAndExpectStatus(
      HttpMethod.GET,
      "/referral/$referralId",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return HTTP 403 Forbidden when retrieving a referral without the appropriate role`() {
    // Given
    val referralId = UUID.randomUUID()

    // When & Then
    webTestClient
      .method(HttpMethod.GET)
      .uri("/referral/$referralId")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
