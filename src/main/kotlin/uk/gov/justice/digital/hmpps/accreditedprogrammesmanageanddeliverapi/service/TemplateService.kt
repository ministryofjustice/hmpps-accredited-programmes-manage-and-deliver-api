package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ModuleSessionTemplate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionScheduleType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
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

  fun getSessionTemplatesForGroupAndModule(
    groupId: UUID,
    moduleId: UUID,
  ): Pair<ModuleEntity, List<ModuleSessionTemplate>> {
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

    return module to sessionTemplates
  }

  private fun addCatchUpModuleSessions(entity: ModuleSessionTemplateEntity): List<ModuleSessionTemplate> {
    val sessionTemplate = entity.toApi()
    val module = entity.module
    return if (entity.sessionType == SessionType.ONE_TO_ONE) {
      // If it's a one-to-one catchup:
      // If the module name is "Post-programme reviews" or "Pre-group one-to-ones", drop the s (e.g. "Pre-group one-to-ones" -> "Pre-group one-to-one")
      // and append "catch-up" directly -> (e.g. "Pre-group one-to-one catch-up", "Post-programme review catch-up").
      // Otherwise, just add "one-to-one catch-up"-> (e.g. "Getting started one-to-one catch-up").
      // If it's not a one-to-one catchup:
      // Return the module name and session number and append catch-up -> (e.g. "Getting started 1 catch-up")
      val catchUpName = if (module.name in listOf("Post-programme reviews", "Pre-group one-to-ones")) {
        "${module.name.dropLast(1)} catch-up"
      } else {
        "${module.name} one-to-one catch-up"
      }
      listOf(
        sessionTemplate,
        sessionTemplate.copy(
          name = catchUpName,
          sessionScheduleType = SessionScheduleType.CATCH_UP,
        ),
      )
    } else {
      listOf(
        sessionTemplate.apply {
          name = "${module.name} ${sessionTemplate.number} catch-up"
          sessionScheduleType = SessionScheduleType.CATCH_UP
        },
      )
    }
  }
}
