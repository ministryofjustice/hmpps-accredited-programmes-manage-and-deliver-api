package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAttitude

class OasysAttitudeFactory {
  private var proCriminalAttitudes: String? = "Negative towards authority"
  private var motivationToAddressBehaviour: String? = "Low motivation"
  private var hostileOrientation: String? = "Hostile towards supervision"

  fun withProCriminalAttitudes(value: String?) = apply { this.proCriminalAttitudes = value }
  fun withMotivationToAddressBehaviour(value: String?) = apply { this.motivationToAddressBehaviour = value }
  fun withHostileOrientation(value: String?) = apply { this.hostileOrientation = value }

  fun produce() = OasysAttitude(
    proCriminalAttitudes = proCriminalAttitudes,
    motivationToAddressBehaviour = motivationToAddressBehaviour,
    hostileOrientation = hostileOrientation,
  )
}
