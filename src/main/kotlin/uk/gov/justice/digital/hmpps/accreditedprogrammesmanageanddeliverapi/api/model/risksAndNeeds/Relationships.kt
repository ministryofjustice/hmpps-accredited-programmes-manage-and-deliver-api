package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Relationships(
  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "true")
  @get:JsonProperty("dvEvidence") val dvEvidence: Boolean? = false,

  @Schema(example = "false")
  @get:JsonProperty("victimFormerPartner") val victimFormerPartner: Boolean? = false,

  @Schema(example = "true")
  @get:JsonProperty("victimFamilyMember") val victimFamilyMember: Boolean? = false,

  @Schema(example = "false")
  @get:JsonProperty("victimOfPartnerFamily") val victimOfPartnerFamily: Boolean? = false,

  @Schema(example = "true")
  @get:JsonProperty("perpOfPartnerOrFamily") val perpOfPartnerOrFamily: Boolean? = false,

  @Schema(example = "This person has a history of domestic violence")
  @get:JsonProperty("relIssuesDetails") val relIssuesDetails: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("relCloseFamily") val relCloseFamily: String? = null,

  @Schema(example = "Not in a relationship")
  @get:JsonProperty("relCurrRelationshipStatus") val relCurrRelationshipStatus: String? = null,

  @Schema(example = "2-Significant problems")
  @get:JsonProperty("prevCloseRelationships") val prevCloseRelationships: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("relationshipWithPartner") val relationshipWithPartner: String? = null,

  @Schema(example = "No")
  @get:JsonProperty("prevOrCurrentDomesticAbuse") val prevOrCurrentDomesticAbuse: String? = null,
)
