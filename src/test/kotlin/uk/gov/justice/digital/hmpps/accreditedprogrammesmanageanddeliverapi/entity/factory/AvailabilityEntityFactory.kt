package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AvailabilityEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var referralId: UUID = UUID.randomUUID()
  private var startDate: LocalDate = LocalDate.now()
  private var endDate: LocalDate? = null
  private var otherDetails: String? = "Some availability notes"
  private var lastModifiedBy: String = "system_user"
  private var lastModifiedAt: LocalDateTime = LocalDateTime.now()
  private var slots: MutableSet<SlotEntity> = mutableSetOf()

  fun withId(id: UUID?) = apply { this.id = id }

  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }

  fun withStartDate(startDate: LocalDate) = apply { this.startDate = startDate }

  fun withEndDate(endDate: LocalDate?) = apply { this.endDate = endDate }

  fun withOtherDetails(otherDetails: String?) = apply { this.otherDetails = otherDetails }

  fun withLastModifiedBy(user: String) = apply { this.lastModifiedBy = user }

  fun withLastModifiedAt(timestamp: LocalDateTime) = apply { this.lastModifiedAt = timestamp }

  fun withSlots(slots: MutableSet<SlotEntity>) = apply { this.slots = slots }

  fun produce(): AvailabilityEntity {
    val availability = AvailabilityEntity(
      id = this.id,
      referralId = this.referralId,
      startDate = this.startDate,
      endDate = this.endDate,
      otherDetails = this.otherDetails,
      lastModifiedBy = this.lastModifiedBy,
      lastModifiedAt = this.lastModifiedAt,
      slots = this.slots,
    )

    // Set back-reference for slots
    slots.forEach { it.availability = availability }

    return availability
  }
}
