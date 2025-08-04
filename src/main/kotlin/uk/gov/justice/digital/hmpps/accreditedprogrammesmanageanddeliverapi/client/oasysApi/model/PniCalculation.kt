package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

data class PniCalculation(
  val sexDomain: LevelScore,
  val thinkingDomain: LevelScore,
  val relationshipDomain: LevelScore,
  val selfManagementDomain: LevelScore,
  val riskLevel: Level,
  val needLevel: Level,
  val totalDomainScore: Int,
  val pni: Type,
  val saraRiskLevel: SaraRiskLevel,
  val missingFields: List<String> = listOf(),
)

data class LevelScore(val level: Level, val score: Int)
data class SaraRiskLevel(val toPartner: Int, val toOther: Int) {
  companion object {
    private val riskLevelMap = mapOf(
      1 to SaraRisk.LOW,
      2 to SaraRisk.MEDIUM,
      3 to SaraRisk.HIGH,
    )

    fun getRiskForPartner(toPartner: Int?): SaraRisk = getRiskFromMap(toPartner)
    fun getRiskToOthers(toOther: Int?): SaraRisk = getRiskFromMap(toOther)

    private fun getRiskFromMap(riskLevel: Int?): SaraRisk = riskLevelMap.getOrDefault(riskLevel, SaraRisk.NOT_APPLICABLE)
  }
}

enum class OverallIntensity {
  HIGH,
  MODERATE,
  ALTERNATIVE_PATHWAY,
  MISSING_INFORMATION,
}

enum class Type {
  H,
  M,
  A,
  O,
  ;

  companion object {
    fun toIntensity(type: Type?): OverallIntensity = when (type) {
      H -> OverallIntensity.HIGH
      M -> OverallIntensity.MODERATE
      A -> OverallIntensity.ALTERNATIVE_PATHWAY
      O -> OverallIntensity.MISSING_INFORMATION
      else -> throw IllegalArgumentException("Unknown overall intensity type: $type")
    }
  }
}

enum class NeedLevel {
  HIGH_NEED,
  MEDIUM_NEED,
  LOW_NEED,
  ;

  companion object {
    fun fromLevel(level: Level?): NeedLevel = when (level) {
      Level.H -> HIGH_NEED
      Level.M -> MEDIUM_NEED
      Level.L -> LOW_NEED
      else -> throw IllegalArgumentException("Unknown Need Level: $level")
    }
  }
}

enum class Level {
  H,
  M,
  L,
}

enum class PniRiskLevel {
  HIGH_RISK,
  MEDIUM_RISK,
  LOW_RISK,
  ;

  companion object {
    fun fromLevel(level: Level?): PniRiskLevel = when (level) {
      Level.H -> HIGH_RISK
      Level.M -> MEDIUM_RISK
      Level.L -> LOW_RISK
      else -> throw IllegalArgumentException("Unknown Risk Level: $level")
    }
  }
}
