package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralStatusHistoryEntityFactory {
  private var id: UUID? = null
  private var status: String? = randomUppercaseString(10)
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdBy: String? = randomSentence(wordRange = 1..2)
  private var startDate: LocalDateTime? = LocalDateTime.now()
  private var endDate: LocalDateTime? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withStatus(status: String?) = apply { this.status = status }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String?) = apply { this.createdBy = createdBy }
  fun withStartDate(startDate: LocalDateTime?) = apply { this.startDate = startDate }
  fun withEndDate(endDate: LocalDateTime?) = apply { this.endDate = endDate }

  fun produce() = ReferralStatusHistoryEntity(
    id = this.id,
    status = this.status,
    createdAt = this.createdAt,
    createdBy = this.createdBy,
    startDate = this.startDate,
    endDate = this.endDate,
  )
}
