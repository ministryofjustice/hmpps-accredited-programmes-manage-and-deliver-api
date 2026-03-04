package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity

data class SubjectAccessRequestAttendee(
  val session: SubjectAccessRequestSession,
)

fun AttendeeEntity.toApi() = SubjectAccessRequestAttendee(
  session = session.toApi(),
)
