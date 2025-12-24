package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CreateAppointmentRequest(
  val appointments: List<NdeliusAppointment>,
) {
  companion object {
    data class NdeliusAppointment(
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
    )

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

fun SessionAttendanceEntity.toAppointment(): CreateAppointmentRequest.Companion.NdeliusAppointment = CreateAppointmentRequest.Companion.NdeliusAppointment(
  reference = UUID.randomUUID(),
  requirementId = if (eventType == ReferralEntitySourcedFrom.REQUIREMENT) eventId else null,
  licenceConditionId = if (eventType == ReferralEntitySourcedFrom.LICENCE_CONDITION) eventId else null,
  date = session.startsAt.toLocalDate(),
  startTime = session.startsAt.toLocalTime(),
  endTime = session.endsAt.toLocalTime(),
  outcome = null,
  // TODO This should be code and not null
  location = CreateAppointmentRequest.Companion.Location(session.programmeGroup.deliveryLocationCode),
  // TODO get staff code
  staff = CreateAppointmentRequest.Companion.Staff(session.programmeGroup.treatmentManager!!.ndeliusPersonCode),
  // TODO get team code from staff
  team = CreateAppointmentRequest.Companion.Team(session.programmeGroup.treatmentManager!!.ndeliusTeamCode),
  notes = null,
  sensitive = false,
  type = CreateAppointmentRequest.AppointmentType.PROGRAMME_ATTENDANCE,
)
