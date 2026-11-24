package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.FacilitatorService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FacilitatorNamesUpdatedHandlerTest {

  private val objectMapper = jacksonMapperBuilder().addModule(JavaTimeModule()).build()

  @Mock
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Mock
  private lateinit var facilitatorService: FacilitatorService

  @Mock
  private lateinit var telemetryService: TelemetryService

  private lateinit var handler: FacilitatorNamesUpdatedHandler

  @BeforeEach
  fun beforeEach() {
    handler = FacilitatorNamesUpdatedHandler(
      objectMapper = objectMapper,
      messageHistoryRepository = messageHistoryRepository,
      facilitatorService = facilitatorService,
      telemetryService = telemetryService,
    )
  }

  @Test
  fun `should handle a message with no facilitator ID`() {
    // Given
    val sqsMessage = sqsMessage()

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.names-updated-event-processed.failure"),
      anyMap(),
    )
    verify(messageHistoryRepository, times(0)).save(any())
    runBlocking {
      verify(facilitatorService, times(0)).updateNameByFacilitatorId(any())
    }
  }

  @Test
  fun `should handle a message with a facilitator ID`() {
    // Given
    val facilitatorId = UUID.randomUUID()
    val sqsMessage = sqsMessage(facilitatorId)
    val facilitator = FacilitatorEntityFactory().withId(facilitatorId).produce()

    runBlocking {
      `when`(facilitatorService.updateNameByFacilitatorId(facilitatorId)).thenReturn(facilitator)
    }

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.details-updated-event-received.success"),
      anyMap(),
    )
    verify(messageHistoryRepository).save(any())
    runBlocking {
      verify(facilitatorService).updateNameByFacilitatorId(facilitatorId)
    }
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.names-updated-event-processed.success"),
      anyMap(),
    )
  }

  @Test
  fun `should handle a message when facilitator service returns null`() {
    // Given
    val facilitatorId = UUID.randomUUID()
    val sqsMessage = sqsMessage(facilitatorId)

    runBlocking {
      `when`(facilitatorService.updateNameByFacilitatorId(facilitatorId)).thenReturn(null)
    }

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.details-updated-event-received.success"),
      anyMap(),
    )
    verify(messageHistoryRepository).save(any())
    runBlocking {
      verify(facilitatorService).updateNameByFacilitatorId(facilitatorId)
    }
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.names-updated-event-processed.failure"),
      anyMap(),
    )
  }

  @Test
  fun `should log failure and throw exception when facilitator ID is invalid`() {
    // Given
    val sqsMessage = sqsMessage("not-a-uuid")

    // When & Then
    assertThrows(IllegalArgumentException::class.java) {
      handler.handle(sqsMessage)
    }

    verify(messageHistoryRepository).save(any())
    verify(telemetryService).logToAppInsights(
      eq("Facilitator.names-updated-event-processed.failure"),
      anyMap(),
    )
  }

  @Test
  fun `should log failure and throw exception when an error occurs`() {
    // Given
    val sqsMessage = sqsMessage(UUID.randomUUID())
    val exception = RuntimeException("Something went wrong")
    `when`(messageHistoryRepository.save(any())).thenThrow(exception)

    // When & Then
    assertThrows(RuntimeException::class.java) {
      handler.handle(sqsMessage)
    }

    verify(telemetryService).logToAppInsights(
      eq("Facilitator.names-updated-event-processed.failure"),
      anyMap(),
    )
  }

  private fun sqsMessage(facilitatorId: UUID? = null): SQSMessage {
    val additionalInformation = facilitatorId?.let { mapOf("facilitatorId" to it.toString()) } ?: mapOf()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("accredited-programmes-manage-and-deliver.facilitator.names-updated")
      .withAdditionalInformation(additionalInformation)
      .produce()
    return SQSMessage(
      messageId = UUID.randomUUID(),
      message = objectMapper.writeValueAsString(domainEventsMessage),
    )
  }

  private fun sqsMessage(facilitatorId: String): SQSMessage {
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("accredited-programmes-manage-and-deliver.facilitator.names-updated")
      .withAdditionalInformation(mapOf("facilitatorId" to facilitatorId))
      .produce()
    return SQSMessage(
      messageId = UUID.randomUUID(),
      message = objectMapper.writeValueAsString(domainEventsMessage),
    )
  }
}
