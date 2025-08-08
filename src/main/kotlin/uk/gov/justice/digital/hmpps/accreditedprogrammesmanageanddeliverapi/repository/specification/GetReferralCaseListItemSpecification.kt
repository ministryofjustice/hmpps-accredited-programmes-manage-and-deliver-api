package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

fun getReferralCaseListItemSpecification(
  openOrClosed: OpenOrClosed,
  crnOrPersonName: String? = null,
  cohort: String? = null,
): Specification<ReferralCaseListItemViewEntity> = Specification<ReferralCaseListItemViewEntity> { root: Root<ReferralCaseListItemViewEntity?>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
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
  query?.distinct(true)
  criteriaBuilder.and(*predicates.toTypedArray())
}
