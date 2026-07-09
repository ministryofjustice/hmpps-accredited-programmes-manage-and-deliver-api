package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom.REQUIREMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
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

  @Test
  fun `toAppointment should use first facilitator as staff officer`() {
    val primaryFacilitator = FacilitatorEntityFactory()
      .withNdeliusPersonCode("FAC001")
      .withNdeliusTeamCode("TEAM001")
      .produce()
    val session = buildSession(facilitators = listOf(primaryFacilitator))
    val attendee = attendeeFor(session)

    val appointment = attendee.toAppointment(UUID.randomUUID())

    assertThat(appointment.staff).isEqualTo(CreateAppointmentRequest.Staff("FAC001"))
    assertThat(appointment.team).isEqualTo(CreateAppointmentRequest.Team("TEAM001"))
  }

  @Test
  fun `toAppointment should produce null staff and team when session has no facilitators`() {
    val session = buildSession(facilitators = emptyList())
    val attendee = attendeeFor(session)

    val appointment = attendee.toAppointment(UUID.randomUUID())

    assertThat(appointment.staff).isNull()
    assertThat(appointment.team).isNull()
  }

  @Test
  fun `toAppointment should include treatment manager name in notes`() {
    val treatmentManager = FacilitatorEntityFactory().withPersonName("Treatment Manager Name").produce()
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = treatmentManager,
    )
    val attendee = attendeeFor(session)

    val appointment = attendee.toAppointment(UUID.randomUUID())

    assertThat(appointment.notes).contains("Treatment Manager: Treatment Manager Name")
  }

  @Test
  fun `toAppointment should include additional facilitator names in notes`() {
    val primaryFacilitator = FacilitatorEntityFactory().withPersonName("Primary Facilitator").produce()
    val secondFacilitator = FacilitatorEntityFactory().withPersonName("Second Facilitator").produce()
    val thirdFacilitator = FacilitatorEntityFactory().withPersonName("Third Facilitator").produce()
    val treatmentManager = FacilitatorEntityFactory().withPersonName("Treatment Manager Name").produce()
    val session = buildSession(
      facilitators = listOf(primaryFacilitator, secondFacilitator, thirdFacilitator),
      treatmentManager = treatmentManager,
    )
    val attendee = attendeeFor(session)

    val appointment = attendee.toAppointment(UUID.randomUUID())

    assertThat(appointment.notes).contains("Treatment Manager: Treatment Manager Name")
    assertThat(appointment.notes).contains("Additional Facilitators: Second Facilitator, Third Facilitator")
    assertThat(appointment.notes).doesNotContain("Primary Facilitator")
  }

  @Test
  fun `toAppointment should produce null notes when no treatment manager and only one facilitator`() {
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = null,
    )
    val attendee = attendeeFor(session)

    val appointment = attendee.toAppointment(UUID.randomUUID())

    assertThat(appointment.notes).isNull()
  }

  private fun buildSession(
    moduleName: String = "Module",
    facilitators: List<FacilitatorEntity> = emptyList(),
    treatmentManager: FacilitatorEntity? = FacilitatorEntityFactory().produce(),
  ): SessionEntity {
    val accreditedProgrammeTemplate = AccreditedProgrammeTemplateEntityFactory().produce()
    val groupFactory = ProgrammeGroupFactory().withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
    if (treatmentManager != null) groupFactory.withTreatmentManager(treatmentManager)
    val group = groupFactory.produce().also { it.treatmentManager = treatmentManager }

    val session = SessionFactory()
      .withProgrammeGroup(group)
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
      .produce()

    session.sessionFacilitators = facilitators
      .mapTo(linkedSetOf()) { SessionFacilitatorEntity(it, session, FacilitatorType.REGULAR_FACILITATOR) }

    return session
  }

  private fun attendeeFor(session: SessionEntity) = AttendeeFactory()
    .withReferral(ReferralEntityFactory().withSourcedFrom(REQUIREMENT).produce())
    .withSession(session)
    .produce()

  private fun createAttendee(moduleName: String) = attendeeFor(buildSession(moduleName = moduleName))
}
