package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class VictimsAndPartners(
  @Schema(example = "Yes", description = "Whether there was direct contact targeting of victims.")
  @get:JsonProperty("contactTargeting") val contactTargeting: String?,

  @Schema(
    example = "Yes",
    description = "Whether the offence was racially motivated or targeted an identifiable group.",
  )
  @get:JsonProperty("raciallyMotivated") val raciallyMotivated: String?,

  @Schema(
    example = "No",
    description = "Whether the offence was in response to a specific victim (e.g. revenge, settling grudges).",
  )
  @get:JsonProperty("revenge") val revenge: String?,

  @Schema(example = "No", description = "Whether there was physical violence towards a partner.")
  @get:JsonProperty("physicalViolenceTowardsPartner") val physicalViolenceTowardsPartner: String?,

  @Schema(example = "Yes", description = "Whether there was repeat victimisation of the same person.")
  @get:JsonProperty("repeatVictimisation") val repeatVictimisation: String?,

  @Schema(example = "No", description = "Whether the victim(s) were strangers to the offender.")
  @get:JsonProperty("victimWasStranger") val victimWasStranger: String?,

  @Schema(example = "No", description = "Whether stalking behaviour was involved.")
  @get:JsonProperty("stalking") val stalking: String?,
)
