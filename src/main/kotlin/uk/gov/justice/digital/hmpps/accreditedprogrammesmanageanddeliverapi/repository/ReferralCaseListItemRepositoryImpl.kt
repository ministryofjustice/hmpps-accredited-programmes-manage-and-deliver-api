package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

@Repository
class ReferralCaseListItemRepositoryImpl(
  private val entityManager: EntityManager,
) : ReferralCaseListItemRepositoryCustom {

  override fun findAllCrns(spec: Specification<ReferralCaseListItemViewEntity>): List<String> {
    val builder = entityManager.criteriaBuilder
    val query = builder.createQuery(String::class.java)
    val root = query.from(ReferralCaseListItemViewEntity::class.java)

    query.select(root.get("crn"))

    spec.toPredicate(root, query, builder)?.let { query.where(it) }

    return entityManager.createQuery(query).resultList
  }
}
