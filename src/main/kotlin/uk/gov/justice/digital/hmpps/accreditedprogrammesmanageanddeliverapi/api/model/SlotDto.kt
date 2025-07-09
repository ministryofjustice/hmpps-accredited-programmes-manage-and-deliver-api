package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import java.time.DayOfWeek

data class SlotDto(
  val dayOfWeek: DayOfWeek,
  val slotName: SlotName,
)

fun SlotEntity.toDto(): SlotDto = SlotDto(
  dayOfWeek = dayOfWeek,
  slotName = slotName,
)
