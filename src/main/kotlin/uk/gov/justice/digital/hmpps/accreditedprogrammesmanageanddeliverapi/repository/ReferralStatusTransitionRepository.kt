package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusTransitionEntity
import java.util.UUID

interface ReferralStatusTransitionRepository : JpaRepository<ReferralStatusTransitionEntity, UUID> {
  fun findByFromStatusId(fromStatusId: UUID): List<ReferralStatusTransitionEntity>
  fun findByFromStatusIdAndVisibleTrue(fromStatusId: UUID): MutableList<ReferralStatusTransitionEntity>

  fun findByFromStatusIdAndToStatusId(fromStatusId: UUID, toStatusId: UUID): ReferralStatusTransitionEntity?
  fun findByFromStatus(fromStatus: ReferralStatusDescriptionEntity): MutableList<ReferralStatusTransitionEntity>
  fun deleteAllByFromStatus(fromStatus: ReferralStatusDescriptionEntity)
  fun findByToStatus(toStatus: ReferralStatusDescriptionEntity): MutableList<ReferralStatusTransitionEntity>
}
