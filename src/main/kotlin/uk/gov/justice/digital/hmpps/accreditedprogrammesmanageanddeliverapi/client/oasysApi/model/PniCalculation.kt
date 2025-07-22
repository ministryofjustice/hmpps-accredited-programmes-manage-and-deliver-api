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

enum class ProgrammePathway {
  HIGH_INTENSITY_BC,
  MODERATE_INTENSITY_BC,
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
    fun toPathway(type: Type?): ProgrammePathway = when (type) {
      H -> ProgrammePathway.HIGH_INTENSITY_BC
      M -> ProgrammePathway.MODERATE_INTENSITY_BC
      A -> ProgrammePathway.ALTERNATIVE_PATHWAY
      O -> ProgrammePathway.MISSING_INFORMATION
      else -> throw IllegalArgumentException("Unknown Programme Pathway type: $type")
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
