package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

fun getReferralCaseListItemSpecification(
  possibleStatuses: List<String>,
  crnOrPersonName: String? = null,
  cohort: String? = null,
  status: String? = null,
  pdu: String? = null,
  reportingTeams: List<String>? = null,
): Specification<ReferralCaseListItemViewEntity> = Specification<ReferralCaseListItemViewEntity> { root: Root<ReferralCaseListItemViewEntity?>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
  val predicates: MutableList<Predicate> = mutableListOf()

  possibleStatuses.let {
    predicates.add(
      root.get<String>("status").`in`(possibleStatuses),
    )
  }

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

  status?.let {
    predicates.add(
      criteriaBuilder.equal(
        root.get<String>("status"),
        status,
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

fun withAllowedCrns(
  baseSpec: Specification<ReferralCaseListItemViewEntity>,
  allowedCrns: Collection<String>,
): Specification<ReferralCaseListItemViewEntity> = Specification { root, query, builder ->
  val basePredicate = baseSpec.toPredicate(root, query, builder)
  val crnPredicate = root.get<String>("crn").`in`(allowedCrns)
  builder.and(basePredicate, crnPredicate)
}

fun withRegionNames(
  baseSpec: Specification<ReferralCaseListItemViewEntity>,
  regionNames: Collection<String>,
): Specification<ReferralCaseListItemViewEntity> = Specification { root, query, builder ->
  val basePredicate = baseSpec.toPredicate(root, query, builder)
  val regionPredicate = root.get<String>("regionName").`in`(regionNames)
  builder.and(basePredicate, regionPredicate)
}
