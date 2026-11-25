package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.scheduled

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.ACP_M_AND_D_FACILITATOR_NAMES_UPDATED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.FacilitatorService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

@Component
class FacilitatorNamesUpdatedJob(
  private val domainEventPublisher: DomainEventPublisher,
  private val telemetryService: TelemetryService,
  private val facilitatorService: FacilitatorService,
  @Value("\${app.scheduling.facilitator-names-updated.chunk-size:100}") private val pageSize: Int,
  private val clock: Clock,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(cron = "\${app.scheduling.facilitator-names-updated.cron}")
  @SchedulerLock(
    name = "facilitatorNameUpdateJob",
    lockAtMostFor = "\${app.scheduling.facilitator-names-updated.shedlock.lock-most:PT2H}",
    lockAtLeastFor = "\${app.scheduling.facilitator-names-updated.shedlock.lock-least:PT1H}",
  )
  fun process() {
    val now = clock.instant()
    log.info("Facilitator names updated job started, at: $now")
    var totalUpdated = 0
    var currentPage = 1
    var hasMoreRecords = true

    while (hasMoreRecords) {
      val pageable: Pageable = PageRequest.of(currentPage - 1, pageSize)
      val facilitatorPage: Page<FacilitatorEntity> = facilitatorService.findAllWherePersonSurnameIsNull(pageable)
      val facilitators: MutableList<FacilitatorEntity> = facilitatorPage.content

      facilitators.forEach { facilitator ->
        val facilitatorId = facilitator.id!!
        val message = getDomainEventMessage(facilitatorId, facilitator.ndeliusPersonCode)
        log.info("Publishing ${ACP_M_AND_D_FACILITATOR_NAMES_UPDATED.value} event for facilitatorId: $facilitatorId")
        try {
          publishEvent(message, facilitatorId)
          totalUpdated++
        } catch (exception: Exception) {
          log.error(
            "Failed to publish event updating facilitator names for facilitatorId: $facilitatorId, skipping and continuing",
            exception,
          )
        }
      }

      hasMoreRecords = facilitatorPage.hasNext()
      currentPage++
    }

    val endTime = clock.instant()
    log.info(
      "Facilitator names updated Job completed: updates sent: $totalUpdated, time took: ${
        Duration.between(
          now,
          endTime,
        )
      }",
    )
  }

  private fun getDomainEventMessage(
    facilitatorId: UUID,
    nDeliusPersonCode: String,
  ): DomainEventsMessage = DomainEventsMessage(
    eventType = ACP_M_AND_D_FACILITATOR_NAMES_UPDATED.value,
    version = 1,
    occurredAt = ZonedDateTime.now(clock),
    description = "An Accredited Programmes facilitator names in community have been updated.",
    additionalInformation = mapOf("facilitatorId" to facilitatorId.toString()),
    personReference = PersonReference.fromNDeliusPersonCode(nDeliusPersonCode),
  )

  private fun publishEvent(
    domainEventsMessage: DomainEventsMessage,
    facilitatorId: UUID,
  ) {
    try {
      domainEventPublisher.publish(domainEventsMessage)
      log.info("Successfully published ${ACP_M_AND_D_FACILITATOR_NAMES_UPDATED.value} event for facilitatorId: $facilitatorId")
      telemetryService.logToAppInsights(
        eventName = "Facilitator.names-updated-event-published.success",
        properties = mapOf(
          "facilitatorId" to facilitatorId.toString(),
          "outcome" to "success",
        ),
      )
    } catch (exception: Exception) {
      log.error(
        "Unsuccessfully published ${ACP_M_AND_D_FACILITATOR_NAMES_UPDATED.value} event for facilitatorId: $facilitatorId with exception: ${exception.message}",
        exception,
      )
      telemetryService.logToAppInsights(
        eventName = "Facilitator.names-updated-event-published.failure",
        properties = mapOf(
          "facilitatorId" to facilitatorId.toString(),
          "outcome" to "failure",
        ),
      )

      throw exception
    }
  }
}
