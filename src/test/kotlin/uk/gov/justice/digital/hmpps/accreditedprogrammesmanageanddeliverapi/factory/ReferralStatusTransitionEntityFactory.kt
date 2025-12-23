package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusTransitionEntity
import java.util.UUID

class ReferralStatusTransitionEntityFactory {
  private val referralStatusDescriptionEntityFactory = ReferralStatusDescriptionEntityFactory()
  private var id: UUID = UUID.randomUUID()
  private var fromStatus: ReferralStatusDescriptionEntity = referralStatusDescriptionEntityFactory
    .withDescription("Awaiting assessment")
    .produce()
  private var toStatus: ReferralStatusDescriptionEntity = referralStatusDescriptionEntityFactory
    .withDescription("Not Eligible")
    .produce()
  private var description: String? = "Test Transition Description"
  private var priority: Int = 1

  fun withId(id: UUID) = apply { this.id = id }
  fun withFromStatus(fromStatus: ReferralStatusDescriptionEntity) = apply { this.fromStatus = fromStatus }
  fun withToStatus(toStatus: ReferralStatusDescriptionEntity) = apply { this.toStatus = toStatus }
  fun withDescription(description: String?) = apply { this.description = description }
  fun withPriority(priority: Int) = apply { this.priority = priority }

  fun produce() = ReferralStatusTransitionEntity(
    id = this.id,
    fromStatus = this.fromStatus,
    toStatus = this.toStatus,
    description = this.description,
    priority = this.priority,
  )
}
