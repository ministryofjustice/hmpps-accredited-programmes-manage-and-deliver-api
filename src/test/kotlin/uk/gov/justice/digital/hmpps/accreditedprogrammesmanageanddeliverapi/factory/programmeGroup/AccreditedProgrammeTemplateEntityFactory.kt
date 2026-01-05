package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate
import java.util.UUID

class AccreditedProgrammeTemplateEntityFactory {
  private var id: UUID? = null
  private var name: String = randomSentence(wordRange = 2..4)
  private var validFrom: LocalDate = LocalDate.now().minusYears(1)
  private var validUntil: LocalDate? = null
  private var modules: MutableSet<ModuleEntity> = mutableSetOf()
  private var programmeGroups: MutableSet<ProgrammeGroupEntity> = mutableSetOf()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withName(name: String) = apply { this.name = name }
  fun withValidFrom(validFrom: LocalDate) = apply { this.validFrom = validFrom }
  fun withValidUntil(validUntil: LocalDate?) = apply { this.validUntil = validUntil }
  fun withModules(modules: MutableSet<ModuleEntity>) = apply { this.modules = modules }
  fun withProgrammeGroups(programmeGroups: MutableSet<ProgrammeGroupEntity>) = apply { this.programmeGroups = programmeGroups }

  fun addModule(module: ModuleEntity) = apply { this.modules.add(module) }
  fun addProgrammeGroup(programmeGroup: ProgrammeGroupEntity) = apply { this.programmeGroups.add(programmeGroup) }

  fun produce() = AccreditedProgrammeTemplateEntity(
    id = this.id,
    name = this.name,
    validFrom = this.validFrom,
    validUntil = this.validUntil,
    modules = this.modules,
    programmeGroups = this.programmeGroups,
  )
}
