package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import java.time.LocalDate

data class OffenceAnalysis(
  val assessmentCompleted: LocalDate?,
  val briefOffenceDetails: String?,
  val victimsAndPartners: VictimsAndPartners?,
  val recognisesImpact: Boolean?,
  val otherOffendersAndInfluences: OtherOffendersAndInfluences?,
  val motivationAndTriggers: String?,
  val responsibility: Responsibility?,
  val patternOfOffending: String?,
) {
  companion object {
    data class VictimsAndPartners(
      val contactTargeting: Boolean?,
      val raciallyMotivated: Boolean?,
      val revenge: Boolean?,
      val physicalViolenceTowardsPartner: Boolean?,
      val repeatVictimisation: Boolean?,
      val victimWasStranger: Boolean?,
      val stalking: Boolean?,
    )

    data class OtherOffendersAndInfluences(
      val wereOtherOffendersInvolved: Boolean?,
      val numberOfOthersInvolved: String?,
      val wasTheOffenderLeader: Boolean?,
      val peerGroupInfluences: String?,
    )

    data class Responsibility(
      val acceptsResponsibility: Boolean?,
      val acceptsResponsibilityDetail: String?,
    )
  }
}
