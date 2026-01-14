package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
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
      "Pre-group",
      "Getting started",
      "Managing myself",
      "Managing life’s problems",
      "Managing people around me",
      "Bringing it all together",
      "Post programme review",
    )
  }

  @Test
  @Transactional
  fun `should expose correct session counts for Getting Started and Managing Myself`() {
    // Given
    val template = getBuildingChoicesTemplate()

    // When
    val modulesByName = template!!.modules.associateBy { it.name }

    // Then - Getting Started (Group only): Moderate 3
    val gettingReady = modulesByName.getValue("Getting started")
    val gettingReadySessions = gettingReady.sessionTemplates
    assertThat(gettingReadySessions.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(3)

    // Then - Managing Myself (Group only): Moderate 7
    val managingMyself = modulesByName.getValue("Managing myself")
    val managingMyselfSessions = managingMyself.sessionTemplates
    assertThat(managingMyselfSessions.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(7)
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
    assertThat(allSessionTemplates.size).isEqualTo(27)
    assertThat(allSessionTemplates.count { it.pathway == Pathway.MODERATE_INTENSITY }).isEqualTo(27)

    // Then - validate session types: 1-1 modules should be ONE_TO_ONE only; non 1-1 named modules should be GROUP only
    val modulesByName = template.modules.associateBy { it.name }

    listOf(
      "Pre-group",
      "Getting started",
      "Managing myself",
      "Managing life’s problems",
      "Managing people around me",
      "Bringing it all together",
      "Post programme review",
    ).forEach { name ->
      val sessions = modulesByName.getValue(name).sessionTemplates
      assertThat(sessions).isNotEmpty
    }
  }
}
