package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository

class StatusChangeDetailsControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Test
  fun `should return 200 response and ReferralStatusInfo`() {
    // Given
    // Creates referral and moves to awaiting allocation status
    val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val referral = testReferralHelper.createReferral()
    testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus, "TEST ADDITIONAL DETAILS")

    // When
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = "/referral/${referral.id}/status-change-details",
      returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
    )

    // Then
    assertThat(response).isNotNull
    assertThat(response.newStatus).isEqualTo(ReferralStatusInfo.Status.AWAITING_ALLOCATION)
    assertThat(response.sourcedFromEntityId).isEqualTo(referral.eventId!!.toLong())
    assertThat(response.sourcedFromEntityType).isEqualTo(referral.sourcedFrom)
    assertThat(response.notes).isEqualTo("TEST ADDITIONAL DETAILS")
    assertThat(response.description).isEqualTo("The person is ready to be allocated to a programme group.")
  }

  @Test
  fun `should return 401 when not authorized`() {
    // Given
    // Creates referral and moves to awaiting allocation status
    val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val referral = testReferralHelper.createReferralAndUpdateStatus(awaitingAllocationStatus)

    // When & Then
    webTestClient
      .method(HttpMethod.GET)
      .uri("/referral/${referral.id}/status-change-details")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
