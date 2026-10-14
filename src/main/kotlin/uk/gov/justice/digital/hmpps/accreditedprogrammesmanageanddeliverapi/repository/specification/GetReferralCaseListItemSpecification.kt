package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralCaseListCriteria

fun getReferralCaseListItemSpecification(
  possibleStatuses: List<String>,
  criteria: ReferralCaseListCriteria,
): Specification<ReferralCaseListItemViewEntity> = Specification { root: Root<ReferralCaseListItemViewEntity>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
  val predicates: MutableList<Predicate> = mutableListOf()
  val caseReferenceNumberOrPersonName = criteria.caseReferenceNumberOrPersonName
  val offenceCohortAndLdcPairs = criteria.offenceCohortAndLdcPairs

  possibleStatuses.let {
    predicates.add(
      root.get<String>("status").`in`(possibleStatuses),
    )
  }

  caseReferenceNumberOrPersonName?.let {
    predicates.add(
      criteriaBuilder.or(
        criteriaBuilder.like(
          criteriaBuilder.lower(
            criteriaBuilder.concat(
              criteriaBuilder.concat(root.get<String>("personForename"), " "),
              root.get<String>("personSurname"),
            ),
          ),
          "%$caseReferenceNumberOrPersonName%".lowercase(),
        ),
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("crn")),
          "%$caseReferenceNumberOrPersonName%".lowercase(),
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

  criteria.statuses?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("status").`in`(it),
    )
  }

  criteria.sexes?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("sex").`in`(it),
    )
  }

  criteria.probationDeliveryUnits?.takeIf { it.isNotEmpty() }?.let {
    predicates.add(
      root.get<String>("pduName").`in`(it),
    )
  }

  criteria.reportingTeams?.takeIf { it.isNotEmpty() }?.let {
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
