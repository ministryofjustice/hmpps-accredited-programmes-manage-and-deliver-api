package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
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
  inner class DefaultFormat {

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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("${sessionTemplate.module.name} ${session.sessionNumber}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("${sessionTemplate.module.name} ${session.sessionNumber} catch-up")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("${attendee.personName}: ${session.sessionName}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.Default))
        .isEqualTo("${attendee.personName}: ${session.sessionName} catch-up")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("${session.sessionName} for ${attendee.personName} has been added.")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("${session.sessionName} catch-up for ${attendee.personName} has been added.")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("${session.moduleName} ${session.sessionNumber} has been added.")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleIndividualSession))
        .isEqualTo("${session.moduleName} ${session.sessionNumber} catch-up has been added.")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo(session.moduleName)
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("${session.sessionName} deadline")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("${session.moduleName} one-to-ones")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview))
        .isEqualTo("${session.moduleName} ${session.sessionNumber}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionsAndAttendance(sessionTemplate)))
        .isEqualTo("${attendee.personName} (${referral.crn}): ${sessionTemplate.name}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("${sessionTemplate.module.name} ${session.sessionNumber}: ${sessionTemplate.name} catch-up")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("${attendee.personName}: ${sessionTemplate.name}")
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

      assertThat(sessionNameFormatter.format(session, SessionNameContext.SessionDetails))
        .isEqualTo("${attendee.personName}: ${sessionTemplate.name} catch-up")
    }
  }
}
