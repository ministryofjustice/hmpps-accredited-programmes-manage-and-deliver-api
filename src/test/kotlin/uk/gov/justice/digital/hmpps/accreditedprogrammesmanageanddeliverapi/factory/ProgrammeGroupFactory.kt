package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDateTime
import java.util.UUID

class ProgrammeGroupFactory {
  private var id: UUID? = null
  private var code: String = "AAA111"
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdByUsername: String = "APerson"
  private var updatedAt: LocalDateTime? = null
  private var updatedByUsername: String? = null
  private var deletedAt: LocalDateTime? = null
  private var deletedByUsername: String? = null

  fun withCode(code: String) = apply { this.code = code }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedByUsername(createdByUsername: String) = apply { this.createdByUsername = createdByUsername }
  fun withUpdatedAt(updatedAt: LocalDateTime) = apply { this.updatedAt = updatedAt }
  fun withUpdatedByUsername(updatedByUsername: String) = apply { this.updatedByUsername = updatedByUsername }
  fun withDeletedAt(deletedAt: LocalDateTime) = apply { this.deletedAt = deletedAt }
  fun withDeletedByUsername(deletedByUsername: String) = apply { this.deletedByUsername = deletedByUsername }

  fun produce(): ProgrammeGroupEntity = ProgrammeGroupEntity(
    id = this.id,
    code = this.code,
    createdAt = this.createdAt,
    createdByUsername = this.createdByUsername,
    updatedAt = this.updatedAt,
    updatedByUsername = this.updatedByUsername,
    deletedAt = this.deletedAt,
    deletedByByUsername = this.deletedByUsername,
  )
}
