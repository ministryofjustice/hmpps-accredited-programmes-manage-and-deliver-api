package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import jakarta.validation.constraints.NotEmpty
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.primaryFacilitator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class UpdateAppointmentsRequest(@NotEmpty val appointments: List<UpdateAppointmentRequest>)

data class UpdateAppointmentRequest(
  val reference: UUID,
  val date: LocalDate,
  val startTime: LocalTime,
  val endTime: LocalTime,
  val outcome: RequestCode?,
  val location: RequestCode?,
  val staff: RequestCode,
  val team: RequestCode,
  val notes: String?,
  val sensitive: Boolean,
)

fun NDeliusAppointmentEntity.toUpdateAppointmentRequest(
  sessionNotes: String? = null,
  outcome: SessionAttendanceNDeliusCode? = null,
): UpdateAppointmentRequest {
  val primaryFacilitator = session.primaryFacilitator()
  return UpdateAppointmentRequest(
    reference = ndeliusAppointmentId,
    date = session.startsAt.toLocalDate(),
    startTime = session.startsAt.toLocalTime(),
    endTime = session.endsAt.toLocalTime(),
    outcome = outcome?.let { RequestCode(it.name) },
    location = RequestCode(session.programmeGroup.deliveryLocationCode),
    staff = RequestCode(primaryFacilitator.ndeliusPersonCode),
    team = RequestCode(primaryFacilitator.ndeliusTeamCode),
    notes = sessionNotes,
    sensitive = false,
  )
}
