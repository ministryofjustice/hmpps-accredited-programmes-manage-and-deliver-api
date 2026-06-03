package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.listener

import java.util.UUID

sealed class ReferralEvent(
  open val referralId: UUID,
)

data class ReferralStatusUpdateEvent(override val referralId: UUID) : ReferralEvent(referralId)
data class ReferralProgrammeCompleteEvent(override val referralId: UUID) : ReferralEvent(referralId)
