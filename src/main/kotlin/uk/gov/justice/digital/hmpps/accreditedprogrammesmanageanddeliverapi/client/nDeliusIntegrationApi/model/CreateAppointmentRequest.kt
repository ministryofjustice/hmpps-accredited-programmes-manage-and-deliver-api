package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
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

fun AttendeeEntity.toAppointment(ndeliusAppointmentId: UUID): CreateAppointmentRequest.NdeliusAppointment {
  val facilitators = session.sessionFacilitators.toList()
  val primaryFacilitator = facilitators.firstOrNull()
  val additionalFacilitators = facilitators.drop(1)

  return CreateAppointmentRequest.NdeliusAppointment(
    reference = ndeliusAppointmentId,
    requirementId = if (referral.sourcedFrom == ReferralEntitySourcedFrom.REQUIREMENT) referral.eventId else null,
    licenceConditionId = if (referral.sourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION) referral.eventId else null,
    date = session.startsAt.toLocalDate(),
    startTime = session.startsAt.toLocalTime(),
    endTime = session.endsAt.toLocalTime(),
    outcome = null,
    location = CreateAppointmentRequest.Location(session.programmeGroup.deliveryLocationCode),
    staff = primaryFacilitator?.let { CreateAppointmentRequest.Staff(it.facilitatorCode) },
    team = primaryFacilitator?.let { CreateAppointmentRequest.Team(it.teamCode) },
    notes = buildSessionNotes(session.programmeGroup.treatmentManager, additionalFacilitators, session),
    sensitive = false,
    type = getAppointmentTypeFromModuleName(session.moduleSessionTemplate.module.name),
  )
}

private fun buildSessionNotes(
  treatmentManager: FacilitatorEntity?,
  additionalFacilitators: List<SessionFacilitatorEntity>,
  session: SessionEntity,
): String? {
  val parts = mutableListOf<String>()
  parts.add(getSessionPart(session))
  treatmentManager?.let { parts.add("Treatment Manager: ${it.personName}") }
  if (additionalFacilitators.isNotEmpty()) {
    parts.add("Additional Facilitators: ${additionalFacilitators.joinToString(", ") { it.facilitator.personName }}")
  }
  return parts.takeIf { it.isNotEmpty() }?.joinToString("\n")
}

private fun getSessionPart(session: SessionEntity): String {
  val type = if (session.isCatchup) "catch-up" else "main"

  return "(Prog ID: ${session.programmeGroup.code.trim()}, Type: $type, Session: ${session.sessionName})"
}

private fun getAppointmentTypeFromModuleName(moduleName: String): AppointmentType = when (moduleName) {
  "Pre-group one-to-ones" -> AppointmentType.PRE_GROUP_ONE_TO_ONE_MEETING
  "Post-programme reviews" -> AppointmentType.THREE_WAY_MEETING
  else -> AppointmentType.PROGRAMME_ATTENDANCE
}
