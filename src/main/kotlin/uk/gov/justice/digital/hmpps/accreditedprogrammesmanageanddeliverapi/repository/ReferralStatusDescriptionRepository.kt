package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.util.UUID

interface ReferralStatusDescriptionRepository : JpaRepository<ReferralStatusDescriptionEntity, UUID> {
  @Query("SELECT rs FROM ReferralStatusDescriptionEntity rs WHERE rs.description = 'Awaiting assessment'")
  fun getAwaitingAssessmentStatusDescription(): ReferralStatusDescriptionEntity
}
