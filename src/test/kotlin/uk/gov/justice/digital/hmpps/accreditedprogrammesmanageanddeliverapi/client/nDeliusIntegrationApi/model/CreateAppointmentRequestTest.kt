package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom.REQUIREMENT
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
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
    // Given
    val attendee = createAttendee(moduleName = "Pre-group one-to-ones")

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.type).isEqualTo(AppointmentType.PRE_GROUP_ONE_TO_ONE_MEETING)
  }

  @Test
  fun `toAppointment should map Post-programme reviews module name to THREE_WAY_MEETING`() {
    // Given
    val attendee = createAttendee(moduleName = "Post-programme reviews")

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.type).isEqualTo(AppointmentType.THREE_WAY_MEETING)
  }

  @Test
  fun `toAppointment should map other module names to PROGRAMME_ATTENDANCE`() {
    val attendee = createAttendee(moduleName = "Some other module")
    val appointment = attendee.toAppointment(UUID.randomUUID())
    assertThat(appointment.type).isEqualTo(AppointmentType.PROGRAMME_ATTENDANCE)
  }

  @Test
  fun `toAppointment should use the first regular facilitator as staff officer`() {
    // Given
    val primaryFacilitator = FacilitatorEntityFactory()
      .withNdeliusPersonCode("FAC001")
      .withNdeliusTeamCode("TEAM001")
      .produce()
    val session = buildSession(facilitators = listOf(primaryFacilitator))
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.staff).isEqualTo(RequestCode("FAC001"))
    assertThat(result.team).isEqualTo(RequestCode("TEAM001"))
  }

  @Test
  fun `toAppointment should select regular facilitators deterministically by name`() {
    // Given
    val coverFacilitator = FacilitatorEntityFactory()
      .withPersonName("Aaron Cover")
      .withNdeliusPersonCode("COVER")
      .withNdeliusTeamCode("COVER_TEAM")
      .produce()
    val laterRegular = FacilitatorEntityFactory().withPersonName("Zoe Regular").produce()
    val earlierRegular = FacilitatorEntityFactory()
      .withPersonName("Bilal Regular")
      .withNdeliusPersonCode("REG")
      .withNdeliusTeamCode("REG_TEAM")
      .produce()
    val session = buildSession(
      facilitators = listOf(laterRegular, earlierRegular),
      coverFacilitators = listOf(coverFacilitator),
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    // Cover facilitator sorts first alphabetically but must be ignored; the earliest regular facilitator by name wins
    assertThat(result.staff).isEqualTo(RequestCode("REG"))
    assertThat(result.team).isEqualTo(RequestCode("REG_TEAM"))
  }

  @Test
  fun `toAppointment should fall back to a cover facilitator when there is no regular facilitator`() {
    // Given
    val coverFacilitator = FacilitatorEntityFactory()
      .withNdeliusPersonCode("COVER")
      .withNdeliusTeamCode("COVER_TEAM")
      .produce()
    val session = buildSession(
      facilitators = emptyList(),
      coverFacilitators = listOf(coverFacilitator),
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.staff).isEqualTo(RequestCode("COVER"))
    assertThat(result.team).isEqualTo(RequestCode("COVER_TEAM"))
  }

  @Test
  fun `toAppointment should include treatment manager name in notes`() {
    // Given
    val treatmentManager = FacilitatorEntityFactory().withPersonName("Treatment Manager Name").produce()
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = treatmentManager,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.notes).contains("Treatment Manager: Treatment Manager Name")
  }

  @Test
  fun `toAppointment should throw when session has no facilitators at all`() {
    // Given
    val session = buildSession(facilitators = emptyList())
    val attendee = attendeeFor(session)
    session.programmeGroup.treatmentManager = null

    // When Then
    assertThrows<BusinessException> { attendee.toAppointment(UUID.randomUUID()) }
  }

  @Test
  fun `toAppointment should include createdBy, session info and additional facilitator names in notes`() {
    // Given
    val primaryFacilitator = FacilitatorEntityFactory().withPersonName("Primary Facilitator").produce()
    val secondFacilitator = FacilitatorEntityFactory().withPersonName("Second Facilitator").produce()
    val thirdFacilitator = FacilitatorEntityFactory().withPersonName("Third Facilitator").produce()
    val treatmentManager = FacilitatorEntityFactory().withPersonName("Treatment Manager Name").produce()
    val session = buildSession(
      facilitators = listOf(primaryFacilitator, secondFacilitator, thirdFacilitator),
      treatmentManager = treatmentManager,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.notes).contains("(Prog ID: AAA111, Type: main, Session: Module Session Template 1)")
    assertThat(result.notes).contains("Treatment Manager: Treatment Manager Name")
    assertThat(result.notes).contains("Additional Facilitators: Second Facilitator, Third Facilitator")
    assertThat(result.notes).doesNotContain("Primary Facilitator")
  }

  @Test
  fun `toAppointment should include catch up session type in notes when no treatment manager and no facilitator`() {
    // Given
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = null,
      isCatch = true,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.notes).contains("(Prog ID: AAA111, Type: catch-up, Session: Module Session Template 1)")
  }

  @Test
  fun `toAppointment should produce notes when no treatment manager and only one facilitator`() {
    // Given
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = null,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.notes).contains("(Prog ID: AAA111, Type: main, Session: Module Session Template 1)")
    assertThat(result.notes).doesNotContain("Treatment Manager:")
    assertThat(result.notes).doesNotContain("Additional Facilitators:")
  }

  @Test
  fun `toAppointment should set description to module name and session number for a group session`() {
    // Given
    val session = buildSession(moduleName = "Getting started", sessionType = SessionType.GROUP, sessionNumber = 4)
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.description).isEqualTo("Getting started 4")
  }

  @Test
  fun `toAppointment should append catch-up to the description for a group catch-up session`() {
    // Given
    val session = buildSession(
      moduleName = "Getting started",
      sessionType = SessionType.GROUP,
      sessionNumber = 4,
      isCatch = true,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.description).isEqualTo("Getting started 4 catch-up")
  }

  @Test
  fun `toAppointment should set description to the session name for a one-to-one session`() {
    // Given
    val session = buildSession(moduleName = "Getting started", sessionType = SessionType.ONE_TO_ONE)
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.description).isEqualTo("Module Session Template 1")
  }

  @Test
  fun `toAppointment should append catch-up to the description for a one-to-one catch-up session`() {
    // Given
    val session = buildSession(
      moduleName = "Getting started",
      sessionType = SessionType.ONE_TO_ONE,
      isCatch = true,
    )
    val attendee = attendeeFor(session)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.description).isEqualTo("Module Session Template 1 catch-up")
  }

  @Test
  fun `toAppointment should not include the person name in a one-to-one description`() {
    // Given
    val session = buildSession(moduleName = "Getting started", sessionType = SessionType.ONE_TO_ONE)
    val attendee = AttendeeFactory()
      .withReferral(
        ReferralEntityFactory().withSourcedFrom(REQUIREMENT).withPersonName("Alex River").produce(),
      )
      .withSession(session)
      .produce()
    session.attendees.add(attendee)

    // When
    val result = attendee.toAppointment(UUID.randomUUID())

    // Then
    assertThat(result.description).isEqualTo("Module Session Template 1")
    assertThat(result.description).doesNotContain("Alex River")
  }

  @Test
  fun `NDeliusAppointmentEntity toAppointment should map correctly with provided outcome and notes`() {
    // Given
    val session = buildSession()
    val referral = ReferralEntityFactory().withSourcedFrom(REQUIREMENT).produce()
    val nDeliusAppointmentEntity = NDeliusAppointmentEntity(
      ndeliusAppointmentId = UUID.randomUUID(),
      session = session,
      referral = referral,
    )

    val outcome = RequestCode("OUTCOME1")
    val notes = "Custom notes"

    // When
    val result = nDeliusAppointmentEntity.toAppointment(outcome = outcome, notes = notes)

    // Then
    assertThat(result.outcome).isEqualTo(outcome)
    assertThat(result.notes).isEqualTo(notes)
    assertThat(result.reference).isEqualTo(nDeliusAppointmentEntity.ndeliusAppointmentId)
  }

  @Test
  fun `NDeliusAppointmentEntity toAppointment should generate notes when notes not provided`() {
    // Given
    val session = buildSession(
      facilitators = listOf(FacilitatorEntityFactory().produce()),
      treatmentManager = FacilitatorEntityFactory().withPersonName("Treatment Manager Name").produce(),
    )
    val referral = ReferralEntityFactory().withSourcedFrom(REQUIREMENT).produce()
    val nDeliusAppointmentEntity = NDeliusAppointmentEntity(
      ndeliusAppointmentId = UUID.randomUUID(),
      session = session,
      referral = referral,
    )

    // When
    val result = nDeliusAppointmentEntity.toAppointment(outcome = null, notes = null)

    // Then
    assertThat(result.notes).contains("Treatment Manager: Treatment Manager Name")
  }

  private fun buildSession(
    moduleName: String = "Module",
    facilitators: List<FacilitatorEntity> = listOf(FacilitatorEntityFactory().produce()),
    coverFacilitators: List<FacilitatorEntity> = emptyList(),
    treatmentManager: FacilitatorEntity? = FacilitatorEntityFactory().produce(),
    isCatch: Boolean = false,
    sessionType: SessionType = SessionType.GROUP,
    sessionNumber: Int = 1,
  ): SessionEntity {
    val accreditedProgrammeTemplate = AccreditedProgrammeTemplateEntityFactory().produce()
    val groupFactory = ProgrammeGroupFactory().withAccreditedProgrammeTemplate(accreditedProgrammeTemplate)
    if (treatmentManager != null) groupFactory.withTreatmentManager(treatmentManager)
    val group = groupFactory.produce().also { it.treatmentManager = treatmentManager }

    val session = SessionFactory()
      .withProgrammeGroup(group)
      .withIsCatchup(isCatch)
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

    session.sessionFacilitators = buildList {
      facilitators.forEach { add(SessionFacilitatorEntity(it, session, FacilitatorType.REGULAR_FACILITATOR)) }
      coverFacilitators.forEach { add(SessionFacilitatorEntity(it, session, FacilitatorType.COVER_FACILITATOR)) }
    }.toCollection(linkedSetOf())

    return session
  }

  private fun attendeeFor(session: SessionEntity) = AttendeeFactory()
    .withReferral(ReferralEntityFactory().withSourcedFrom(REQUIREMENT).produce())
    .withSession(session)
    .produce()

  private fun createAttendee(moduleName: String) = attendeeFor(buildSession(moduleName = moduleName))
}
