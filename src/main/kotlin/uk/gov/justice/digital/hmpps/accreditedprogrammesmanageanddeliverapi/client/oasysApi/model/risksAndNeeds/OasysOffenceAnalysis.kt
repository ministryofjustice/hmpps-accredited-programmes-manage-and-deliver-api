package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OffenceAnalysis
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.OtherOffendersAndInfluences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Responsibility
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.VictimsAndPartners
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

  fun createVictimsAndPartners(whatOccurred: List<String>?): VictimsAndPartners {
    fun hasOccurred(type: WhatOccurred) = if (whatOccurred?.contains(type.description) == true) "Yes" else "No"

    return VictimsAndPartners(
      contactTargeting = hasOccurred(WhatOccurred.TARGETING),
      raciallyMotivated = hasOccurred(WhatOccurred.RACIAL_MOTIVATED),
      revenge = hasOccurred(WhatOccurred.REVENGE),
      physicalViolenceTowardsPartner = hasOccurred(WhatOccurred.PHYSICAL_VIOLENCE_TOWARDS_PARTNER),
      repeatVictimisation = hasOccurred(WhatOccurred.REPEAT_VICTIMISATION),
      victimWasStranger = hasOccurred(WhatOccurred.VICTIM_WAS_STRANGER),
      stalking = hasOccurred(WhatOccurred.STALKING),
    )
  }

  fun createOtherOffendersAndInfluences() = OtherOffendersAndInfluences(
    wereOtherOffendersInvolved = othersInvolved ?: "No information available",
    numberOfOthersInvolved = numberOfOthersInvolved ?: "No information available",
    // TODO find where this field comes from
    wasTheOffenderLeader = "No information available",
    peerGroupInfluences = peerGroupInfluences ?: "No information available",
  )

  fun createResponsibility() = Responsibility(
    acceptsResponsibility = acceptsResponsibilityYesNo ?: "No information available",
    acceptsResponsibilityDetail = acceptsResponsibility ?: "No information available",
  )
}

fun OasysOffenceAnalysis.toModel(assessmentComplete: LocalDateTime?): OffenceAnalysis = OffenceAnalysis(
  assessmentCompleted = assessmentComplete?.toLocalDate(),
  briefOffenceDetails = offenceAnalysis,
  victimsAndPartners = createVictimsAndPartners(whatOccurred),
  recognisesImpact = recognisesImpact,
  otherOffendersAndInfluences = createOtherOffendersAndInfluences(),
  motivationAndTriggers = offenceMotivation,
  responsibility = createResponsibility(),
  patternOfOffending = patternOffending,
)
