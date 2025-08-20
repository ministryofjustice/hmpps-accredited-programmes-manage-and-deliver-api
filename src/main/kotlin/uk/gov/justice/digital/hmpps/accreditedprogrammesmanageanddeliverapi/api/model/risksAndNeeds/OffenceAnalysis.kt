package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class OffenceAnalysis(
  @get:JsonProperty("assessmentCompleted", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val assessmentCompleted: LocalDate? = null,

  @Schema(
    example = "Physical assault on cellmate requiring medical attention on 22nd March 2024. Weapon possession discovered during cell search.",
    description = "Brief details of the current offence.",
  )
  @get:JsonProperty("briefOffenceDetails") val briefOffenceDetails: String?,
  @get:JsonProperty("victimsAndPartners") val victimsAndPartners: VictimsAndPartners?,
  @Schema(example = "true", description = "Whether the individual recognises the impact of their offending behaviour.")
  @get:JsonProperty("recognisesImpact") val recognisesImpact: Boolean?,
  @get:JsonProperty("otherOffendersAndInfluences") val otherOffendersAndInfluences: OtherOffendersAndInfluences?,
  @Schema(
    example = "Anger and frustration when challenged by authority",
    description = "The motivation and triggers for the offending behaviour.",
  )
  @get:JsonProperty("motivationAndTriggers") val motivationAndTriggers: String?,
  @get:JsonProperty("responsibility") val responsibility: Responsibility?,
  @Schema(
    example = "Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention.",
    description = "Analysis of patterns in the offending behaviour.",
  )
  @get:JsonProperty("patternOfOffending") val patternOfOffending: String?,
) {
  companion object {
    data class VictimsAndPartners(
      @Schema(example = "true", description = "Whether there was direct contact targeting of victims.")
      @get:JsonProperty("contactTargeting") val contactTargeting: Boolean?,

      @Schema(
        example = "false",
        description = "Whether the offence was racially motivated or targeted an identifiable group.",
      )
      @get:JsonProperty("raciallyMotivated") val raciallyMotivated: Boolean?,

      @Schema(
        example = "true",
        description = "Whether the offence was in response to a specific victim (e.g. revenge, settling grudges).",
      )
      @get:JsonProperty("revenge") val revenge: Boolean?,

      @Schema(example = "false", description = "Whether there was physical violence towards a partner.")
      @get:JsonProperty("physicalViolenceTowardsPartner") val physicalViolenceTowardsPartner: Boolean?,

      @Schema(example = "true", description = "Whether there was repeat victimisation of the same person.")
      @get:JsonProperty("repeatVictimisation") val repeatVictimisation: Boolean?,

      @Schema(example = "false", description = "Whether the victim(s) were strangers to the offender.")
      @get:JsonProperty("victimWasStranger") val victimWasStranger: Boolean?,

      @Schema(example = "false", description = "Whether stalking behaviour was involved.")
      @get:JsonProperty("stalking") val stalking: Boolean?,
    )

    data class OtherOffendersAndInfluences(
      @Schema(example = "true", description = "Whether other offenders were involved in the offence.")
      @get:JsonProperty("wereOtherOffendersInvolved") val wereOtherOffendersInvolved: Boolean?,

      @Schema(example = "2", description = "The number of other offenders involved.")
      @get:JsonProperty("numberOfOthersInvolved") val numberOfOthersInvolved: String?,

      @Schema(example = "true", description = "Whether the offender was the leader among other offenders.")
      @get:JsonProperty("wasTheOffenderLeader") val wasTheOffenderLeader: Boolean?,

      @Schema(
        example = "Gang pressure and peer approval seeking",
        description = "Details about peer group influences on the offending.",
      )
      @get:JsonProperty("peerGroupInfluences") val peerGroupInfluences: String?,
    )

    data class Responsibility(
      @Schema(example = "false", description = "Whether the individual accepts responsibility for their offending.")
      @get:JsonProperty("acceptsResponsibility") val acceptsResponsibility: Boolean?,

      @Schema(
        example = "Blames victims and circumstances",
        description = "Details about the individual's acceptance of responsibility.",
      )
      @get:JsonProperty("acceptsResponsibilityDetail") val acceptsResponsibilityDetail: String?,
    )
  }
}
