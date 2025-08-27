package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

interface ReferralCaseListItemRepositoryCustom {
  fun findAllCrns(spec: Specification<ReferralCaseListItemViewEntity>): List<String>
}
