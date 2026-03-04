package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.util.UUID

interface MessageHistoryRepository : JpaRepository<MessageHistoryEntity, UUID> {
  fun findByReferral(referral: ReferralEntity): List<MessageHistoryEntity>
}
