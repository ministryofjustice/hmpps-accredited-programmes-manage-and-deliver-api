package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class SubjectAccessRequestAvailability(
  val startDate: LocalDate,
  val endDate: LocalDate?,
  val otherDetails: String?,
  val lastModifiedBy: String,
  val lastModifiedAt: LocalDateTime,
  val slots: List<SubjectAccessRequestAvailabilitySlot>,
)

fun AvailabilityEntity.toApi() = SubjectAccessRequestAvailability(
  startDate = startDate,
  endDate = endDate,
  otherDetails = otherDetails,
  lastModifiedBy = lastModifiedBy,
  lastModifiedAt = lastModifiedAt,
  slots = slots.map { it.toApi() },
)
