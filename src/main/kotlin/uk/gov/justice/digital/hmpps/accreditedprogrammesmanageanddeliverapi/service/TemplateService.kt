package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionTypeResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTemplateItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import java.util.UUID

@Service
@Transactional
class TemplateService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val moduleRepository: ModuleRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getOneToOneSessionTemplatesForGroupAndModule(groupId: UUID, moduleId: UUID): ScheduleSessionTypeResponse {
    log.info("Retrieving session templates for group: $groupId and module: $moduleId")

    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Programme group not found with id: $groupId")

    val templateId = group.accreditedProgrammeTemplate?.id
      ?: throw NotFoundException("Programme group with id: $groupId has no associated accredited programme template")

    val module = moduleRepository.findByIdOrNull(moduleId)
      ?: throw NotFoundException("Module not found with id: $moduleId")

    if (module.accreditedProgrammeTemplate.id != templateId) {
      throw NotFoundException("Module with id: $moduleId does not belong to the accredited programme template for group: $groupId")
    }

    val sessionTemplates = moduleSessionTemplateRepository.findByModuleId(moduleId)
      .sortedBy { it.sessionNumber }
      .filter { it.sessionType == SessionType.ONE_TO_ONE }
      .map { template ->
        SessionTemplateItem(
          id = template.id!!,
          number = template.sessionNumber,
          name = template.name,
        )
      }

    log.info("Found ${sessionTemplates.size} One-to-One session templates for module: $moduleId")

    return ScheduleSessionTypeResponse(sessionTemplates = sessionTemplates)
  }
}
