package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

data class ProgrammeGroupDetails(
  val group: Group,
  val allocationAndWaitlistData: AllocationAndWaitlistData,
) {
  data class Group(val code: String, val regionName: String)
  data class Counts(val waitlist: Int = 0, val allocated: Int = 0)
  data class Pagination(val size: Int, val page: Int)
  data class Filters(
    val sex: List<String> = listOf("Male", "Female"),
    val cohort: List<OffenceCohort> = OffenceCohort.entries,
    val pduNames: List<String> = emptyList(),
    val reportingTeams: List<String> = emptyList(),
  )

  data class AllocationAndWaitlistData(
    val counts: Counts,
    val pagination: Pagination,
    val filters: Filters,
    val paginatedAllocationData: List<GroupAllocatedItem> = emptyList(),
    val paginatedWaitlistData: List<GroupWaitlistItem> = emptyList(),
  )
}
