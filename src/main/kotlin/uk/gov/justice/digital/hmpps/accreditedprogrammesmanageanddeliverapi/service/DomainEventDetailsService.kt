package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeCompletionDetails
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class DomainEventDetailsService {

  @Autowired
  private lateinit var referralService: ReferralService

  fun getProgrammeCompletionDetailsForReferral(referralId: UUID): ProgrammeCompletionDetails {
    val referral = referralService.getReferralById(referralId)

    val eventId = referral.eventId
    requireNotNull(eventId) { "EventId must not be null" }

    return ProgrammeCompletionDetails(
      requirementId = eventId,
      requirementCompletedAt = LocalDateTime.now(),
    )
  }
}
