package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom.REQUIREMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ModuleSessionTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AccreditedProgrammeTemplateEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.util.UUID

class CreateAppointmentRequestTest {

  @Test
  fun `toAppointment should map Pre-group one-to-ones module name to PRE_GROUP_ONE_TO_ONE_MEETING`() {
    val attendee = createAttendee(moduleName = "Pre-group one-to-ones")
    val appointment = attendee.toAppointment(UUID.randomUUID())
    assertThat(appointment.type).isEqualTo(AppointmentType.PRE_GROUP_ONE_TO_ONE_MEETING)
  }

  @Test
  fun `toAppointment should map Post-programme reviews module name to THREE_WAY_MEETING`() {
    val attendee = createAttendee(moduleName = "Post-programme reviews")
    val appointment = attendee.toAppointment(UUID.randomUUID())
    assertThat(appointment.type).isEqualTo(AppointmentType.THREE_WAY_MEETING)
  }

  @Test
  fun `toAppointment should map other module names to PROGRAMME_ATTENDANCE`() {
    val attendee = createAttendee(moduleName = "Some other module")
    val appointment = attendee.toAppointment(UUID.randomUUID())
    assertThat(appointment.type).isEqualTo(AppointmentType.PROGRAMME_ATTENDANCE)
  }

  private fun createAttendee(moduleName: String): AttendeeEntity {
    val accreditedProgrammeTemplate = AccreditedProgrammeTemplateEntityFactory().produce()
    return AttendeeFactory()
      .withReferral(ReferralEntityFactory().withSourcedFrom(REQUIREMENT).produce())
      .withSession(
        SessionFactory().withProgrammeGroup(
          ProgrammeGroupFactory()
            .withTreatmentManager(FacilitatorEntityFactory().produce())
            .withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
            .produce(),
        )
          .withModuleSessionTemplate(
            ModuleSessionTemplateEntityFactory()
              .withModule(
                ModuleEntityFactory().withId(UUID.randomUUID())
                  .withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
                  .withName(moduleName)
                  .withModuleNumber(1)
                  .produce(),
              )
              .produce(),
          )
          .produce(),
      )
      .produce()
  }
}
