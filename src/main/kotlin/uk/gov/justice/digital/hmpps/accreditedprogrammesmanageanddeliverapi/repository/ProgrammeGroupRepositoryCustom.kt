package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity

interface ProgrammeGroupRepositoryCustom {
  fun getDistinctFieldValues(
    spec: Specification<ProgrammeGroupEntity>,
    fieldName: String,
  ): List<String>
}
