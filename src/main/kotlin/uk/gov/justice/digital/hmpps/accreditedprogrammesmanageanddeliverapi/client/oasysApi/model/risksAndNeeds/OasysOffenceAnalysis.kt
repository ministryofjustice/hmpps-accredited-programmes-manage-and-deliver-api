package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffenceAnalysis(
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
) {
  enum class WhatOccurred(val description: String) {
    TARGETING("Were there any direct victim(s) eg contact targeting"),
    RACIAL_MOTIVATED("Were any of the victim(s) targeted because of racial motivation or hatred of other identifiable group"),
    REVENGE("Response to a specific victim (eg revenge, settling grudges)"),
    PHYSICAL_VIOLENCE_TOWARDS_PARTNER("Physical violence towards partner"),
    REPEAT_VICTIMISATION("Repeat victimisation of the same person"),
    VICTIM_WAS_STRANGER("Were the victim(s) stranger(s) to the offender"),
    STALKING("Stalking"),
  }

  fun createVictimsAndPartners(whatOccurred: List<String>?) = OffenceAnalysis.Companion.VictimsAndPartners(
    contactTargeting = whatOccurred?.contains(WhatOccurred.TARGETING.description),
    raciallyMotivated = whatOccurred?.contains(WhatOccurred.RACIAL_MOTIVATED.description),
    revenge = whatOccurred?.contains(WhatOccurred.REVENGE.description),
    physicalViolenceTowardsPartner = whatOccurred?.contains(WhatOccurred.PHYSICAL_VIOLENCE_TOWARDS_PARTNER.description),
    repeatVictimisation = whatOccurred?.contains(WhatOccurred.REPEAT_VICTIMISATION.description),
    victimWasStranger = whatOccurred?.contains(WhatOccurred.VICTIM_WAS_STRANGER.description),
    stalking = whatOccurred?.contains(WhatOccurred.STALKING.description),
  )

  fun createOtherOffendersAndInfluences() = OffenceAnalysis.Companion.OtherOffendersAndInfluences(
    wereOtherOffendersInvolved = othersInvolved == YES,
    numberOfOthersInvolved = numberOfOthersInvolved,
    // TODO find where this field comes from
    wasTheOffenderLeader = null,
    peerGroupInfluences = peerGroupInfluences,
  )

  fun createResponsibility() = OffenceAnalysis.Companion.Responsibility(
    acceptsResponsibility = acceptsResponsibilityYesNo == YES,
    acceptsResponsibilityDetail = acceptsResponsibility,
  )
}

fun OasysOffenceAnalysis.toModel(assessmentComplete: LocalDateTime?): OffenceAnalysis = OffenceAnalysis(
  assessmentCompleted = assessmentComplete?.toLocalDate(),
  briefOffenceDetails = offenceAnalysis,
  victimsAndPartners = createVictimsAndPartners(whatOccurred),
  recognisesImpact = recognisesImpact == YES,
  otherOffendersAndInfluences = createOtherOffendersAndInfluences(),
  motivationAndTriggers = offenceMotivation,
  responsibility = createResponsibility(),
  patternOfOffending = patternOffending,
)
