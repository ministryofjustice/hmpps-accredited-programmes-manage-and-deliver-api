package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.publish

@Service
class DomainEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("hmppseventtopic") ?: throw IllegalStateException("hmppseventtopic not found")
  }

  fun publish(hmppsDomainEvent: DomainEventsMessage) {
    domainEventsTopic.publish(hmppsDomainEvent.eventType, objectMapper.writeValueAsString(hmppsDomainEvent))
  }
}
