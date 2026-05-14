package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Transactional
@Component
class MaterializedViewRefresher(
  @Autowired
  private val entityManager: EntityManager,
) {
  private val allowedViewName = Regex("^[a-z_]+$")

  fun refresh(viewName: String) {
    require(allowedViewName.matches(viewName)) { "Invalid materialized view name '$viewName'" }
    entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW $viewName").executeUpdate()
  }

  fun refreshReferralCaseListItemView() = refresh("referral_caselist_item_view")

  fun refreshGroupWaitlistItemView() = refresh("group_waitlist_item_view")

  fun refreshReportingGroupSizeView() = refresh("reporting_group_size")
}
