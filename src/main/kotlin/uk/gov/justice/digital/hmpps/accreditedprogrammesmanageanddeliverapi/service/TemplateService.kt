package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ModuleSessionTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionScheduleType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
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

  fun getSessionTemplatesForGroupAndModule(groupId: UUID, moduleId: UUID): List<ModuleSessionTemplate> {
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
      .flatMap(::addCatchUpModuleSessions)

    log.info("Found ${sessionTemplates.size} session templates for module: $moduleId")

    return sessionTemplates
  }

  private fun addCatchUpModuleSessions(entity: ModuleSessionTemplateEntity): List<ModuleSessionTemplate> {
    val sessionTemplate = entity.toApi()
    return if (entity.sessionType == SessionType.ONE_TO_ONE) {
      listOf(
        sessionTemplate,
        sessionTemplate.copy(
          name = "${sessionTemplate.name} catch-up",
          sessionScheduleType = SessionScheduleType.CATCH_UP,
        ),
      )
    } else {
      listOf(sessionTemplate)
    }
  }
}
