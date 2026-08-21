package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity

data class SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  val outcomeDescription: String?,
  val attendance: Boolean?,
  val compliant: Boolean,
)

fun SessionAttendanceNDeliusOutcomeEntity.toApi() = SubjectAccessRequestSessionAttendanceNDeliusOutcome(
  outcomeDescription = description,
  attendance = attendance,
  compliant = compliant,
)
