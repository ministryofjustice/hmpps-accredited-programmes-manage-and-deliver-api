package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.util.Optional
import java.util.UUID

interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {
  @Query("SELECT r FROM ReferralEntity r LEFT JOIN FETCH r.programmeGroupMemberships WHERE r.id = :id")
  fun findByIdWithMemberships(id: UUID): ReferralEntity?

  @Query("SELECT id from ReferralEntity")
  fun getAllIds(): List<UUID>

  @EntityGraph(attributePaths = ["statusHistories"])
  override fun findById(id: UUID): Optional<ReferralEntity?>

  @EntityGraph(attributePaths = ["statusHistories"])
  fun findByCrn(crn: String): List<ReferralEntity>
}
