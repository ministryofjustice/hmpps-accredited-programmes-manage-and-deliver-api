package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionScheduleType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
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
    private lateinit var modules: List<ModuleEntity>

    @BeforeEach
    fun setup() {
      val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.getBuildingChoicesTemplate()
      assertThat(buildingChoicesTemplate).isNotNull
      buildingChoicesTemplateId = buildingChoicesTemplate.id!!

      modules = moduleRepository.findByAccreditedProgrammeTemplateId(buildingChoicesTemplateId)
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
    fun `Successfully retrieves session templates for Pre-Group module (one session and its catch up)`() {
      // When
      val sessionTemplates = service.getSessionTemplatesForGroupAndModule(groupId, preGroupModuleId)

      // Then
      assertThat(sessionTemplates).hasSize(2)
      val sessionTemplate = sessionTemplates.first()
      assertThat(sessionTemplate.number).isEqualTo(1)
      assertThat(sessionTemplate.name).isEqualTo("Pre-group one-to-one")
      assertThat(sessionTemplate.sessionScheduleType).isEqualTo(SessionScheduleType.SCHEDULED)
      assertThat(sessionTemplate.id).isNotNull

      val catchUpTemplate = sessionTemplates.last()
      assertThat(catchUpTemplate.number).isEqualTo(1)
      assertThat(catchUpTemplate.name).isEqualTo("Pre-group one-to-one catch-up")
      assertThat(catchUpTemplate.sessionScheduleType).isEqualTo(SessionScheduleType.CATCH_UP)
      assertThat(catchUpTemplate.id).isNotNull
    }

    @Test
    fun `Successfully retrieves all session templates for Managing myself module`() {
      // Given
      val managingMyselfModule = modules.find { it.name == "Managing myself" }

      // When
      val sessionTemplates = service.getSessionTemplatesForGroupAndModule(groupId, managingMyselfModule?.id!!)

      // Then
      assertThat(sessionTemplates).hasSize(8)
        .extracting("name", "number", "sessionScheduleType")
        .containsExactly(
          tuple("Understanding my feelings", 1, SessionScheduleType.SCHEDULED),
          tuple("Helpful and unhelpful feelings", 2, SessionScheduleType.SCHEDULED),
          tuple("Managing my feelings, part 1", 3, SessionScheduleType.SCHEDULED),
          tuple("Managing my feelings, part 2", 4, SessionScheduleType.SCHEDULED),
          tuple("Understanding my thinking", 5, SessionScheduleType.SCHEDULED),
          tuple("Developing my flexible thinking", 6, SessionScheduleType.SCHEDULED),
          tuple("Managing myself one-to-one", 7, SessionScheduleType.SCHEDULED),
          tuple("Managing myself one-to-one catch-up", 7, SessionScheduleType.CATCH_UP),
        )
    }

    @Test
    fun `Throws NotFoundException when group does not exist`() {
      // Given
      val nonExistentGroupId = UUID.randomUUID()

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getSessionTemplatesForGroupAndModule(nonExistentGroupId, preGroupModuleId)
      }
      assertThat(exception.message).contains("Programme group not found with id: $nonExistentGroupId")
    }

    @Test
    fun `Throws NotFoundException when module does not exist`() {
      // Given
      val nonExistentModuleId = UUID.randomUUID()

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getSessionTemplatesForGroupAndModule(groupId, nonExistentModuleId)
      }
      assertThat(exception.message).contains("Module not found with id: $nonExistentModuleId")
    }

    @Test
    fun `Throws NotFoundException when module does not belong to group's template`() {
      val anotherTemplate = testDataGenerator.createAccreditedProgrammeTemplate("Another Template")
      val anotherModule = testDataGenerator.createModule(anotherTemplate, "Another Module", 1)

      // When / Then
      val exception = assertThrows<NotFoundException> {
        service.getSessionTemplatesForGroupAndModule(groupId, anotherModule.id!!)
      }
      assertThat(exception.message).contains(
        "Module with id: ${anotherModule.id} does not belong to the accredited programme template for group: $groupId",
      )
    }
  }
}
