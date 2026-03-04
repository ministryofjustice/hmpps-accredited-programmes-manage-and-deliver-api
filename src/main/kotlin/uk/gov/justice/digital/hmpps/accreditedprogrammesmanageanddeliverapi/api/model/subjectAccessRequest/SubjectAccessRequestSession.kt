package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.time.LocalDateTime

data class SubjectAccessRequestSession(
  val createdByUsername: String?,
  val endsAt: LocalDateTime,
  val locationName: String?,
  val startsAt: LocalDateTime,
)

fun SessionEntity.toApi() = SubjectAccessRequestSession(
  createdByUsername = createdByUsername,
  endsAt = endsAt,
  locationName = locationName,
  startsAt = startsAt,
)
