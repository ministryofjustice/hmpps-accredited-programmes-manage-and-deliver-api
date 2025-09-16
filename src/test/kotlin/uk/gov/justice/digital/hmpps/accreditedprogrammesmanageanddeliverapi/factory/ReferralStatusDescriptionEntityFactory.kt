package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.util.UUID

class ReferralStatusDescriptionEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var description: String = "Test Status Description"
  private var isClosed: Boolean = false
  private var labelColour: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withDescription(description: String) = apply { this.description = description }
  fun withIsClosed(isClosed: Boolean) = apply { this.isClosed = isClosed }
  fun withLabelColour(labelColour: String?) = apply { this.labelColour = labelColour }

  fun produce() = ReferralStatusDescriptionEntity(
    id = this.id,
    description = this.description,
    isClosed = this.isClosed,
    labelColour = this.labelColour,
  )
}
