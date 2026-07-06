package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.INTERVENTIONS_COMMUNITY_REFERRAL_CREATED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.INTERVENTIONS_COMMUNITY_REFERRAL_IMPORTED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.PROBATION_CASE_MERGE_COMPLETED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.HmppsDomainEventTypes.PROBATION_CASE_UNMERGE_COMPLETED
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model.SQSMessage

@Service
class DomainEventsListener(
  val objectMapper: ObjectMapper,
  val referralCreatedHandler: ReferralCreatedHandler,
  val referralImportedHandler: ReferralImportedHandler,
  val referralMergedHandler: ReferralMergedHandler,
  val referralUnmergedHandler: ReferralUnmergedHandler,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun receive(sqsMessage: SQSMessage) {
    logger.info("Received Event of type: ${sqsMessage.eventType}")
    when (sqsMessage.eventType) {
      INTERVENTIONS_COMMUNITY_REFERRAL_CREATED.value -> referralCreatedHandler.handle(sqsMessage)
      INTERVENTIONS_COMMUNITY_REFERRAL_IMPORTED.value -> referralImportedHandler.handle(sqsMessage)
      PROBATION_CASE_MERGE_COMPLETED.value -> referralMergedHandler.handle(sqsMessage)
      PROBATION_CASE_UNMERGE_COMPLETED.value -> referralUnmergedHandler.handle(sqsMessage)
      else -> logger.info("Unknown event type ${sqsMessage.eventType}")
    }
  }
}
