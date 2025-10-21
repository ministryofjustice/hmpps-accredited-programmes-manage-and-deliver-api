package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.util.UUID

fun getGroupWaitlistItemSpecification(
  groupId: UUID,
  sex: String?,
  cohort: OffenceCohort?,
  nameOrCRN: String?,
  pdu: String?,
): Specification<GroupWaitlistItemViewEntity> = Specification<GroupWaitlistItemViewEntity> { root, _, criteriaBuilder ->
  val predicates = mutableListOf<Predicate>()

  predicates.add(criteriaBuilder.equal(root.get<UUID>("activeProgrammeGroupId"), groupId))

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
