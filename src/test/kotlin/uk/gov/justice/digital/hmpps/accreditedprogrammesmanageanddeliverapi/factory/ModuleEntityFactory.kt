package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import java.time.LocalDate
import java.util.UUID
import kotlin.String

class ModuleEntityFactory {
  private var id: UUID? = null
  private var accreditedProgrammeTemplate: AccreditedProgrammeTemplateEntity = AccreditedProgrammeTemplateEntity(
    UUID.randomUUID(),
    "A1234AA",
    LocalDate.now(),
    LocalDate.now().plusDays(1),
  )
  private var name: String = "Module 1"
  private var moduleNumber: Int = 1
  private var sessionTemplates: MutableSet<ModuleSessionTemplateEntity> = mutableSetOf()

  fun withId(id: UUID) = apply { this.id = id }
  fun withAccreditedProgrammeTemplate(accreditedProgrammeTemplate: AccreditedProgrammeTemplateEntity) = apply { this.accreditedProgrammeTemplate = accreditedProgrammeTemplate }

  fun withName(name: String) = apply { this.name = name }
  fun withModuleNumber(moduleNumber: Int) = apply { this.moduleNumber = moduleNumber }
  fun withSessionTemplates(sessionTemplates: MutableSet<ModuleSessionTemplateEntity>) = apply { this.sessionTemplates = sessionTemplates }

  fun produce() = ModuleEntity(
    id = this.id,
    accreditedProgrammeTemplate = this.accreditedProgrammeTemplate,
    name = this.name,
    moduleNumber = this.moduleNumber,
    sessionTemplates = this.sessionTemplates,
  )
}
