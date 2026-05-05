package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupRequest
import java.util.UUID

class RemoveFromGroupRequestFactory {
  private var referralStatusDescriptionId: UUID? = null
  private var additionalDetails: String = "Test additional details"

  fun withId(referralStatusDescriptionId: UUID?) = apply { this.referralStatusDescriptionId = referralStatusDescriptionId }

  fun withAdditionalDetails(additionalDetails: String) = apply { this.additionalDetails = additionalDetails }

  fun produce() = RemoveFromGroupRequest(
    referralStatusDescriptionId = this.referralStatusDescriptionId!!,
    additionalDetails = this.additionalDetails,
  )
}
