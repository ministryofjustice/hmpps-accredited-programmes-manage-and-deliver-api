package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentsRequest

class UpdateAppointmentsRequestFactory {
  private var appointments: List<UpdateAppointmentRequest> = listOf(UpdateAppointmentRequestFactory().produce())

  fun withAppointments(appointments: List<UpdateAppointmentRequest>) = apply { this.appointments = appointments }

  fun produce() = UpdateAppointmentsRequest(
    appointments = this.appointments,
  )
}
