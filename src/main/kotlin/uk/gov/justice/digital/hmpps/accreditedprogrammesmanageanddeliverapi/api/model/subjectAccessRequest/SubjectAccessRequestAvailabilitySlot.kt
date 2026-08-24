package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilitySlotEntity
import java.time.format.TextStyle
import java.util.Locale

data class SubjectAccessRequestAvailabilitySlot(
  val dayOfWeek: String,
  val slotName: String,
)

fun AvailabilitySlotEntity.toApi() = SubjectAccessRequestAvailabilitySlot(
  dayOfWeek = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.UK),
  slotName = slotName.displayName.replaceFirstChar { it.uppercase() },
)
