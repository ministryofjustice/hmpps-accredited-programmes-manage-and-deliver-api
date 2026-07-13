package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.scheduled

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.Duration.ofMillis

class ReferralDetailsUpdatedJobIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var job: ReferralDetailsUpdatedJob

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Value("\${services.manage-and-deliver-api.base-url}")
  private lateinit var madBaseUrl: String

  @BeforeEach
  fun setUp() {
    domainEventsQueueConfig.purgeAllQueues()
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should process referral details updated job`() {
    // Given
    val savedReferral = testReferralHelper.createReferral()
    val referralId = savedReferral.id

    // When
    runBlocking {
      delay(1001) // this is required for savedReferral.updatedAt to be older than 1 second from now
    }
    job.process()

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo("accredited-programmes-manage-and-deliver.referral.details-updated")
      assertThat(it.detailUrl).isEqualTo("$madBaseUrl/referral-details/$referralId/personal-details")
      assertThat(it.description).isEqualTo("An Accredited Programmes referral details in community have been updated.")
      assertThat(it.message).contains("\"additionalInformation\":{\"referralId\":\"$referralId\"}")
    }
  }

  @Test
  fun `should not publish events when no referrals are stale`() {
    // Given
    testReferralHelper.createReferral()

    // When
    job.process()

    // Then
    assertThat(
      with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() },
    ).isEqualTo(0)
    assertThat(messageHistoryRepository.findAll()).isEmpty()
  }
}
