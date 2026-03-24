package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class ModuleSessionTemplateRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  private fun getBuildingChoicesTemplate() = accreditedProgrammeTemplateRepository.findAll().firstOrNull { it.name == "Building Choices" }

  @Test
  @Transactional
  fun `findByModuleIdAndNotCatchUp should return only non-ONE_TO_ONE session IDs for a given module`() {
    // Given
    val template = getBuildingChoicesTemplate()
    val modules = template!!.modules
    val gettingStartedModule = modules.find { it.name == "Getting started" }
    assertThat(gettingStartedModule).isNotNull
    val moduleId = gettingStartedModule!!.id!!

    // When
    val sessionIds = moduleSessionTemplateRepository.findByModuleIdAndNotCatchUp(moduleId)

    // Verify that the returned IDs are for non-ONE_TO_ONE sessions
    val allSessionsForModule = moduleSessionTemplateRepository.findByModuleId(moduleId)
    val nonOneToOneSessions = allSessionsForModule.filter { it.sessionType.toString() != "ONE_TO_ONE" }

    assertThat(sessionIds.size).isEqualTo(nonOneToOneSessions.size)
    assertThat(sessionIds).containsExactlyInAnyOrderElementsOf(nonOneToOneSessions.map { it.id })
  }

  @Test
  @Transactional
  fun `findByModuleIdAndNotCatchUp should return empty list if module has only ONE_TO_ONE sessions`() {
    // Given
    val template = getBuildingChoicesTemplate()

    val modules = template!!.modules
    val preGroupModule = modules.find { it.name == "Pre-group one-to-ones" }
    assertThat(preGroupModule).isNotNull
    val moduleId = preGroupModule!!.id!!

    // When
    val sessionIds = moduleSessionTemplateRepository.findByModuleIdAndNotCatchUp(moduleId)

    // Then
    assertThat(sessionIds).isEmpty()
  }

  @Test
  @Transactional
  fun `isAPreGroupSession should return true when module contains Pre-group one-to-one session`() {
    // Given
    val template = getBuildingChoicesTemplate()
    assertThat(template)
      .withFailMessage("Expected 'Building Choices' template to be seeded")
      .isNotNull()

    val modules = template!!.modules
    val preGroupModule = modules.find { it.name == "Pre-group one-to-ones" }
    assertThat(preGroupModule).isNotNull
    val moduleId = preGroupModule!!.id!!

    // When
    val isPreGroupSession = moduleSessionTemplateRepository.isAPreGroupSession(moduleId)

    // Then
    assertThat(isPreGroupSession).isTrue()
  }

  @Test
  @Transactional
  fun `isAPreGroupSession should return false when module does not contain Pre-group one-to-one session`() {
    // Given
    val template = getBuildingChoicesTemplate()
    assertThat(template)
      .withFailMessage("Expected 'Building Choices' template to be seeded")
      .isNotNull()

    val modules = template!!.modules
    val gettingStartedModule = modules.find { it.name == "Getting started" }
    assertThat(gettingStartedModule).isNotNull
    val moduleId = gettingStartedModule!!.id!!

    // When
    val isPreGroupSession = moduleSessionTemplateRepository.isAPreGroupSession(moduleId)

    // Then
    assertThat(isPreGroupSession).isFalse()
  }

  @Test
  @Transactional
  fun `isAPostProgrammeSession should return true when module contains Post-programme review session`() {
    // Given
    val template = getBuildingChoicesTemplate()

    val modules = template!!.modules
    val postProgrammeModule = modules.find { it.name == "Post-programme reviews" }
    assertThat(postProgrammeModule).isNotNull
    val moduleId = postProgrammeModule!!.id!!

    // When
    val isPostProgrammeSession = moduleSessionTemplateRepository.isAPostProgrammeSession(moduleId)

    // Then
    assertThat(isPostProgrammeSession).isTrue()
  }

  @Test
  @Transactional
  fun `isAPostProgrammeSession should return false when module does not contain Post-programme review session`() {
    // Given
    val template = getBuildingChoicesTemplate()

    val modules = template!!.modules
    val gettingStartedModule = modules.find { it.name == "Getting started" }
    assertThat(gettingStartedModule).isNotNull
    val moduleId = gettingStartedModule!!.id!!

    // When
    val isPostProgrammeSession = moduleSessionTemplateRepository.isAPostProgrammeSession(moduleId)

    // Then
    assertThat(isPostProgrammeSession).isFalse()
  }
}

