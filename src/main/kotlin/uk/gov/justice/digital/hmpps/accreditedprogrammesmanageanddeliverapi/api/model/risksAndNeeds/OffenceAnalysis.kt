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

  @Schema(example = "Yes", description = "Whether the individual recognises the impact of their offending behaviour.")
  @get:JsonProperty("recognisesImpact") val recognisesImpact: String?,

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
)
