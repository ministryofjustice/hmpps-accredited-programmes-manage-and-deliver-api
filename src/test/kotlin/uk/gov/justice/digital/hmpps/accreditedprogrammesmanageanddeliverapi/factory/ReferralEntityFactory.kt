package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralEntityFactory {
  private val referralStatusHistoryEntityFactory = ReferralStatusHistoryEntityFactory()
  private var id: UUID? = null
  private var personName: String? = randomSentence(wordRange = 1..3)
  private var interventionName: String? = "Building Choices"
  private var crn: String? = randomUppercaseString(6)
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var statusHistories: MutableList<ReferralStatusHistoryEntity> =
    mutableListOf(referralStatusHistoryEntityFactory.withStatus("Assessment started").produce())
  private var setting: String = "COMMUNITY"

  fun withId(id: UUID?) = apply { this.id = id }
  fun withPersonName(personName: String?) = apply { this.personName = personName }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStatusHistories(statusHistories: MutableList<ReferralStatusHistoryEntity>) = apply { this.statusHistories = statusHistories }

  fun addStatusHistory(statusHistory: ReferralStatusHistoryEntity) = apply { this.statusHistories.add(statusHistory) }

  fun produce() = ReferralEntity(
    id = this.id,
    personName = this.personName!!,
    crn = this.crn!!,
    createdAt = this.createdAt,
    interventionName = this.interventionName,
    statusHistories = this.statusHistories,
    setting = setting,
  )
}
