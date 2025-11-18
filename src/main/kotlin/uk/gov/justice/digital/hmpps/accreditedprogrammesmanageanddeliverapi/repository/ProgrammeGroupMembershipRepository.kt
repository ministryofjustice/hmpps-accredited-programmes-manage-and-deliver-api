package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import java.util.UUID

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
    """,
  )
  fun findByReferralAndProgrammeGroup(referralId: UUID, programmeGroupId: UUID): ProgrammeGroupMembershipEntity?
}
