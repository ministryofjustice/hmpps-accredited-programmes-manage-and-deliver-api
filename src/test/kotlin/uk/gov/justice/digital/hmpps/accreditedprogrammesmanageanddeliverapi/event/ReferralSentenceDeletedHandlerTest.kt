package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.DomainEventsMessageFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.MessageHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReferralSentenceDeletedHandlerTest {

  private val objectMapper = jacksonMapperBuilder().addModule(JavaTimeModule()).build()

  @Mock
  private lateinit var messageHistoryRepository: MessageHistoryRepository

  @Mock
  private lateinit var referralService: ReferralService

  @Mock
  private lateinit var telemetryService: TelemetryService

  private lateinit var handler: ReferralSentenceDeletedHandler

  @BeforeEach
  fun beforeEach() {
    handler = ReferralSentenceDeletedHandler(
      objectMapper = objectMapper,
      messageHistoryRepository = messageHistoryRepository,
      referralService = referralService,
      telemetryService = telemetryService,
    )
  }

  @Test
  fun `should handle a message successfully`() {
    // Given
    val crn = "X123456"
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withEventType("probation-case.registration.deleted")
      .withPersonReference(PersonReference.fromCrn(crn))
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryService).logToAppInsights(
      eq("Referral.sentence-deleted-event-received.success"),
      anyMap(),
    )
    verify(messageHistoryRepository).save(any())
    verify(referralService).deleteReferralByCaseReferenceNumber(crn)
    verify(telemetryService).logToAppInsights(
      eq("Referral.sentence-deleted-event-processed.success"),
      anyMap(),
    )
  }

  @Test
  fun `should handle a message with no CRN`() {
    // Given
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withPersonReference(PersonReference(listOf()))
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    // When
    handler.handle(sqsMessage)

    // Then
    verify(telemetryService).logToAppInsights(
      eq("Referral.sentence-deleted-event-processed.failure"),
      anyMap(),
    )
    verify(messageHistoryRepository, times(0)).save(any())
    verify(referralService, times(0)).deleteReferralByCaseReferenceNumber(anyString())
  }

  @Test
  fun `should log failure and throw exception when an error occurs`() {
    // Given
    val crn = "X123456"
    val messageId = UUID.randomUUID()
    val domainEventsMessage = DomainEventsMessageFactory()
      .withPersonReference(PersonReference.fromCrn(crn))
      .produce()
    val sqsMessage = SQSMessage(messageId = messageId, message = objectMapper.writeValueAsString(domainEventsMessage))

    val exception = RuntimeException("Something went wrong")
    `when`(messageHistoryRepository.save(any())).thenThrow(exception)

    // When & Then
    assertThrows(RuntimeException::class.java) {
      handler.handle(sqsMessage)
    }

    verify(telemetryService).logToAppInsights(
      eq("Referral.sentence-deleted-event-processed.failure"),
      anyMap(),
    )
  }
}
