package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.util.UUID

fun getGroupWaitlistItemSpecification(
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: List<String>?,
  cohorts: List<ProgrammeGroupCohort>?,
  nameOrCRN: String?,
  pdus: List<String>?,
  reportingTeams: List<String>?,
  regionName: String,
): Specification<GroupWaitlistItemViewEntity> = Specification { root, _, criteriaBuilder ->
  val predicates = mutableListOf<Predicate>()

  /**
   * ALLOCATED tab - look for referrals which are assigned to our group id
   * WAITLIST tab - look for ALL referrals which are NOT part of a group and have "Awaiting allocation" status
   */
  when (selectedTab) {
    GroupPageTab.ALLOCATED -> predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))

    GroupPageTab.WAITLIST -> {
      predicates.add(criteriaBuilder.equal(root.get<String>("regionName"), regionName))
      predicates.add(criteriaBuilder.equal(root.get<String>("status"), "Awaiting allocation"))
      predicates.add(criteriaBuilder.isNull(root.get<UUID>("activeProgrammeGroupId")))
      sex?.takeIf { it.isNotEmpty() }?.let {
        predicates.add(root.get<String>("sex").`in`(it))
      }

      cohorts?.takeIf { it.isNotEmpty() }?.let { cohortList ->
        val cohortOrPredicates = cohortList.map { cohort ->
          val (offenceType, hasLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
          criteriaBuilder.and(
            criteriaBuilder.equal(root.get<String>("cohort"), offenceType.name),
            criteriaBuilder.equal(root.get<Boolean>("hasLdc"), hasLdc),
          )
        }
        predicates.add(criteriaBuilder.or(*cohortOrPredicates.toTypedArray()))
      }

      pdus?.let {
        predicates.add(root.get<String>("pduName").`in`(pdus))
      }

      reportingTeams?.let {
        pdus?.let {
          predicates.add(
            root.get<String>("reportingTeam").`in`(reportingTeams),
          )
        }
      }
    }
  }

  // This filter is present on both tabs
  nameOrCRN?.let { search ->
    val searchTerm = "%${search.lowercase()}%"
    predicates.add(
      criteriaBuilder.or(
        criteriaBuilder.like(criteriaBuilder.lower(root.get("personName")), searchTerm),
        criteriaBuilder.like(criteriaBuilder.lower(root.get("crn")), searchTerm),
      ),
    )
  }

  criteriaBuilder.and(*predicates.toTypedArray())
}
