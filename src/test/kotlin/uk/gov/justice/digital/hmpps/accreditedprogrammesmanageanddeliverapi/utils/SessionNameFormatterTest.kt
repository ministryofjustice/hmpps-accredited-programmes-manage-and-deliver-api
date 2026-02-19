package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository

class SessionNameFormatterTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @Autowired
  private lateinit var sessionNameFormatter: SessionNameFormatter

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  inner class Default {

    @Test
    fun `returns moduleName sessionNumber pattern for group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${sessionTemplate.module.name} ${session.sessionNumber}",
        sessionNameFormatter.format(session, SessionNameContext.Default),
      )
    }

    @Test
    fun `returns moduleName sessionNumber catch-up pattern for group catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${sessionTemplate.module.name} ${session.sessionNumber} catch-up",
        sessionNameFormatter.format(session, SessionNameContext.Default),
      )
    }

    @Test
    fun `returns personName sessionName pattern for one-to-one session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${attendee.personName}: ${session.sessionName}",
        sessionNameFormatter.format(session, SessionNameContext.Default),
      )
    }

    @Test
    fun `returns personName sessionName catch-up pattern for one-to-one catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${attendee.personName}: ${session.sessionName} catch-up",
        sessionNameFormatter.format(session, SessionNameContext.Default),
      )
    }
  }

  @Nested
  inner class ScheduleIndividualSession {

    @Test
    fun `returns sessionName for personName has been added for one-to-one session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${session.sessionName} for ${attendee.personName} has been added.",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession),
      )
    }

    @Test
    fun `returns sessionName catch-up for personName has been added for one-to-one catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${session.sessionName} catch-up for ${attendee.personName} has been added.",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession),
      )
    }

    @Test
    fun `returns moduleName sessionNumber has been added for group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${session.moduleName} ${session.sessionNumber} has been added.",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession),
      )
    }

    @Test
    fun `returns moduleName sessionNumber catch-up has been added for group catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${session.moduleName} ${session.sessionNumber} catch-up has been added.",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession),
      )
    }
  }

  @Nested
  inner class ScheduleOverview {

    @Test
    fun `returns moduleName as-is for pre-group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.module.name.startsWith("Pre-group") }!!

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

      assertEquals(
        session.moduleName,
        sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview),
      )
    }

    @Test
    fun `returns sessionName deadline for post-programme session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.module.name.startsWith("Post-programme") }!!

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

      assertEquals(
        "${session.sessionName} deadline",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview),
      )
    }

    @Test
    fun `returns moduleName one-to-ones for one-to-one session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${session.moduleName} one-to-ones",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview),
      )
    }

    @Test
    fun `returns moduleName sessionNumber for group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${session.moduleName} ${session.sessionNumber}",
        sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview),
      )
    }
  }

  @Nested
  inner class SessionsAndAttendance {

    @Test
    fun `returns module name sessionNumber templateName for group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name}",
        sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)),
      )
    }

    @Test
    fun `returns personName crn templateName for one-to-one session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${attendee.personName} (${referral.crn}): ${sessionTemplate.name}",
        sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)),
      )
    }
  }

  @Nested
  inner class SessionDetails {

    @Test
    fun `returns module name sessionNumber templateName for group session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name}",
        sessionNameFormatter.format(session, SessionNameContext.SessionDetails),
      )
    }

    @Test
    fun `returns module name sessionNumber templateName catch-up for group catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }!!

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

      assertEquals(
        "${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name} catch-up",
        sessionNameFormatter.format(session, SessionNameContext.SessionDetails),
      )
    }

    @Test
    fun `returns personName templateName for one-to-one session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${attendee.personName}: ${sessionTemplate.name}",
        sessionNameFormatter.format(session, SessionNameContext.SessionDetails),
      )
    }

    @Test
    fun `returns personName templateName catch-up for one-to-one catchup session`() {
      val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
      val sessionTemplate =
        moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }!!

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

      assertEquals(
        "${attendee.personName}: ${sessionTemplate.name} catch-up",
        sessionNameFormatter.format(session, SessionNameContext.SessionDetails),
      )
    }
  }
}
