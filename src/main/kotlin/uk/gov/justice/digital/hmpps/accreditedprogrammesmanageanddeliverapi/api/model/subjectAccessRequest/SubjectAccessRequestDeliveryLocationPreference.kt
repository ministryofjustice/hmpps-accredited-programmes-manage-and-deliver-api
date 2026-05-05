package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import java.time.LocalDateTime

data class SubjectAccessRequestDeliveryLocationPreference(
  val createdBy: String?,
  val createdAt: LocalDateTime?,
  val lastUpdatedAt: LocalDateTime?,
  val locationCannotAttendText: String?,
  val preferredDeliveryLocations: List<SubjectAccessRequestPreferredDeliveryLocation>,
)

fun DeliveryLocationPreferenceEntity.toApi() = SubjectAccessRequestDeliveryLocationPreference(
  createdBy = createdBy,
  createdAt = createdAt,
  lastUpdatedAt = lastUpdatedAt,
  locationCannotAttendText = locationsCannotAttendText,
  preferredDeliveryLocations = preferredDeliveryLocations.map { it.toApi() },
)
