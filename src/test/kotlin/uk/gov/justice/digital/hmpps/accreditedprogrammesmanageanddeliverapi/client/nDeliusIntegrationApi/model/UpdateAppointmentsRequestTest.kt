package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusAppointmentEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AccreditedProgrammeTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.util.UUID

class UpdateAppointmentsRequestTest {

  @Test
  fun `toUpdateAppointmentRequest should set description to module name and session number for a group session`() {
    val appointment = buildAppointment(moduleName = "Getting started", sessionNumber = 4)

    val request = appointment.toUpdateAppointmentRequest()

    assertThat(request.description).isEqualTo("Getting started 4")
  }

  @Test
  fun `toUpdateAppointmentRequest should append catch-up to the description for a catch-up session`() {
    val appointment = buildAppointment(moduleName = "Getting started", sessionNumber = 4, isCatchup = true)

    val request = appointment.toUpdateAppointmentRequest()

    assertThat(request.description).isEqualTo("Getting started 4 catch-up")
  }

  @Test
  fun `toUpdateAppointmentRequest should set description to the session name for a one-to-one session`() {
    val appointment = buildAppointment(moduleName = "Getting started", sessionType = SessionType.ONE_TO_ONE)

    val request = appointment.toUpdateAppointmentRequest()

    assertThat(request.description).isEqualTo("Module Session Template 1")
  }

  @Test
  fun `toUpdateAppointmentRequest should populate description when only the outcome and notes are being updated`() {
    val appointment = buildAppointment(moduleName = "Getting started", sessionNumber = 2)

    val request = appointment.toUpdateAppointmentRequest(
      sessionNotes = "Attended and engaged well",
      outcome = SessionAttendanceNDeliusCode.ATTC,
    )

    assertThat(request.description).isEqualTo("Getting started 2")
    assertThat(request.notes).isEqualTo("Attended and engaged well")
    assertThat(request.outcome).isEqualTo(RequestCode(SessionAttendanceNDeliusCode.ATTC.name))
  }

  @Test
  fun `toUpdateAppointmentRequest should never send a null description`() {
    val appointment = buildAppointment(moduleName = "Getting started", sessionNumber = 1)

    val request = appointment.toUpdateAppointmentRequest()

    assertThat(request.description).isNotNull()
    assertThat(request.description).isNotBlank()
  }

  private fun buildAppointment(
    moduleName: String = "Module",
    sessionType: SessionType = SessionType.GROUP,
    sessionNumber: Int = 1,
    isCatchup: Boolean = false,
  ): NDeliusAppointmentEntity {
    val session = buildSession(moduleName, sessionType, sessionNumber, isCatchup)
    return NDeliusAppointmentEntityFactory().withSession(session).produce()
  }

  private fun buildSession(
    moduleName: String,
    sessionType: SessionType,
    sessionNumber: Int,
    isCatchup: Boolean,
  ): SessionEntity {
    val accreditedProgrammeTemplate = AccreditedProgrammeTemplateEntityFactory().produce()
    val facilitator: FacilitatorEntity = FacilitatorEntityFactory()
      .withNdeliusPersonCode("FAC001")
      .withNdeliusTeamCode("TEAM001")
      .produce()
    val group = ProgrammeGroupFactory()
      .withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
      .produce()

    val session = SessionFactory()
      .withProgrammeGroup(group)
      .withIsCatchup(isCatchup)
      .withModuleSessionTemplate(
        ModuleSessionTemplateEntityFactory()
          .withModule(
            ModuleEntityFactory().withId(UUID.randomUUID())
              .withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
              .withName(moduleName)
              .withModuleNumber(1)
              .produce(),
          )
          .withSessionType(sessionType)
          .withSessionNumber(sessionNumber)
          .produce(),
      )
      .produce()

    session.sessionFacilitators =
      linkedSetOf(SessionFacilitatorEntity(facilitator, session, FacilitatorType.REGULAR_FACILITATOR))

    return session
  }
}
