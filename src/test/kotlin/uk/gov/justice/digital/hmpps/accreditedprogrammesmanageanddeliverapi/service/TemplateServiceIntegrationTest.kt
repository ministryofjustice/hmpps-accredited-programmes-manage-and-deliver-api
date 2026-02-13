package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.util.UUID

class TemplateServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var service: TemplateService

  @Autowired
  private lateinit var moduleRepository: ModuleRepository

  @Nested
  @DisplayName("getSessionTemplatesForGroupAndModule")
  inner class GetSessionTemplatesForGroupAndModule {

    private lateinit var buildingChoicesTemplateId: UUID
    private lateinit var preGroupModuleId: UUID
    private lateinit var gettingStartedModuleId: UUID
    private lateinit var groupId: UUID

    @BeforeEach
    fun setup() {
      val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      assertThat(buildingChoicesTemplate).isNotNull
      buildingChoicesTemplateId = buildingChoicesTemplate.id!!

      val modules = moduleRepository.findByAccreditedProgrammeTemplateId(buildingChoicesTemplateId)
      assertThat(modules).isNotEmpty

      // Find Pre-Group module (module_number = 1)
      val preGroupModule = modules.find { it.moduleNumber == 1 }
      assertThat(preGroupModule).isNotNull
      preGroupModuleId = preGroupModule!!.id!!

      // Find Getting Started module (module_number = 2)
      val gettingStartedModule = modules.find { it.moduleNumber == 2 }
      assertThat(gettingStartedModule).isNotNull
      gettingStartedModuleId = gettingStartedModule!!.id!!

      // Create a test group associated with Building Choices template
      val group = testDataGenerator.createGroup(
        ProgrammeGroupFactory()
          .withSex(ProgrammeGroupSexEnum.MALE)
          .withCode("TEST_GROUP")
          .withRegionName("Test Region")
          .withAccreditedProgrammeTemplate(buildingChoicesTemplate)
          .withEarliestStartDate(LocalDate.now().plusDays(1))
          .produce(),
      )
      groupId = group.id!!
    }

    @AfterEach
    fun tearDown() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `Successfully retrieves session templates for Pre-Group module (one session)`() {
      // When
      val sessionTemplates = service.getOneToOneSessionTemplatesForGroupAndModule(groupId, preGroupModuleId)

      // Then
      assertThat(sessionTemplates).hasSize(1)
      val sessionTemplate = sessionTemplates.first()
      assertThat(sessionTemplate.number).isEqualTo(1)
      assertThat(sessionTemplate.name).isEqualTo("Pre-group one-to-one")
      assertThat(sessionTemplate.id).isNotNull
    }

    @Test
    fun `Throws NotFoundException when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getOneToOneSessionTemplatesForGroupAndModule(nonExistentGroupId, preGroupModuleId)
      }
      assertThat(exception.message).contains("Programme group not found with id: $nonExistentGroupId")
    }

    @Test
    fun `Throws NotFoundException when module does not exist`() {
      // Given
      val nonExistentModuleId = UUID.randomUUID()

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getOneToOneSessionTemplatesForGroupAndModule(groupId, nonExistentModuleId)
      }
      assertThat(exception.message).contains("Module not found with id: $nonExistentModuleId")
    }

    @Test
    fun `Throws NotFoundException when module does not belong to group's template`() {
      val anotherTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Another Template")
      val anotherModule = testDataGenerator.createModule(anotherTemplate, "Another Module", 1)

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getOneToOneSessionTemplatesForGroupAndModule(groupId, anotherModule.id!!)
      }
      assertThat(exception.message).contains(
        "Module with id: ${anotherModule.id} does not belong to the accredited programme template for group: $groupId",
      )
    }
  }
}
