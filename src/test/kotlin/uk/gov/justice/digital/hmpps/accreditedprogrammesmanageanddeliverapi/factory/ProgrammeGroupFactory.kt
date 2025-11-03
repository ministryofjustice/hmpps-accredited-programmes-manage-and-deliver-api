package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSex
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDateTime
import java.util.UUID

class ProgrammeGroupFactory {
  private var id: UUID? = null
  private var code: String = "AAA111"
  private var cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE
  private var sex: ProgrammeGroupSex = ProgrammeGroupSex.MALE
  private var isLdc: Boolean = false
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdByUsername: String = "APerson"
  private var updatedAt: LocalDateTime? = null
  private var updatedByUsername: String? = null
  private var deletedAt: LocalDateTime? = null
  private var deletedByUsername: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withCode(code: String) = apply { this.code = code }
  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withSex(sex: ProgrammeGroupSex) = apply { this.sex = sex }
  fun withIsLdc(isLdc: Boolean) = apply { this.isLdc = isLdc }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedByUsername(createdByUsername: String) = apply { this.createdByUsername = createdByUsername }
  fun withUpdatedAt(updatedAt: LocalDateTime) = apply { this.updatedAt = updatedAt }
  fun withUpdatedByUsername(updatedByUsername: String) = apply { this.updatedByUsername = updatedByUsername }
  fun withDeletedAt(deletedAt: LocalDateTime) = apply { this.deletedAt = deletedAt }
  fun withDeletedByUsername(deletedByUsername: String) = apply { this.deletedByUsername = deletedByUsername }

  fun produce(): ProgrammeGroupEntity = ProgrammeGroupEntity(
    id = this.id,
    code = this.code,
    cohort = this.cohort,
    sex = this.sex,
    isLdc = this.isLdc,
    createdAt = this.createdAt,
    createdByUsername = this.createdByUsername,
    updatedAt = this.updatedAt,
    updatedByUsername = this.updatedByUsername,
    deletedAt = this.deletedAt,
    deletedByUsername = this.deletedByUsername,
  )

  fun toCreateGroup(): CreateGroup {
    val group = produce()
    return CreateGroup(group.code, ProgrammeGroupCohort.from(group.cohort, group.isLdc), group.sex)
  }
}
