package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.microsoft.applicationinsights.TelemetryClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReferralDetailsUpdatedHandlerTest {

  private val objectMapper = jacksonMapperBuilder().addModule(JavaTimeModule()).build()

  @Mock
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Mock
  private lateinit var referralService: ReferralService

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  private lateinit var handler: ReferralDetailsUpdatedHandler

  @BeforeEach
  fun beforeEach() {
    handler = ReferralDetailsUpdatedHandler(
      objectMapper = objectMapper,
      messageHistoryRepository = messageHistoryRepository,
      referralService = referralService,
      telemetryClient = telemetryClient,
    )
  }

  @Test
  fun `should handle a message with no referral ID`() {
    // Given
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("accredited-programmes-manage-and-deliver.referral.details-updated")
      .withAdditionalInformation(mapOf())
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryClient).trackEvent(any(), any(), anyOrNull())
    verify(messageHistoryRepository, times(0)).save(any())
    runBlocking {
      verify(referralService, times(0)).refreshPersonalDetailsForReferral(any(), any())
    }
  }

  @Test
  fun `should handle a message with a referral ID`() {
    // Given
    val referralId = UUID.randomUUID()
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("accredited-programmes-manage-and-deliver.referral.details-updated")
      .withAdditionalInformation(mapOf("referralId" to referralId.toString()))
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    runBlocking {
      `when`(referralService.refreshPersonalDetailsForReferral(referralId, false)).thenReturn(mock())
    }

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryClient, times(2)).trackEvent(any(), any(), anyOrNull())
    verify(messageHistoryRepository).save(any())
    runBlocking {
      verify(referralService).refreshPersonalDetailsForReferral(referralId, false)
    }
  }

  @Test
  fun `should handle a message when referral service returns null`() {
    // Given
    val referralId = UUID.randomUUID()
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("accredited-programmes-manage-and-deliver.referral.details-updated")
      .withAdditionalInformation(mapOf("referralId" to referralId.toString()))
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    runBlocking {
      `when`(referralService.refreshPersonalDetailsForReferral(referralId, false)).thenReturn(null)
    }

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryClient, times(2)).trackEvent(any(), any(), anyOrNull())
    verify(messageHistoryRepository).save(any())
    runBlocking {
      verify(referralService).refreshPersonalDetailsForReferral(referralId, false)
    }
  }
}
