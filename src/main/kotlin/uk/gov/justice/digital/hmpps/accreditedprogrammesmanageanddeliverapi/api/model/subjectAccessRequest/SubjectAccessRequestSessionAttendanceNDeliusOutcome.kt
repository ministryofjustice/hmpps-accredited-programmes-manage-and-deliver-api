package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity

data class SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  val attendance: Boolean?,
  val compliant: Boolean,
)

fun SessionAttendanceNDeliusOutcomeEntity.toApi() = SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  attendance = attendance,
  compliant = compliant,
)
