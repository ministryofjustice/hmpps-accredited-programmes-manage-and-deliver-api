package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceDetail
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffenceDetail(
  val offenceAnalysis: String?,
  val whatOccurred: List<String>?,
  val recognisesImpact: String?,
  val numberOfOthersInvolved: String?,
  val othersInvolved: String?,
  val peerGroupInfluences: String?,
  val offenceMotivation: String?,
  val acceptsResponsibilityYesNo: String?,
  val acceptsResponsibility: String?,
  val patternOffending: String?,
)

fun OasysOffenceDetail.toModel() = OffenceDetail(
  offenceDetails = offenceAnalysis,
  contactTargeting = whatOccurred?.contains(WhatOccurred.TARGETING.desc),
  raciallyMotivated = whatOccurred?.contains(WhatOccurred.RACIAL.desc),
  revenge = whatOccurred?.contains(WhatOccurred.REVENGE.desc),
  domesticViolence = whatOccurred?.contains(WhatOccurred.DOMESTIC_VIOLENCE.desc),
  repeatVictimisation = whatOccurred?.contains(WhatOccurred.VICTIMISATION.desc),
  victimWasStranger = whatOccurred?.contains(WhatOccurred.STRANGER.desc),
  stalking = whatOccurred?.contains(WhatOccurred.STALKING.desc),
  recognisesImpact = recognisesImpact == YES,
  numberOfOthersInvolved = numberOfOthersInvolved,
  othersInvolvedDetail = othersInvolved,
  peerGroupInfluences = peerGroupInfluences,
  motivationAndTriggers = offenceMotivation,
  acceptsResponsibility = acceptsResponsibility == YES,
  acceptsResponsibilityDetail = patternOffending,
)

enum class WhatOccurred(val desc: String) {
  TARGETING("Were there any direct victim(s) eg contact targeting"),
  RACIAL("Were any of the victim(s) targeted because of racial motivation or hatred of other identifiable group"),
  REVENGE("Response to a specific victim (eg revenge, settling grudges)"),
  DOMESTIC_VIOLENCE("Physical violence towards partner"),
  VICTIMISATION("Repeat victimisation of the same person"),
  STRANGER("Were the victim(s) stranger(s) to the offender"),
  STALKING("Stalking"),
}
