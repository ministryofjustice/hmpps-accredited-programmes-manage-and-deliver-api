package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity

@Repository
class ProgrammeGroupRepositoryImpl(
  private val entityManager: EntityManager,
) : ProgrammeGroupRepositoryCustom {

  override fun getDistinctFieldValues(
    spec: Specification<ProgrammeGroupEntity>,
    fieldName: String,
  ): List<String> {
    val cb = entityManager.criteriaBuilder
    val query: CriteriaQuery<String> = cb.createQuery(String::class.java)
    val root: Root<ProgrammeGroupEntity> = query.from(ProgrammeGroupEntity::class.java)

    // Apply the specification as a predicate
    val predicate = spec.toPredicate(root, query, cb)

    // Select distinct field values where the field is not null
    query.select(root.get(fieldName))
      .distinct(true)
      .where(
        cb.and(
          predicate ?: cb.conjunction(),
          cb.isNotNull(root.get<String>(fieldName)),
        ),
      )

    return entityManager.createQuery(query).resultList
  }
}
