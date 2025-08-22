package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Relationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRelationships(
  val prevOrCurrentDomesticAbuse: String?,
  val victimOfPartner: String?,
  val victimOfFamily: String?,
  val perpAgainstFamily: String?,
  val perpAgainstPartner: String?,
  val relIssuesDetails: String?,
  @JsonAlias("SARA")
  val sara: Sara?,
  val emotionalCongruence: String?,
  val relCloseFamily: String?,
  val prevCloseRelationships: String?,
  val relationshipWithPartner: String?,
  val relCurrRelationshipStatus: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sara(
  @Schema(
    description = "Risk of violence towards a partner",
    example = "Low",
    allowableValues = ["Low", "Medium", "High"],
  )
  val imminentRiskOfViolenceTowardsPartner: String?,
  @Schema(
    description = "Risk of violence towards others",
    example = "Medium",
    allowableValues = ["Low", "Medium", "High"],
  )
  val imminentRiskOfViolenceTowardsOthers: String?,
)

fun OasysRelationships.toModel(assessmentCompletedDate: LocalDate?) = Relationships(
  assessmentCompleted = assessmentCompletedDate,
  dvEvidence = prevOrCurrentDomesticAbuse?.let { it == YES },
  victimFormerPartner = victimOfPartner?.let { it == YES },
  victimFamilyMember = victimOfFamily?.let { it == YES },
  victimOfPartnerFamily = perpAgainstFamily?.let { it == YES },
  perpOfPartnerOrFamily = perpAgainstPartner?.let { it == YES },
  relIssuesDetails = relIssuesDetails,
  relCloseFamily = relCloseFamily,
  relCurrRelationshipStatus = relCurrRelationshipStatus,
  prevCloseRelationships = prevCloseRelationships,
  emotionalCongruence = emotionalCongruence,
  relationshipWithPartner = relationshipWithPartner,
  prevOrCurrentDomesticAbuse = prevOrCurrentDomesticAbuse,
)
