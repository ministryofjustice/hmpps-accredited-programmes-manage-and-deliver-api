package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Level
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.LevelScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniCalculation
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.SaraRiskLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type

class PniCalculationFactory {
  private var sexDomain: LevelScore = LevelScore(Level.H, 10)
  private var thinkingDomain: LevelScore = LevelScore(Level.H, 10)
  private var relationshipDomain: LevelScore = LevelScore(Level.H, 10)
  private var selfManagementDomain: LevelScore = LevelScore(Level.H, 10)
  private var riskLevel: Level = Level.H
  private var needLevel: Level = Level.H
  private var totalDomainScore: Int = 5
  private var pni: Type = Type.H
  private var saraRiskLevel: SaraRiskLevel = SaraRiskLevel(10, 2)
  private var missingFields: List<String> = listOf()

  fun withSexDomain(sexDomain: LevelScore) = apply { this.sexDomain = sexDomain }
  fun withThinkingDomain(thinkingDomain: LevelScore) = apply { this.thinkingDomain = thinkingDomain }
  fun withRelationshipDomain(relationshipDomain: LevelScore) = apply { this.relationshipDomain = relationshipDomain }
  fun withSelfManagementDomain(selfManagementDomain: LevelScore) = apply { this.selfManagementDomain = selfManagementDomain }
  fun withRiskLevel(riskLevel: Level) = apply { this.riskLevel = riskLevel }
  fun withNeedLevel(needLevel: Level) = apply { this.needLevel = needLevel }
  fun withTotalDomainScore(totalDomainScore: Int) = apply { this.totalDomainScore = totalDomainScore }
  fun withPni(pni: Type) = apply { this.pni = pni }
  fun withSaraRiskLevel(saraRiskLevel: SaraRiskLevel) = apply { this.saraRiskLevel = saraRiskLevel }
  fun withMissingFields(missingFields: List<String>) = apply { this.missingFields = missingFields }

  fun produce() = PniCalculation(
    sexDomain = this.sexDomain,
    thinkingDomain = this.thinkingDomain,
    relationshipDomain = this.relationshipDomain,
    selfManagementDomain = this.selfManagementDomain,
    riskLevel = this.riskLevel,
    needLevel = this.needLevel,
    totalDomainScore = this.totalDomainScore,
    pni = this.pni,
    saraRiskLevel = this.saraRiskLevel,
    missingFields = this.missingFields,
  )
}
