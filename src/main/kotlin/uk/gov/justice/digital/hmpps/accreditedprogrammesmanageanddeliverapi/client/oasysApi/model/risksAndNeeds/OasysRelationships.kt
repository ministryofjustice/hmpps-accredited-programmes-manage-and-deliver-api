package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Relationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES

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
  val imminentRiskOfViolenceTowardsPartner: String?,
  val imminentRiskOfViolenceTowardsOthers: String?,
)

fun OasysRelationships.toModel() = Relationships(
  dvEvidence = prevOrCurrentDomesticAbuse == YES,
  victimFormerPartner = victimOfPartner == YES,
  victimFamilyMember = victimOfFamily == YES,
  victimOfPartnerFamily = perpAgainstFamily == YES,
  perpOfPartnerOrFamily = perpAgainstPartner == YES,
  relIssuesDetails = relIssuesDetails,
  relCloseFamily = relCloseFamily,
  relCurrRelationshipStatus = relCurrRelationshipStatus,
  prevCloseRelationships = prevCloseRelationships,
  emotionalCongruence = emotionalCongruence,
  relationshipWithPartner = relationshipWithPartner,
  prevOrCurrentDomesticAbuse = prevOrCurrentDomesticAbuse,
)
