package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReportingGroupSizeEntity
import java.time.LocalDate
import java.util.UUID

interface ReportingGroupSizeRepository : JpaRepository<ReportingGroupSizeEntity, UUID> {
  @Query(
    value = "SELECT * FROM reporting_group_size WHERE created_at::date > :date",
    nativeQuery = true,
  )
  fun getAllGroupsCreatedAfter(date: LocalDate): List<ReportingGroupSizeEntity>

  @Query(
    value = "SELECT * FROM reporting_group_size WHERE earliest_possible_start_date > :date",
    nativeQuery = true,
  )
  fun getAllGroupsWithEarliestStartDateAfter(date: LocalDate): List<ReportingGroupSizeEntity>
}
