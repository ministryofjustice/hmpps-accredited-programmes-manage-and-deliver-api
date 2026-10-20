package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

fun getReferralCaseListItemSpecification(
  possibleStatuses: List<String>,
  crnOrPersonName: String? = null,
  offenceCohortAndLdcPairs: List<Pair<OffenceCohort, Boolean>> = emptyList(),
  statuses: List<String>? = null,
  sexes: List<String>? = null,
  pdus: List<String>? = null,
  reportingTeams: List<String>? = null,
): Specification<ReferralCaseListItemViewEntity> = Specification { root: Root<ReferralCaseListItemViewEntity>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
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

  if (offenceCohortAndLdcPairs.isNotEmpty()) {
    val cohortPredicates = offenceCohortAndLdcPairs.map { (cohort, hasLdc) ->
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get<String>("cohort"), cohort.name),
        criteriaBuilder.equal(root.get<Boolean>("hasLdc"), hasLdc),
      )
    }
    predicates.add(criteriaBuilder.or(*cohortPredicates.toTypedArray()))
  }

  statuses?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("status").`in`(it),
    )
  }

  sexes?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("sex").`in`(it),
    )
  }

  pdus?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("pduName").`in`(it),
    )
  }

  reportingTeams?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("reportingTeam").`in`(it),
    )
  }

  query?.distinct(true)
  criteriaBuilder.and(*predicates.toTypedArray())
}

fun withCrns(
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
