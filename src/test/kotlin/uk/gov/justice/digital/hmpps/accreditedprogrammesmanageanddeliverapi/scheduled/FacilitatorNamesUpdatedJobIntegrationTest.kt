package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.scheduled

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.awaitility.kotlin.withPollDelay
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import java.time.Duration.ofMillis

class FacilitatorNamesUpdatedJobIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var job: FacilitatorNamesUpdatedJob

  @Autowired
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @BeforeEach
  fun setUp() {
    domainEventsQueueConfig.purgeAllQueues()
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should process facilitator names updated job`() {
    // Given
    val facilitatorEntity = FacilitatorEntityFactory().produce()
    val savedFacilitatorEntity = testDataGenerator.createFacilitator(facilitatorEntity)
    assertThat(savedFacilitatorEntity.personSurname).isNull()
    val facilitatorId = facilitatorEntity.id!!

    // When
    job.process()

    // Then
    await withPollDelay ofMillis(100) untilCallTo { with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() } } matches { it == 0 }
    await untilCallTo {
      messageHistoryRepository.findAll().firstOrNull()
    } matches { it != null }

    messageHistoryRepository.findAll().first().let {
      assertThat(it.id).isNotNull
      assertThat(it.eventType).isEqualTo("accredited-programmes-manage-and-deliver.facilitator.names-updated")
      assertThat(it.description).isEqualTo("An Accredited Programmes facilitator names in community have been updated.")
      assertThat(it.message).contains("\"additionalInformation\":{\"facilitatorId\":\"$facilitatorId\"}")
    }
  }

  @Test
  fun `should not publish events when no facilitators are stale`() {
    // Given
    val facilitatorEntity = FacilitatorEntityFactory().withPersonSurname("Smith").produce()
    val savedFacilitatorEntity = testDataGenerator.createFacilitator(facilitatorEntity)
    assertThat(savedFacilitatorEntity.personSurname).isNotNull()

    // When
    job.process()

    // Then
    assertThat(
      with(domainEventsQueueConfig) { domainEventQueue.countAllMessagesOnQueue() },
    ).isEqualTo(0)
    assertThat(messageHistoryRepository.findAll()).isEmpty()
  }
}
