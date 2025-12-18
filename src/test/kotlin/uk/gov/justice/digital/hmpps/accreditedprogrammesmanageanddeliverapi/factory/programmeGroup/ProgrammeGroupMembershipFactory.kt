package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import java.time.LocalDateTime
import java.util.UUID

class ProgrammeGroupMembershipFactory(
  referral: ReferralEntity? = null,
  programmeGroup: ProgrammeGroupEntity? = null,
) {
  private var id: UUID? = null
  private var referral: ReferralEntity = referral ?: ReferralEntityFactory().produce()
  private var programmeGroup: ProgrammeGroupEntity = programmeGroup ?: ProgrammeGroupFactory().produce()
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdByUsername: String = "APerson"
  private var deletedAt: LocalDateTime? = null
  private var deletedByUsername: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withProgrammeGroup(programmeGroup: ProgrammeGroupEntity) = apply { this.programmeGroup = programmeGroup }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedByUsername(createdByUsername: String) = apply { this.createdByUsername = createdByUsername }
  fun withDeletedAt(deletedAt: LocalDateTime) = apply { this.deletedAt = deletedAt }
  fun withDeletedByUsername(deletedByUsername: String) = apply { this.deletedByUsername = deletedByUsername }

  fun produce(): ProgrammeGroupMembershipEntity = ProgrammeGroupMembershipEntity(
    id = this.id,
    referral = this.referral,
    programmeGroup = this.programmeGroup,
    createdAt = this.createdAt,
    createdByUsername = this.createdByUsername,
    deletedAt = this.deletedAt,
    deletedByUsername = this.deletedByUsername,
  )

  fun produceSet(): MutableSet<ProgrammeGroupMembershipEntity> = mutableSetOf(produce())
}
