package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class DeleteAppointmentsRequest(@NotEmpty val appointments: List<AppointmentReference>)
data class AppointmentReference(val reference: UUID)
