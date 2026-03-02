package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository

class SessionNameFormatterTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var sessionNameFormatter: SessionNameFormatter

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  inner class DefaultFormat {

    @Test
    fun `returns moduleName sessionNumber pattern for group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("Getting started 1")
    }

    @Test
    fun `returns moduleName sessionNumber catch-up pattern for group catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("Getting started 1 catch-up")
    }

    @Test
    fun `returns personName sessionName pattern for one-to-one session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("Alex River: Getting started one-to-one")
    }

    @Test
    fun `returns personName sessionName catch-up pattern for one-to-one catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("Alex River: Getting started one-to-one catch-up")
    }
  }

  @Nested
  inner class ScheduleIndividualSession {

    @Test
    fun `returns sessionName for personName has been added for one-to-one session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("Getting started one-to-one for Alex River has been added.")
    }

    @Test
    fun `returns sessionName catch-up for personName has been added for one-to-one catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("Getting started one-to-one catch-up for Alex River has been added.")
    }

    @Test
    fun `returns moduleName sessionNumber has been added for group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("Getting started 1 has been added.")
    }

    @Test
    fun `returns moduleName sessionNumber catch-up has been added for group catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("Getting started 1 catch-up has been added.")
    }
  }

  @Nested
  inner class ScheduleOverview {

    @Test
    fun `returns moduleName as-is for pre-group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Pre-group one-to-ones", 1)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Pre-group one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("Pre-group one-to-ones")
    }

    @Test
    fun `returns sessionName deadline for post-programme session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Post-programme reviews", 7)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Post-programme review",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("Post-programme review deadline")
    }

    @Test
    fun `returns moduleName one-to-ones for one-to-one session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("Getting started one-to-ones")
    }

    @Test
    fun `returns moduleName sessionNumber for group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("Getting started 1")
    }
  }

  @Nested
  inner class SessionsAndAttendance {

    @Test
    fun `returns module name sessionNumber templateName for group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("Getting started 1: Introduction to Building Choices")
    }

    @Test
    fun `returns personName crn templateName for one-to-one session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("Alex River (X123456): Getting started one-to-one")
    }

    @Test
    fun `returns module name sessionNumber templateName catch-up for group catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("Getting started 1: Introduction to Building Choices catch-up")
    }

    @Test
    fun `returns personName crn templateName catch-up for one-to-one catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("Alex River (X123456): Getting started one-to-one catch-up")
    }
  }

  @Nested
  inner class SessionDetails {

    @Test
    fun `returns module name sessionNumber templateName for group session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("Getting started 1: Introduction to Building Choices")
    }

    @Test
    fun `returns module name sessionNumber templateName catch-up for group catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 1,
          sessionType = SessionType.GROUP,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Introduction to Building Choices",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("Getting started 1: Introduction to Building Choices catch-up")
    }

    @Test
    fun `returns personName templateName for one-to-one session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("Alex River: Getting started one-to-one")
    }

    @Test
    fun `returns personName templateName catch-up for one-to-one catchup session`() {
      val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
      val module = testDataGenerator.createModule(programmeTemplate, "Getting started", 2)
      val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
        ModuleSessionTemplateEntity(
          module = module,
          sessionNumber = 3,
          sessionType = SessionType.ONE_TO_ONE,
          pathway = Pathway.MODERATE_INTENSITY,
          name = "Getting started one-to-one",
          durationMinutes = 120,
        ),
      )
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withAccreditedProgrammeTemplate(programmeTemplate)
          .produce(),
      )
      val session = testDataGenerator.createSession(
        SessionFactory()
          .withProgrammeGroup(group)
          .withModuleSessionTemplate(sessionTemplate)
          .withIsCatchup(true)
          .produce(),
      )
      val referral = testDataGenerator.createReferral("Alex River", "X123456")
      val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
      session.attendees.add(attendee)
      sessionRepository.save(session)

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("Alex River: Getting started one-to-one catch-up")
    }
  }
}
