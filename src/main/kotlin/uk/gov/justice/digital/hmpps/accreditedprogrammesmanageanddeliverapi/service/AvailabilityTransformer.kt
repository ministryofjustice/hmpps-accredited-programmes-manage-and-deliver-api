package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.toDayOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

fun CreateAvailability.toEntity(lastModifiedBy: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN"): AvailabilityEntity {
  val availabilityEntity = AvailabilityEntity(
    referralId = this.referralId,
    startDate = this.startDate ?: LocalDate.now(),
    endDate = this.endDate,
    otherDetails = this.otherDetails,
    lastModifiedBy = lastModifiedBy,
    lastModifiedAt = LocalDateTime.now(),
  )

  val slotEntities = this.availabilities.flatMap { dailyAvailability ->
    val dayOfWeek = dailyAvailability.label.toDayOfWeek()

    dailyAvailability.slots
      .filter { it.value }
      .map { slot ->
        val slotName = SlotName.entries.find {
          it.displayName.equals(slot.label, ignoreCase = true)
        } ?: throw IllegalArgumentException("Invalid slot label: ${slot.label}")

        AvailabilitySlotEntity(
          dayOfWeek = dayOfWeek,
          slotName = slotName,
          availability = availabilityEntity,
        )
      }
  }

  availabilityEntity.slots.addAll(slotEntities)
  return availabilityEntity
}

fun AvailabilityEntity.toModel(): Availability {
  val groupedByDay: Map<DayOfWeek, List<AvailabilitySlotEntity>> = this.slots.groupBy { it.dayOfWeek }

  val dailyAvailabilities = DayOfWeek.entries.map { dayOfWeek ->
    val slotsForDay = groupedByDay[dayOfWeek] ?: emptyList()

    DailyAvailabilityModel(
      label = dayOfWeek.toAvailabilityOptions(),
      slots = SlotName.entries.map { slotName ->
        val selected = slotsForDay.any { it.slotName == slotName }
        Slot(
          label = slotName.displayName,
          value = selected,
        )
      },
    )
  }

  return Availability(
    id = this.id,
    referralId = this.referralId,
    startDate = this.startDate.atStartOfDay(),
    endDate = this.endDate?.atStartOfDay(),
    otherDetails = this.otherDetails,
    lastModifiedBy = this.lastModifiedBy,
    lastModifiedAt = this.lastModifiedAt,
    availabilities = dailyAvailabilities,
  )
}
