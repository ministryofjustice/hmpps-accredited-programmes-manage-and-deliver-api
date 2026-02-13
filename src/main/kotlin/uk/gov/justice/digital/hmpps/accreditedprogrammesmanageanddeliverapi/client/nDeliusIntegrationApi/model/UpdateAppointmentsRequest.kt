package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import jakarta.validation.constraints.NotEmpty
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
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

data class RequestCode(val code: String)

fun NDeliusAppointmentEntity.toUpdateAppointmentRequest(sessionNotes: String? = null): UpdateAppointmentRequest = UpdateAppointmentRequest(
  reference = ndeliusAppointmentId,
  date = session.startsAt.toLocalDate(),
  startTime = session.startsAt.toLocalTime(),
  endTime = session.endsAt.toLocalTime(),
  outcome = null,
  location = RequestCode(session.programmeGroup.deliveryLocationCode),
  staff = RequestCode(session.programmeGroup.treatmentManager!!.ndeliusPersonCode),
  team = RequestCode(session.programmeGroup.treatmentManager!!.ndeliusTeamCode),
  notes = sessionNotes,
  sensitive = false,
)
