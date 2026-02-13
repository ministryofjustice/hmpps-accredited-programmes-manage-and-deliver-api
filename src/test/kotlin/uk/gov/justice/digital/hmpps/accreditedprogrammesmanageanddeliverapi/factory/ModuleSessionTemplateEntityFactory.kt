package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway.MODERATE_INTENSITY
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.GROUP
import java.time.LocalDate
import java.util.UUID
import kotlin.String

class ModuleSessionTemplateEntityFactory {
  private var id: UUID? = null
  private var module: ModuleEntity = ModuleEntity(
    UUID.randomUUID(),
    AccreditedProgrammeTemplateEntity(
      UUID.randomUUID(),
      "accredited programme 1",
      LocalDate.now(),
      LocalDate.now().plusDays(1),
      mutableSetOf(),
      mutableSetOf(),
    ),
    "module 1",
    1,
    mutableSetOf(),
  )
  private var sessionNumber: Int? = 1
  private var sessionType: SessionType? = GROUP
  private var pathway: Pathway? = MODERATE_INTENSITY
  private var name: String? = "Module Session Template 1"
  private var description: String? = null
  private var durationMinutes: Int? = 60

  fun withId(id: UUID) = apply { this.id = id }
  fun withModule(module: ModuleEntity) = apply { this.module = module }
  fun withSessionNumber(sessionNumber: Int) = apply { this.sessionNumber = sessionNumber }
  fun withSessionType(sessionType: SessionType) = apply { this.sessionType = sessionType }
  fun withSessionType(pathway: Pathway) = apply { this.pathway = pathway }
  fun withName(name: String) = apply { this.name = name }
  fun withDescription(description: String) = apply { this.description = description }
  fun withDurationInMinutes(durationMinutes: Int) = apply { this.durationMinutes = durationMinutes }

  fun produce() = ModuleSessionTemplateEntity(
    id = this.id,
    module = this.module,
    sessionNumber = this.sessionNumber!!,
    sessionType = this.sessionType!!,
    pathway = this.pathway!!,
    name = this.name!!,
    description = this.description,
    durationMinutes = this.durationMinutes!!,
  )
}
