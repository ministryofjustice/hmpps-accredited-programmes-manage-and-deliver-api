package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.UserRegionOverrideEntity
import java.util.UUID

@Repository
interface UserRegionOverrideRepository : JpaRepository<UserRegionOverrideEntity, UUID> {

  @Query(
    """
      SELECT uro.regionName
      FROM UserRegionOverrideEntity uro
      WHERE lower(uro.username) = lower(:username)
        AND uro.deletedAt IS NULL
      ORDER BY uro.regionName
    """,
  )
  fun findActiveRegionNamesByUsername(username: String): List<String>
}
