package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumberAsInt
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDateTime
import java.util.UUID

class ReferralEntityFactory {
  private val referralStatusHistoryEntityFactory = ReferralStatusHistoryEntityFactory()
  private var id: UUID? = null
  private var personName: String? = randomSentence(wordRange = 1..3)
  private var interventionName: String? = "Building Choices"
  private var interventionType: InterventionType = InterventionType.ACP
  private var crn: String? = randomCrn()
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var statusHistories: MutableList<ReferralStatusHistoryEntity> = mutableListOf()
  private var setting: SettingType = SettingType.COMMUNITY
  private var eventId: String = randomUppercaseString(6)
  private var sourcedFrom: ReferralEntitySourcedFrom? = null
  private var eventNumber: Int? = randomNumberAsInt(1)
  private var cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE
  private var deliveryLocationPreferences: DeliveryLocationPreferenceEntity? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withPersonName(personName: String?) = apply { this.personName = personName }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStatusHistories(statusHistories: MutableList<ReferralStatusHistoryEntity>) = apply { this.statusHistories = statusHistories }

  fun withSourcedFrom(value: ReferralEntitySourcedFrom?) = apply { this.sourcedFrom = value }

  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withInterventionName(interventionName: String?) = apply { this.interventionName = interventionName }

  fun withInterventionType(interventionType: InterventionType) = apply { this.interventionType = interventionType }
  fun withEventNumber(eventNumber: Int?) = apply { this.eventNumber = eventNumber }
  fun withEventId(eventId: String) = apply { this.eventId = eventId }
  fun addStatusHistory(statusHistory: ReferralStatusHistoryEntity) = apply { this.statusHistories.add(statusHistory) }
  fun withDeliveryLocationPreferences(deliveryLocationPreferences: DeliveryLocationPreferenceEntity) = apply { this.deliveryLocationPreferences = deliveryLocationPreferences }

  fun produce() = ReferralEntity(
    id = this.id,
    personName = this.personName!!,
    crn = this.crn!!,
    createdAt = this.createdAt,
    interventionName = this.interventionName,
    statusHistories = this.statusHistories,
    setting = this.setting,
    interventionType = this.interventionType,
    sourcedFrom = this.sourcedFrom,
    eventId = this.eventId,
    eventNumber = this.eventNumber,
    cohort = this.cohort,
  )
}
