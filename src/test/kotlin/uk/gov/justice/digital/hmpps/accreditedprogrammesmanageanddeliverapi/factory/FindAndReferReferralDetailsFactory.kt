package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.FindAndReferReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import java.util.UUID

class FindAndReferReferralDetailsFactory {
  private var interventionType: String = randomUppercaseString(10)
  private var interventionName: String = randomSentence(wordRange = 1..3)
  private var personReference: String = randomUppercaseString(6)
  private var personReferenceType: String = "CRN"
  private var referralId: UUID = UUID.randomUUID()
  private var setting: String = randomUppercaseString(8)

  fun withInterventionType(interventionType: String) = apply { this.interventionType = interventionType }
  fun withInterventionName(interventionName: String) = apply { this.interventionName = interventionName }
  fun withPersonReference(personReference: String) = apply { this.personReference = personReference }
  fun withPersonReferenceType(personReferenceType: String) = apply { this.personReferenceType = personReferenceType }
  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withSetting(setting: String) = apply { this.setting = setting }

  fun produce() = FindAndReferReferralDetails(
    interventionType = this.interventionType,
    interventionName = this.interventionName,
    personReference = this.personReference,
    personReferenceType = this.personReferenceType,
    referralId = this.referralId,
    setting = this.setting,
  )
}
