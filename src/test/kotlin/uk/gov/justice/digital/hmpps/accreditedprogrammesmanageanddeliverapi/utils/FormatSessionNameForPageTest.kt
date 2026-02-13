package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.AttendeeFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository

class FormatSessionNameForPageTest(@Autowired private val attendeeRepository: AttendeeRepository) : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `return $moduleName $sessionNumber pattern for group session`() {
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .produce(),
    )

    assertEquals("${sessionTemplate.module.name} ${sessionTemplate.sessionNumber}", formatSessionNameForPage(session))
  }

  @Test
  fun `return $moduleName $sessionNumber catch-up pattern for group session catchup`() {
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.GROUP }

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withIsCatchup(true)
        .produce(),
    )

    assertEquals(
      "${sessionTemplate.module.name} ${sessionTemplate.sessionNumber} catch-up",
      formatSessionNameForPage(session),
    )
  }

  @Test
  fun `return $personName $sessionName pattern for individual session`() {
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .produce(),
    )
    val referral = testDataGenerator.createReferral("Alex River", "X123456")
    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees.add(attendee)
    sessionRepository.save(session)

    assertEquals(
      "${attendee.personName}: ${session.sessionName}",
      formatSessionNameForPage(session),
    )
  }

  @Test
  fun `return $personName $sessionName catch-up pattern for individual session`() {
    val programmeTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")!!
    val sessionTemplate = moduleSessionTemplateRepository.findAll().find { it.sessionType == SessionType.ONE_TO_ONE }

    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )
    val session = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate!!)
        .withIsCatchup(true)
        .produce(),
    )
    val referral = testDataGenerator.createReferral("Alex River", "X123456")
    val attendee = AttendeeFactory().withReferral(referral).withSession(session).produce()
    session.attendees.add(attendee)
    sessionRepository.save(session)

    assertEquals(
      "${attendee.personName}: ${session.sessionName} catch-up",
      formatSessionNameForPage(session),
    )
  }
}
