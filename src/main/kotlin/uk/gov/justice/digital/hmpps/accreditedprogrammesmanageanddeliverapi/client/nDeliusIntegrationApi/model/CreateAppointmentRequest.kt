package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CreateAppointmentRequest(
  val appointments: List<NdeliusAppointment>,
) {
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

fun AttendeeEntity.toAppointment(ndeliusAppointmentId: UUID): CreateAppointmentRequest.NdeliusAppointment = CreateAppointmentRequest.NdeliusAppointment(
  reference = ndeliusAppointmentId,
  requirementId = if (referral.sourcedFrom == ReferralEntitySourcedFrom.REQUIREMENT) referral.eventId else null,
  licenceConditionId = if (referral.sourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION) referral.eventId else null,
  date = session.startsAt.toLocalDate(),
  startTime = session.startsAt.toLocalTime(),
  endTime = session.endsAt.toLocalTime(),
  outcome = null,
  location = CreateAppointmentRequest.Location(session.programmeGroup.deliveryLocationCode),
  staff = CreateAppointmentRequest.Staff(session.programmeGroup.treatmentManager!!.ndeliusPersonCode),
  team = CreateAppointmentRequest.Team(session.programmeGroup.treatmentManager!!.ndeliusTeamCode),
  notes = null,
  sensitive = false,
  type = AppointmentType.PROGRAMME_ATTENDANCE,
)
