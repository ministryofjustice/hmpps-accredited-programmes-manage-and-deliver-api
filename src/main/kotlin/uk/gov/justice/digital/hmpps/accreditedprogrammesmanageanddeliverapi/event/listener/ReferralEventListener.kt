package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.ReferralEventService

/**
 * This listener ensures that we only publish our referral events AFTER we have commited a transaction
 */
@Component
class ReferralEventListener(private val referralEventService: ReferralEventService) {

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onReferralStatusUpdate(event: ReferralStatusUpdateEvent) {
    referralEventService.publishReferralStatusUpdatedEvent(event.referralId)
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun onReferralProgrammeComplete(event: ReferralProgrammeCompleteEvent) {
    referralEventService.publishReferralCompletedEvent(event.referralId)
  }
}
