package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequestCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentRequest
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class UpdateAppointmentRequestFactory {
  private var reference: UUID = UUID.randomUUID()
  private var date: LocalDate = LocalDate.now()
  private var startTime: LocalTime = LocalTime.of(10, 0)
  private var endTime: LocalTime = LocalTime.of(11, 0)
  private var outcome: RequestCode? = null
  private var location: RequestCode? = null
  private var staff: RequestCode = RequestCode("STAFF1")
  private var team: RequestCode = RequestCode("TEAM1")
  private var notes: String? = "Default notes"
  private var sensitive: Boolean = false

  fun withReference(reference: UUID) = apply { this.reference = reference }
  fun withDate(date: LocalDate) = apply { this.date = date }
  fun withStartTime(startTime: LocalTime) = apply { this.startTime = startTime }
  fun withEndTime(endTime: LocalTime) = apply { this.endTime = endTime }
  fun withOutcome(outcome: RequestCode?) = apply { this.outcome = outcome }
  fun withLocation(location: RequestCode?) = apply { this.location = location }
  fun withStaff(staff: RequestCode) = apply { this.staff = staff }
  fun withTeam(team: RequestCode) = apply { this.team = team }
  fun withNotes(notes: String?) = apply { this.notes = notes }
  fun withSensitive(sensitive: Boolean) = apply { this.sensitive = sensitive }

  fun produce() = UpdateAppointmentRequest(
    reference = this.reference,
    date = this.date,
    startTime = this.startTime,
    endTime = this.endTime,
    outcome = this.outcome,
    location = this.location,
    staff = this.staff,
    team = this.team,
    notes = this.notes,
    sensitive = this.sensitive,
  )
}
