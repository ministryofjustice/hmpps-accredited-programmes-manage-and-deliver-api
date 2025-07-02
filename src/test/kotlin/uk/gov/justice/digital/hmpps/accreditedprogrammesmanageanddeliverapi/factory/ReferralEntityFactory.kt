package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ReferralEntityFactory {
  private var id: UUID? = null
  private var personName: String? = randomSentence(wordRange = 1..3)
  private var crn: String? = randomUppercaseString(6)
  private var cohort: String? = randomUppercaseString(4)
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var sentenceEndDate: LocalDate = LocalDate.now().plusMonths(6)
  private var statusHistories: MutableList<ReferralStatusHistoryEntity> = mutableListOf()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withPersonName(personName: String?) = apply { this.personName = personName }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withCohort(cohort: String?) = apply { this.cohort = cohort }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withSentenceEndDate(sentenceEndDate: LocalDate) = apply { this.sentenceEndDate = sentenceEndDate }
  fun withStatusHistories(statusHistories: MutableList<ReferralStatusHistoryEntity>) = apply { this.statusHistories = statusHistories }

  fun addStatusHistory(statusHistory: ReferralStatusHistoryEntity) = apply { this.statusHistories.add(statusHistory) }

  fun produce() = ReferralEntity(
    id = this.id,
    personName = this.personName,
    crn = this.crn,
    cohort = this.cohort,
    createdAt = this.createdAt,
    statusHistories = this.statusHistories,
    sentenceEndDate = this.sentenceEndDate,
  )
}
