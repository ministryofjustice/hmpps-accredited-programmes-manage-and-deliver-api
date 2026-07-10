package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.job

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.DomainEventPublisher
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.ACP_M_AND_D_REFERRAL_DETAILS_UPDATED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.PersonReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralService
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

@Component
class ReferralDetailsUpdatedJob(
  private val domainEventPublisher: DomainEventPublisher,
  private val telemetryClient: TelemetryClient,
  private val referralService: ReferralService,
  @Value("\${services.manage-and-deliver-api.base-url}") private val madBaseUrl: String,
  @Value("\${app.scheduling.referral-details-updated.chunk-size:100}") private val pageSize: Int,
  @Value("\${app.scheduling.referral-details-updated.cutoff:432000}") private val cutoff: Long,
  private val clock: Clock,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(cron = "\${app.scheduling.referral-details-updated.cron}")
  fun process() {
    val now = clock.instant()
    log.info("Referral details updated Job started: at $now")
    var totalUpdated = 0
    val cutoffTime = LocalDateTime.now(clock).minusSeconds(cutoff)
    var currentPage = 1
    var hasMoreRecords = true

    while (hasMoreRecords) {
      val pageable: Pageable = PageRequest.of(currentPage - 1, pageSize)
      val referralPage: Page<ReferralEntity> = referralService.findAllByUpdatedAtBefore(cutoffTime, pageable)
      val referrals: MutableList<ReferralEntity> = referralPage.content

      referrals.forEach { referral ->
        val referralId = referral.id!!
        val message = getDomainEventMessage(referralId, referral.crn)
        log.info("Publishing ${ACP_M_AND_D_REFERRAL_DETAILS_UPDATED.value} event for referralId: $referralId")
        try {
          publishEvent(message, referralId)
          totalUpdated++
        } catch (exception: Exception) {
          log.error("Failed to publish event for referralId: $referralId, skipping and continuing", exception)
        }
      }

      hasMoreRecords = referralPage.hasNext()
      currentPage++
    }

    val endTime = clock.instant()
    log.info(
      "Referral details updated Job completed: updates sent: $totalUpdated, time took: ${
        Duration.between(
          now,
          endTime,
        )
      }",
    )
  }

  private fun getDomainEventMessage(
    referralId: UUID,
    crn: String,
  ): DomainEventsMessage = DomainEventsMessage(
    eventType = ACP_M_AND_D_REFERRAL_DETAILS_UPDATED.value,
    version = 1,
    detailUrl = "$madBaseUrl/referral-details/$referralId/personal-details",
    occurredAt = ZonedDateTime.now(clock),
    description = "An Accredited Programmes referral details in community have been updated.",
    additionalInformation = mapOf("referralId" to referralId.toString()),
    personReference = PersonReference.fromCrn(crn),
  )

  private fun publishEvent(
    domainEventsMessage: DomainEventsMessage,
    referralId: UUID,
  ) {
    try {
      domainEventPublisher.publish(domainEventsMessage)
      log.info("Successfully published ${ACP_M_AND_D_REFERRAL_DETAILS_UPDATED.value} event for referralId: $referralId")
      telemetryClient.logToAppInsights(
        "Referral.details-updated-event-published.success",
        mapOf(
          "referralId" to referralId.toString(),
          "outcome" to "success",
        ),
      )
    } catch (exception: Exception) {
      log.error(
        "Unsuccessfully published ${ACP_M_AND_D_REFERRAL_DETAILS_UPDATED.value} event for referralId: $referralId with exception: ${exception.message}",
        exception,
      )
      telemetryClient.logToAppInsights(
        "Referral.details-updated-event-published.failure",
        mapOf(
          "referralId" to referralId.toString(),
          "outcome" to "failure",
        ),
      )

      throw exception
    }
  }
}
