package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.FindAndReferReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomNumberAsInt
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.util.UUID

class FindAndReferReferralDetailsFactory {
  private var interventionType: InterventionType = InterventionType.ACP
  private var interventionName: String = randomSentence(wordRange = 1..3)
  private var personReference: String = randomUppercaseString(6)
  private var personReferenceType: PersonReferenceType = PersonReferenceType.CRN
  private var referralId: UUID = UUID.randomUUID()
  private var setting: SettingType = SettingType.COMMUNITY
  private var sourcedFromReferenceType: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.REQUIREMENT
  private var sourcedFromReference = randomUppercaseString(6)
  private var eventNumber: Int = randomNumberAsInt(1)

  fun withInterventionType(interventionType: InterventionType) = apply { this.interventionType = interventionType }
  fun withInterventionName(interventionName: String) = apply { this.interventionName = interventionName }
  fun withPersonReference(personReference: String) = apply { this.personReference = personReference }
  fun withPersonReferenceType(personReferenceType: PersonReferenceType) = apply { this.personReferenceType = personReferenceType }
  fun withSourcedFromReferenceType(value: ReferralEntitySourcedFrom) = apply { this.sourcedFromReferenceType = value }
  fun withSourcedFromReference(value: String) = apply { this.sourcedFromReference = value }

  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withSetting(setting: SettingType) = apply { this.setting = setting }

  fun produce() = FindAndReferReferralDetails(
    interventionType = this.interventionType,
    interventionName = this.interventionName,
    personReference = this.personReference,
    personReferenceType = this.personReferenceType,
    referralId = this.referralId,
    setting = this.setting,
    sourcedFromReferenceType = this.sourcedFromReferenceType,
    sourcedFromReference = this.sourcedFromReference,
    eventNumber = this.eventNumber,
  )
}
