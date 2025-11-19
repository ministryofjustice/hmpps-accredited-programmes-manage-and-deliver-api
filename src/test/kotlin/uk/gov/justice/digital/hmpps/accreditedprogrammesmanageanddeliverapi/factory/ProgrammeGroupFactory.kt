package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ProgrammeGroupFactory {
  private var id: UUID? = null
  private var code: String = "AAA111"
  private var cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE
  private var sex: ProgrammeGroupSexEnum = ProgrammeGroupSexEnum.MALE
  private var isLdc: Boolean = false
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdByUsername: String = "APerson"
  private var updatedAt: LocalDateTime? = null
  private var updatedByUsername: String? = null
  private var deletedAt: LocalDateTime? = null
  private var deletedByUsername: String? = null
  private var regionName: String = "TEST REGION"
  private var startedAtDate: LocalDate? = null
  private var earliestStartDate: LocalDate? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withCode(code: String) = apply { this.code = code }
  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withSex(sex: ProgrammeGroupSexEnum) = apply { this.sex = sex }
  fun withIsLdc(isLdc: Boolean) = apply { this.isLdc = isLdc }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedByUsername(createdByUsername: String) = apply { this.createdByUsername = createdByUsername }
  fun withUpdatedAt(updatedAt: LocalDateTime) = apply { this.updatedAt = updatedAt }
  fun withUpdatedByUsername(updatedByUsername: String) = apply { this.updatedByUsername = updatedByUsername }
  fun withDeletedAt(deletedAt: LocalDateTime) = apply { this.deletedAt = deletedAt }
  fun withDeletedByUsername(deletedByUsername: String) = apply { this.deletedByUsername = deletedByUsername }
  fun withRegionName(regionName: String) = apply { this.regionName = regionName }
  fun withStartedAt(startedAt: LocalDate) = apply { this.startedAtDate = startedAt }
  fun withEarliestStartDate(earliestStartDate: LocalDate) = apply { this.earliestStartDate = earliestStartDate }

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
    regionName = this.regionName,
    startedAtDate = this.startedAtDate,
    earliestPossibleStartDate = this.earliestStartDate,
  )

  fun toCreateGroup(): CreateGroupRequest {
    val group = produce()
    return CreateGroupRequest(group.code, ProgrammeGroupCohort.from(group.cohort, group.isLdc), group.sex, LocalDate.parse("2025-01-01"))
  }
}
