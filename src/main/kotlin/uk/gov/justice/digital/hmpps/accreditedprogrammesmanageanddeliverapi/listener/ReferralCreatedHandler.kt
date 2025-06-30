package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SQSMessageHistoryRepository

@Service
class ReferralCreatedHandler(
  private val objectMapper: ObjectMapper,
  private val sqsMessageHistoryRepository: SQSMessageHistoryRepository,

) {
  fun handle(domainEventMessage: DomainEventsMessage) {
    val sqsMessageHistoryEntity = DomainEventsMessage.toEntity(domainEventMessage, objectMapper)
    sqsMessageHistoryRepository.save(sqsMessageHistoryEntity)
  }
}
