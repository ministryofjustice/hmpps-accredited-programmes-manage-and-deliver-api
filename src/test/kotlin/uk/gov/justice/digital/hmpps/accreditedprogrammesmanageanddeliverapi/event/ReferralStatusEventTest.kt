package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo.Status
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.Duration.ofMillis

class ReferralStatusEventTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Test
  fun `publish a status change event and retrieve the details via rest endpoint`() {
    // Creates referral and moves to awaiting allocation status
    val awaitingAllocationStatus = referralStatusDescriptionRepository.getAwaitingAllocationStatusDescription()
    val referral = testReferralHelper.createReferral()
    testReferralHelper.updateReferralStatus(referral, awaitingAllocationStatus, "TEST ADDITIONAL DETAILS")

    // Wait for message to be processed
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { interventionsQueue.countAllMessagesOnQueue() } } matches { it == 1 }
    val eventBody = objectMapper.readValue<SQSMessage>(
      with(domainEventsQueueConfig) {
        interventionsQueue.receiveMessageOnQueue().body()
      },
    )
    assertThat(eventBody.eventType).isEqualTo(HmppsDomainEventTypes.ACP_COMMUNITY_REFERRAL_STATUS_UPDATED.value)

    val hmppsDomainEvent: DomainEventsMessage = objectMapper.readValue(eventBody.message)

    val extractUrlPath = hmppsDomainEvent.detailUrl!!.substringAfter("localhost:8080")

    // Make request for details
    val response = performRequestAndExpectOk(
      httpMethod = HttpMethod.GET,
      uri = extractUrlPath,
      returnType = object : ParameterizedTypeReference<ReferralStatusInfo>() {},
    )

    assertThat(response).isNotNull
    assertThat(response.notes).isEqualTo("TEST ADDITIONAL DETAILS")
    assertThat(response.sourcedFromEntityType).isEqualTo(ReferralEntitySourcedFrom.LICENCE_CONDITION)
    assertThat(response.sourcedFromEntityId).isEqualTo(referral.eventId!!.toLong())

    assertThat(response.newStatus).isEqualTo(Status.AWAITING_ALLOCATION)
  }
}
