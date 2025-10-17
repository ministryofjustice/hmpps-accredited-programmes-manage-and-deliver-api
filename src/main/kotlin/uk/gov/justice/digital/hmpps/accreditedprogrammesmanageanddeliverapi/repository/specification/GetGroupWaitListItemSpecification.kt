package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitListItemViewEntity

fun getGroupWaitListItemSpecification(
  crnOrPersonName: String? = null,
  cohort: String? = null,
  pdu: String? = null,
  reportingTeams: List<String>? = null,
): Specification<GroupWaitListItemViewEntity> = Specification<GroupWaitListItemViewEntity> { root: Root<GroupWaitListItemViewEntity?>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
  val predicates: MutableList<Predicate> = mutableListOf()

  crnOrPersonName?.let {
    predicates.add(
      criteriaBuilder.or(
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("personName")),
          "%$crnOrPersonName%".lowercase(),
        ),
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("crn")),
          "%$crnOrPersonName%".lowercase(),
        ),
      ),
    )
  }

  cohort?.let {
    predicates.add(
      criteriaBuilder.equal(
        root.get<String>("cohort"),
        cohort,
      ),
    )
  }

  pdu?.let {
    predicates.add(
      criteriaBuilder.equal(
        root.get<String>("pduName"),
        pdu,
      ),
    )
  }

  reportingTeams?.let {
    pdu?.let {
      predicates.add(
        root.get<String>("reportingTeam").`in`(reportingTeams),
      )
    }
  }

  query?.distinct(true)
  criteriaBuilder.and(*predicates.toTypedArray())
}
