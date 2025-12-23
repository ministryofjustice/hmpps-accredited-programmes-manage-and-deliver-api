package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CreateAppointmentRequest(
  val reference: UUID,
  val requirementId: String?,
  val licenceConditionId: String?,
  val date: LocalDate,
  val startTime: LocalTime,
  val endTime: LocalTime,
  val outcome: Outcome?,
  val location: Location?,
  val staff: Staff?,
  val team: Team?,
  val notes: String?,
  val sensitive: Boolean,
  val type: AppointmentType,
) {
  companion object {
    data class Outcome(val code: String)
    data class Location(val code: String)
    data class Staff(val code: String)
    data class Team(val code: String)
  }

  enum class AppointmentType {
    PROGRAMME_ATTENDANCE,
    THREE_WAY_MEETING,
  }
}
