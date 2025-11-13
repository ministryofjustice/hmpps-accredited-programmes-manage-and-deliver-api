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
