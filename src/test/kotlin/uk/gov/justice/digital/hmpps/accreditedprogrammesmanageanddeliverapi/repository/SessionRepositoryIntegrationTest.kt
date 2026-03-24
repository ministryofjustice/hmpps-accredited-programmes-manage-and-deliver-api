package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDateTime

class SessionRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sessionRepository: SessionRepository

  @BeforeEach
  override fun beforeEach() {
    super.beforeEach()
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `findByModuleSessionTemplateIdAndProgrammeGroupIdWhenNotCatchUp should return empty list when only catchup sessions exist`() {
    // Given
    val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
    val module = testDataGenerator.createModule(programmeTemplate, "Test Module", 1)
    val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 1,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Test Session",
        durationMinutes = 120,
      ),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )

    testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2126, 4, 1, 10, 0))
        .withEndsAt(LocalDateTime.of(2126, 4, 1, 12, 0))
        .withIsCatchup(true)
        .produce(),
    )

    // When
    val sessions = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupIdWhenNotCatchUp(
      sessionTemplate.id!!,
      group.id!!,
    )

    // Then
    assertThat(sessions).isEmpty()
  }

  @Test
  @Transactional
  fun `findByModuleSessionTemplateIdAndProgrammeGroupIdWhenNotCatchUp should return only non-catchup sessions`() {
    // Given
    val programmeTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Test Programme")
    val module = testDataGenerator.createModule(programmeTemplate, "Test Module", 1)
    val sessionTemplate = testDataGenerator.createModuleSessionTemplate(
      ModuleSessionTemplateEntity(
        module = module,
        sessionNumber = 1,
        sessionType = SessionType.GROUP,
        pathway = Pathway.MODERATE_INTENSITY,
        name = "Test Session",
        durationMinutes = 120,
      ),
    )
    val group = testDataGenerator.createGroup(
      ProgrammeGroupFactory()
        .withAccreditedProgrammeTemplate(programmeTemplate)
        .produce(),
    )

    val regularSession1 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2126, 4, 1, 10, 0))
        .withEndsAt(LocalDateTime.of(2126, 4, 1, 12, 0))
        .withIsCatchup(false)
        .produce(),
    )
    val regularSession2 = testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2126, 4, 8, 10, 0))
        .withEndsAt(LocalDateTime.of(2126, 4, 8, 12, 0))
        .withIsCatchup(false)
        .produce(),
    )

    testDataGenerator.createSession(
      SessionFactory()
        .withProgrammeGroup(group)
        .withModuleSessionTemplate(sessionTemplate)
        .withStartsAt(LocalDateTime.of(2126, 4, 15, 10, 0))
        .withEndsAt(LocalDateTime.of(2126, 4, 15, 12, 0))
        .withIsCatchup(true)
        .produce(),
    )

    // When
    val sessions = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupIdWhenNotCatchUp(
      sessionTemplate.id!!,
      group.id!!,
    )

    // Then
    assertThat(sessions).hasSize(2)
    assertThat(sessions.map { it.id }).containsExactlyInAnyOrder(regularSession1.id, regularSession2.id)
    assertThat(sessions.all { !it.isCatchup }).isTrue()
  }
}

