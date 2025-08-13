package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRelationships
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Sara
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import kotlin.random.Random

class OasysRelationshipsFactory {
  private var prevOrCurrentDomesticAbuse: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var victimOfPartner: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var victimOfFamily: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var perpAgainstFamily: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var perpAgainstPartner: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var relIssuesDetails: String? = if (Random.nextBoolean()) randomSentence(wordRange = 5..15) else null
  private var sara: Sara? = SaraFactory().produce()
  private var emotionalCongruence: String? = listOf("HIGH", "MEDIUM", "LOW", "NOT_APPLICABLE").random()
  private var relCloseFamily: String? =
    listOf("VERY_CLOSE", "REASONABLY_CLOSE", "NOT_VERY_CLOSE", "NOT_CLOSE_AT_ALL").random()
  private var prevCloseRelationships: String? = listOf("YES", "NO", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS").random()
  private var relationshipWithPartner: String? = listOf("STABLE", "UNSTABLE", "CONFLICTED", "NO_PARTNER").random()
  private var relCurrRelationshipStatus: String? =
    listOf("SINGLE", "MARRIED", "COHABITING", "SEPARATED", "DIVORCED", "WIDOWED").random()

  fun withPrevOrCurrentDomesticAbuse(prevOrCurrentDomesticAbuse: String?) = apply { this.prevOrCurrentDomesticAbuse = prevOrCurrentDomesticAbuse }

  fun withVictimOfPartner(victimOfPartner: String?) = apply { this.victimOfPartner = victimOfPartner }
  fun withVictimOfFamily(victimOfFamily: String?) = apply { this.victimOfFamily = victimOfFamily }
  fun withPerpAgainstFamily(perpAgainstFamily: String?) = apply { this.perpAgainstFamily = perpAgainstFamily }
  fun withPerpAgainstPartner(perpAgainstPartner: String?) = apply { this.perpAgainstPartner = perpAgainstPartner }
  fun withRelIssuesDetails(relIssuesDetails: String?) = apply { this.relIssuesDetails = relIssuesDetails }
  fun withSara(sara: Sara?) = apply { this.sara = sara }
  fun withEmotionalCongruence(emotionalCongruence: String?) = apply { this.emotionalCongruence = emotionalCongruence }
  fun withRelCloseFamily(relCloseFamily: String?) = apply { this.relCloseFamily = relCloseFamily }
  fun withPrevCloseRelationships(prevCloseRelationships: String?) = apply { this.prevCloseRelationships = prevCloseRelationships }

  fun withRelationshipWithPartner(relationshipWithPartner: String?) = apply { this.relationshipWithPartner = relationshipWithPartner }

  fun withRelCurrRelationshipStatus(relCurrRelationshipStatus: String?) = apply { this.relCurrRelationshipStatus = relCurrRelationshipStatus }

  fun produce() = OasysRelationships(
    prevOrCurrentDomesticAbuse = this.prevOrCurrentDomesticAbuse,
    victimOfPartner = this.victimOfPartner,
    victimOfFamily = this.victimOfFamily,
    perpAgainstFamily = this.perpAgainstFamily,
    perpAgainstPartner = this.perpAgainstPartner,
    relIssuesDetails = this.relIssuesDetails,
    sara = this.sara,
    emotionalCongruence = this.emotionalCongruence,
    relCloseFamily = this.relCloseFamily,
    prevCloseRelationships = this.prevCloseRelationships,
    relationshipWithPartner = this.relationshipWithPartner,
    relCurrRelationshipStatus = this.relCurrRelationshipStatus,
  )
}

class SaraFactory {
  private var imminentRiskOfViolenceTowardsPartner: String? = listOf("YES", "NO", "DON'T_KNOW").random()
  private var imminentRiskOfViolenceTowardsOthers: String? = listOf("YES", "NO", "DON'T_KNOW").random()

  fun withImminentRiskOfViolenceTowardsPartner(imminentRiskOfViolenceTowardsPartner: String?) = apply { this.imminentRiskOfViolenceTowardsPartner = imminentRiskOfViolenceTowardsPartner }

  fun withImminentRiskOfViolenceTowardsOthers(imminentRiskOfViolenceTowardsOthers: String?) = apply { this.imminentRiskOfViolenceTowardsOthers = imminentRiskOfViolenceTowardsOthers }

  fun produce() = Sara(
    imminentRiskOfViolenceTowardsPartner = this.imminentRiskOfViolenceTowardsPartner,
    imminentRiskOfViolenceTowardsOthers = this.imminentRiskOfViolenceTowardsOthers,
  )
}
