package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class OtherOffendersAndInfluences(
  @Schema(example = "Yes", description = "Whether other offenders were involved in the offence.")
  @get:JsonProperty("wereOtherOffendersInvolved") val wereOtherOffendersInvolved: String?,

  @Schema(example = "2", description = "The number of other offenders involved.")
  @get:JsonProperty("numberOfOthersInvolved") val numberOfOthersInvolved: String?,

  @Schema(example = "No", description = "Whether the offender was the leader among other offenders.")
  @get:JsonProperty("wasTheOffenderLeader") val wasTheOffenderLeader: String?,

  @Schema(
    example = "Gang pressure and peer approval seeking",
    description = "Details about peer group influences on the offending.",
  )
  @get:JsonProperty("peerGroupInfluences") val peerGroupInfluences: String?,
)
