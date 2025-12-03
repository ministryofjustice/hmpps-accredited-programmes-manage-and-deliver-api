package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class AccreditedProgrammeTemplateRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var repository: AccreditedProgrammeTemplateRepository

  private fun getBuildingChoicesTemplate() = repository.findAll().firstOrNull { it.name == "Building Choices" }

  @Test
  @Transactional
  fun `should have seeded template 'Building Choices' with 10 modules in order`() {
    // Given
    val programmeTemplate = getBuildingChoicesTemplate()
    assertThat(programmeTemplate)
      .withFailMessage("Expected Flyway V55 to seed 'Building Choices' template")
      .isNotNull()

    // When
    val moduleNamesInOrder = programmeTemplate!!.modules
      .sortedBy(ModuleEntity::moduleNumber)
      .map { it.name }

    // Then
    assertThat(moduleNamesInOrder).containsExactly(
      "Pre-Group",
      "Getting Started",
      "Getting Started 1-1",
      "Managing Myself",
      "Managing Myself 1-1",
      "Managing Life’s Problems",
      "Managing Life’s Problems 1-1",
      "Managing People Around Me",
      "Managing People Around Me 1-1",
      "Bringing It All Together",
    )
  }

  @Test
  @Transactional
  fun `should expose correct session counts for Getting Started and Managing Myself`() {
    // Given
    val template = getBuildingChoicesTemplate()

    // When
    val modulesByName = template!!.modules.associateBy { it.name }

    // Then - Getting Started (Group only): Moderate 2
    val gettingReady = modulesByName.getValue("Getting Started")
    val gettingReadySessions = gettingReady.sessionTemplates
    assertThat(gettingReadySessions).allMatch { it.sessionType == SessionType.GROUP }
    assertThat(gettingReadySessions.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(2)

    // Then - Managing Myself (Group only): Moderate 6
    val managingMyself = modulesByName.getValue("Managing Myself")
    val managingMyselfSessions = managingMyself.sessionTemplates
    assertThat(managingMyselfSessions).allMatch { it.sessionType == SessionType.GROUP }
    assertThat(managingMyselfSessions.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(6)
  }

  @Test
  @Transactional
  fun `should expose correct totals across template by pathway and type`() {
    // Given
    val template = getBuildingChoicesTemplate()
    assertThat(template).isNotNull()

    // When
    val allSessionTemplates = template!!.modules.flatMap { it.sessionTemplates }

    // Then
    assertThat(allSessionTemplates.size).isEqualTo(26)
    assertThat(allSessionTemplates.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(26)

    // Then - validate session types: 1-1 modules should be ONE_TO_ONE only; non 1-1 named modules should be GROUP only
    val modulesByName = template.modules.associateBy { it.name }

    listOf(
      "Pre-Group",
      "Getting Started 1-1",
      "Managing Myself 1-1",
      "Managing Life’s Problems 1-1",
      "Managing People Around Me 1-1",
    ).forEach { name ->
      val sessions = modulesByName.getValue(name).sessionTemplates
      assertThat(sessions).isNotEmpty
      assertThat(sessions).allMatch { it.sessionType == SessionType.ONE_TO_ONE }
    }

    listOf(
      "Getting Started",
      "Managing Myself",
      "Managing Life’s Problems",
      "Managing People Around Me",
      "Bringing It All Together",
    ).forEach { name ->
      val sessions = modulesByName.getValue(name).sessionTemplates
      assertThat(sessions).isNotEmpty
      assertThat(sessions).allMatch { it.sessionType == SessionType.GROUP }
    }
  }
}
