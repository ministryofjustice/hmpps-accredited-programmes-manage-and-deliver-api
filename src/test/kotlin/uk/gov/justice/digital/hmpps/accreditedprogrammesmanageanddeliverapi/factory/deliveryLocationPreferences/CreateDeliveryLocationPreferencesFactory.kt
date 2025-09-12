package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.deliveryLocationPreferences

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.CreateDeliveryLocationPreferences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences.PreferredDeliveryLocation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString

class CreateDeliveryLocationPreferencesFactory {

  private var preferredDeliveryLocations: MutableSet<PreferredDeliveryLocation> = mutableSetOf(
    PreferredDeliveryLocation(
      pduCode = randomUppercaseString(3),
      pduDescription = randomSentence(wordRange = 1..3),
      deliveryLocations = listOf(
        CodeDescription(code = randomUppercaseString(4), description = randomSentence(wordRange = 1..4)),
        CodeDescription(code = randomUppercaseString(4), description = randomSentence(wordRange = 1..4)),
      ),
    ),
  )

  private var cannotAttendText: String? = randomSentence(wordRange = 5..10)

  fun withPreferredDeliveryLocations(preferredDeliveryLocations: MutableSet<PreferredDeliveryLocation>) = apply {
    this.preferredDeliveryLocations = preferredDeliveryLocations
  }

  fun withCannotAttendText(cannotAttendText: String?) = apply {
    this.cannotAttendText = cannotAttendText
  }

  fun produce(): CreateDeliveryLocationPreferences = CreateDeliveryLocationPreferences(
    preferredDeliveryLocations = preferredDeliveryLocations,
    cannotAttendText = cannotAttendText,
  )
}

class PreferredDeliveryLocationsFactory {

  private var pduCode: String = randomUppercaseString(3)
  private var pduDescription: String = randomSentence(wordRange = 1..3)
  private var deliveryLocations: List<CodeDescription> = listOf(
    CodeDescription(code = randomUppercaseString(4), description = randomSentence(wordRange = 1..4)),
  )

  fun withPduCode(pduCode: String) = apply { this.pduCode = pduCode }
  fun withPduDescription(pduDescription: String) = apply { this.pduDescription = pduDescription }
  fun withDeliveryLocations(deliveryLocations: List<CodeDescription>) = apply { this.deliveryLocations = deliveryLocations }

  fun produce(): PreferredDeliveryLocation = PreferredDeliveryLocation(
    pduCode = pduCode,
    pduDescription = pduDescription,
    deliveryLocations = deliveryLocations,
  )
}
