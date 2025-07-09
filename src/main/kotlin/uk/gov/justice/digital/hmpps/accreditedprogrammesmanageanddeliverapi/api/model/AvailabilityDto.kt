package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import java.time.LocalDate

data class AvailabilityDto(
  val availabilityId: String,
  val referralId: String,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val details: String?,
  val slots: List<SlotDto>,
)

fun toDto(availability: AvailabilityEntity): AvailabilityDto = AvailabilityDto(
  availabilityId = availability.id.toString(),
  referralId = availability.referralId.toString(),
  startDate = availability.startDate,
  endDate = availability.endDate,
  details = availability.otherDetails,
  slots = availability.slots.map { slot -> SlotDto(dayOfWeek = slot.dayOfWeek, slotName = slot.slotName) },
)
