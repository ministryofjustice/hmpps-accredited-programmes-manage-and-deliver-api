package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralOnProgrammeStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralCohortHistoryFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDateTime

class ReferralStatusControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  private val readOnlyRole = "ROLE_ACCREDITED_PROGRAMMES__MANAGE_AND_DELIVER_OCI_READ_ONLY"

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    stubAuthTokenEndpoint()
    probationAccessControlApiStubs.stubOpenAccessForAnyCrn()
  }

  @Nested
  @DisplayName("Get on-programme status endpoint")
  inner class GetOnProgrammeStatus {
    @Test
    fun `should return 200 with on-programme status when referral exists`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

      testDataGenerator.createReferralWithFields(
        referralEntity,
        listOf(statusHistory, cohortHistory),
      )

      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/${savedReferral.crn}/latest-status")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(readOnlyRole)))
        .exchange()
        .expectStatus().isOk
        .expectBody(ReferralOnProgrammeStatus::class.java)
        .returnResult().responseBody!!

      // Then
      assertThat(response.referralId).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)
      assertThat(response.sentenceType).isEqualTo(savedReferral.sourcedFrom?.name)
      assertThat(response.eventId).isEqualTo(savedReferral.eventId)
      assertThat(response.eventNumber).isEqualTo(savedReferral.eventNumber)
      assertThat(response.currentStatus).isEqualTo("Awaiting assessment")
      assertThat(response.isOnProgramme).isFalse()
    }

    @Test
    fun `should return 200 with correct on-programme status when allocated to group`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getOnProgrammeStatusDescription(),
        )
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

      testDataGenerator.createReferralWithFields(
        referralEntity,
        listOf(statusHistory, cohortHistory),
      )

      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/${savedReferral.crn}/latest-status")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(readOnlyRole)))
        .exchange()
        .expectStatus().isOk
        .expectBody(ReferralOnProgrammeStatus::class.java)
        .returnResult().responseBody!!

      // Then
      assertThat(response.referralId).isEqualTo(savedReferral.id)
      assertThat(response.crn).isEqualTo(savedReferral.crn)
      assertThat(response.currentStatus).isEqualTo("On programme")
      assertThat(response.isOnProgramme).isTrue()
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      // Given
      val nonExistentCrn = "X999999"

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/$nonExistentCrn/latest-status")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(readOnlyRole)))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when unauthorised`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(
        referralEntity,
        referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
      )
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

      testDataGenerator.createReferralWithFields(
        referralEntity,
        listOf(statusHistory, cohortHistory),
      )

      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When & Then
      webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/${savedReferral.crn}/latest-status")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG_ROLE")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 200 with latest status when multiple status updates exist`() {
      // Given
      val referralEntity = ReferralEntityFactory().produce()
      val earlierStatus = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 15, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
        )
      val latestStatus = ReferralStatusHistoryEntityFactory()
        .withCreatedAt(LocalDateTime.of(2025, 9, 24, 16, 0))
        .produce(
          referralEntity,
          referralStatusDescriptionRepository.getOnProgrammeStatusDescription(),
        )
      val cohortHistory = ReferralCohortHistoryFactory().withReferral(referralEntity).produce()

      testDataGenerator.createReferralWithFields(
        referralEntity,
        listOf(earlierStatus, cohortHistory),
      )
      testDataGenerator.createReferralStatusHistory(latestStatus)

      val savedReferral = referralRepository.findByCrn(referralEntity.crn)[0]

      // When
      val response = webTestClient
        .method(HttpMethod.GET)
        .uri("/referral/${savedReferral.crn}/latest-status")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(readOnlyRole)))
        .exchange()
        .expectStatus().isOk
        .expectBody(ReferralOnProgrammeStatus::class.java)
        .returnResult().responseBody!!

      // Then
      assertThat(response.currentStatus).isEqualTo("On programme")
      assertThat(response.isOnProgramme).isTrue()
    }
  }
}
