package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.UUID

class ReferralStatusControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
    stubAuthTokenEndpoint()
  }

  @Nested
  @DisplayName("Get possible transitions endpoint")
  inner class GetPossibleTransitions {
    @Test
    fun `should return list of possible referral status transitions for a given status id`() {
      // Given

      val allStatuses = referralStatusDescriptionRepository.findAll()
      assertThat(allStatuses).isNotEmpty()
      val suitableButNotReadyStatus = allStatuses.first { it.description == "Suitable but not ready" }

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-status/${suitableButNotReadyStatus.id}/transitions",
        returnType = object : ParameterizedTypeReference<List<ReferralStatus>>() {},
      )

      // Then
      assertThat(response).hasSize(4)
      assertThat(response.map { it.status }).containsExactlyInAnyOrder("Awaiting assessment", "Deprioritised", "Recall", "Return to court")
      assertThat(response.map { it.transitionDescription }).containsExactlyInAnyOrder(
        "The person’s suitability or readiness has changed. The programme team will reassess them.",
        "The person is suitable but does not meet the prioritisation criteria. The referral will be paused in case they are reprioritised.",
        "The person has been recalled. Depending on the recall type, the referral may be withdrawn or returned to awaiting assessment.",
        "The person is not suitable for the programme or cannot continue with it. The referral will be returned to court.",
      )
    }

    @Test
    fun `Should return empty list when referral status description does not exist`() {
      // Given
      val nonExistentId = UUID.randomUUID()

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/referral-status/$nonExistentId/transitions",
        returnType = object : ParameterizedTypeReference<List<ReferralStatus>>() {},
      )

      // Then
      assertThat(response).isEmpty()
    }
  }

  @Nested
  @DisplayName("Get possible transitions endpoint")
  inner class GetReferralStatusForm {
    @Test
    fun `should return the status data for the ui form based on a referral id`() {
      // Given
      val referralStatusDescriptionEntity = referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()
      val referral = ReferralEntityFactory().produce()
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, referralStatusDescriptionEntity)

      testDataGenerator.createReferralWithStatusHistory(referral, statusHistory)

      // When
      val response = performRequestAndExpectOk(
        httpMethod = HttpMethod.GET,
        uri = "/bff/referral-status-form/${referral.id}",
        returnType = object : ParameterizedTypeReference<ReferralStatusFormData>() {},
      )

      // Then
      assertThat(response).isNotNull
      assertThat(response.currentStatus.title).isEqualTo("Awaiting assessment")
      assertThat(response.availableStatuses).isNotEmpty
    }

    @Test
    fun `should return empty list when referral status description does not exist`() {
      // Given
      val nonExistentId = UUID.randomUUID()

      // When
      performRequestAndExpectStatus(
        httpMethod = HttpMethod.GET,
        uri = "/bff/referral-status-form/$nonExistentId",
        returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
        expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
      )
    }
  }
}
