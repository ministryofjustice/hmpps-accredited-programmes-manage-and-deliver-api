package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateReferralStatusHistory
import java.util.UUID

class CreateReferralStatusHistoryFactory {
  private var referralStatusDescriptionId: UUID = UUID.randomUUID()
  private var additionalDetails: String = "Updating the status following a one-to-one meeting with Person on Probation"

  fun withReferralStatusDescriptionId(referralStatusDescriptionId: UUID) = apply { this.referralStatusDescriptionId = referralStatusDescriptionId }

  fun withAdditionalDetails(additionalDetails: String) = apply { this.additionalDetails = additionalDetails }

  fun produce() = CreateReferralStatusHistory(
    referralStatusDescriptionId = this.referralStatusDescriptionId,
    additionalDetails = this.additionalDetails,
  )
}
