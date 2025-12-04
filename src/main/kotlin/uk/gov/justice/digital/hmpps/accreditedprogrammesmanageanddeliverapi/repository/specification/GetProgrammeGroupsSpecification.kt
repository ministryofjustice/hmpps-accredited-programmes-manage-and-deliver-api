package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import java.time.LocalDateTime

fun getProgrammeGroupsSpecification(
  groupCode: String?,
  pdu: String?,
  deliveryLocation: String?,
  cohort: ProgrammeGroupCohort?,
  sex: String?,
  regionName: String?,
): Specification<ProgrammeGroupEntity> = Specification<ProgrammeGroupEntity> { root, _, cb ->
  val predicates = mutableListOf<Predicate>()

  // Exclude soft-deleted
  predicates.add(cb.isNull(root.get<LocalDateTime>("deletedAt")))

  groupCode?.let {
    predicates.add(cb.like(cb.lower(root.get("code")), "%$groupCode%".lowercase()))
  }

  pdu?.let {
    predicates.add(cb.equal(root.get<String>("probationDeliveryUnitName"), it))
  }

  deliveryLocation?.let {
    predicates.add(cb.equal(root.get<String>("deliveryLocationName"), it))
  }

  cohort?.let {
    val (offenceType, hasLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(it)
    predicates.add(cb.equal(root.get<OffenceCohort>("cohort"), offenceType))
    predicates.add(cb.equal(root.get<Boolean>("isLdc"), hasLdc))
  }

  sex?.let {
    predicates.add(cb.equal(root.get<ProgrammeGroupSexEnum>("sex"), it))
  }

  regionName?.let {
    predicates.add(cb.equal(root.get<String>("regionName"), it))
  }

  cb.and(*predicates.toTypedArray())
}
