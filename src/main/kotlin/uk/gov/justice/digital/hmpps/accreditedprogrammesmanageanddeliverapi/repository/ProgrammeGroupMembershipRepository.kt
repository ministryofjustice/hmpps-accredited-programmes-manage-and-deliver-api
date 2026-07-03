package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import java.util.UUID

@Repository
interface ProgrammeGroupMembershipRepository : JpaRepository<ProgrammeGroupMembershipEntity, UUID> {

  @EntityGraph(attributePaths = ["programmeGroup"])
  @Query(
    """
    SELECT pgm FROM ProgrammeGroupMembershipEntity pgm
    WHERE pgm.referral.id = :referralId
    AND pgm.deletedAt IS NULL
    ORDER BY pgm.createdAt DESC
    LIMIT 1
""",
  )
  fun findCurrentGroupByReferralId(referralId: UUID): ProgrammeGroupMembershipEntity?

  @Query(
    """
    SELECT pgm FROM ProgrammeGroupMembershipEntity pgm
    WHERE pgm.referral.id = :referralId
    AND pgm.programmeGroup.id = :programmeGroupId
    AND pgm.deletedAt IS NULL
    """,
  )
  fun findNonDeletedByReferralAndGroupIds(referralId: UUID, programmeGroupId: UUID): ProgrammeGroupMembershipEntity?

  @EntityGraph(attributePaths = ["programmeGroup"])
  fun findAllByProgrammeGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(programmeGroupId: UUID): List<ProgrammeGroupMembershipEntity>

  // Includes soft-deleted memberships: a group is only considered "empty" if it has never had any
  // membership, so that past sessions can be safely cascade-rescheduled without checking attendance.
  fun existsByProgrammeGroupId(programmeGroupId: UUID): Boolean

  @EntityGraph(attributePaths = ["referral"])
  @Query(
    """
    SELECT pgm FROM ProgrammeGroupMembershipEntity pgm
    WHERE pgm.programmeGroup.id = :programmeGroupId
    AND pgm.deletedAt IS NULL
    ORDER BY pgm.createdAt ASC
    """,
  )
  fun findAllActiveByProgrammeGroupId(programmeGroupId: UUID): List<ProgrammeGroupMembershipEntity>

  @EntityGraph(attributePaths = ["programmeGroup", "attendances", "attendances.session", "attendances.outcomeType", "attendances.notesHistory"])
  @Query(
    """
    SELECT pgm FROM ProgrammeGroupMembershipEntity pgm
    WHERE pgm.referral.id = :referralId
    ORDER BY pgm.createdAt DESC
    """,
  )
  fun findAllByReferralIdWithAttendances(referralId: UUID): List<ProgrammeGroupMembershipEntity>
}
