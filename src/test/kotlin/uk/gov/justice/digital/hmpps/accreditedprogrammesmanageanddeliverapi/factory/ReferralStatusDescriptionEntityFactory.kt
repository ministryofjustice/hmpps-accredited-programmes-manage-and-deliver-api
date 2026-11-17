package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType.AWAITING_ASSESSMENT
import java.util.UUID

class ReferralStatusDescriptionEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var referralStatusType: ReferralStatusType = AWAITING_ASSESSMENT
  private var isClosed: Boolean = false
  private var labelColour: String = "blue"

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferralStatusType(referralStatusType: ReferralStatusType) = apply { this.referralStatusType = referralStatusType }

  fun withIsClosed(isClosed: Boolean) = apply { this.isClosed = isClosed }
  fun withLabelColour(labelColour: String) = apply { this.labelColour = labelColour }

  fun produce() = ReferralStatusDescriptionEntity(
    id = this.id,
    referralStatusType = this.referralStatusType,
    isClosed = this.isClosed,
    labelColour = this.labelColour,
  )
}
