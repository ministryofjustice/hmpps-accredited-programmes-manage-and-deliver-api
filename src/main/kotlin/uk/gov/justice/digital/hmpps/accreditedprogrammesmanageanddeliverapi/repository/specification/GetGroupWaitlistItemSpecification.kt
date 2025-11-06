package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.util.UUID

fun getGroupWaitlistItemSpecification(
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: ProgrammeGroupCohort?,
  nameOrCRN: String?,
  pdu: String?,
  reportingTeams: List<String>?,
): Specification<GroupWaitlistItemViewEntity> = Specification<GroupWaitlistItemViewEntity> { root, _, criteriaBuilder ->
  val predicates = mutableListOf<Predicate>()

  /**
   * ALLOCATED tab - look for referrals which are assigned to our group id
   * WAITLIST tab - look for ALL referrals which are NOT part of a group
   */
  when (selectedTab) {
    GroupPageTab.ALLOCATED -> predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))
    GroupPageTab.WAITLIST -> predicates.add(criteriaBuilder.isNull(root.get<UUID>("activeProgrammeGroupId")))
  }

  sex?.let {
    predicates.add(criteriaBuilder.equal(root.get<String>("sex"), it))
  }

  cohort?.let {
    val (offenceType, hasLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(it)
    predicates.add(criteriaBuilder.equal(root.get<String>("cohort"), offenceType.name))
    predicates.add(criteriaBuilder.equal(root.get<Boolean>("hasLdc"), hasLdc))
  }

  nameOrCRN?.let { search ->
    val searchTerm = "%${search.lowercase()}%"
    predicates.add(
      criteriaBuilder.or(
        criteriaBuilder.like(criteriaBuilder.lower(root.get("personName")), searchTerm),
        criteriaBuilder.like(criteriaBuilder.lower(root.get("crn")), searchTerm),
      ),
    )
  }

  pdu?.let {
    predicates.add(criteriaBuilder.equal(root.get<String>("pduName"), it))
  }

  reportingTeams?.let {
    pdu?.let {
      predicates.add(
        root.get<String>("reportingTeam").`in`(reportingTeams),
      )
    }
  }

  criteriaBuilder.and(*predicates.toTypedArray())
}
