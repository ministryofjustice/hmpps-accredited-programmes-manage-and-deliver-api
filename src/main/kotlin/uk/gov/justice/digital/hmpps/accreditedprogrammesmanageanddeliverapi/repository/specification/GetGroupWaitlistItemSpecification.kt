package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.util.UUID

fun getGroupWaitlistItemSpecification(
  selectedTab: GroupPageTab,
  groupId: UUID,
  sex: String?,
  cohort: OffenceCohort?,
  nameOrCRN: String?,
  pdu: String?,
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
    predicates.add(criteriaBuilder.equal(root.get<String>("cohort"), it.name))
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

  criteriaBuilder.and(*predicates.toTypedArray())
}
