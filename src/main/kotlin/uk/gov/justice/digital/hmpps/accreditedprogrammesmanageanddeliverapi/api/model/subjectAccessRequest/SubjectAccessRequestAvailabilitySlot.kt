package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity

data class SubjectAccessRequestAvailabilitySlot(
  val dayOfWeek: String,
  val slotName: String,
)

fun AvailabilitySlotEntity.toApi() = SubjectAccessRequestAvailabilitySlot(
  dayOfWeek = dayOfWeek.name,
  slotName = slotName.name,
)
