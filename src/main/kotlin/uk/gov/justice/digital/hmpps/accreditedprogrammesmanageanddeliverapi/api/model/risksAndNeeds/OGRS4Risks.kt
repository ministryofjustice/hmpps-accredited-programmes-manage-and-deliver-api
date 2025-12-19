package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import java.math.BigDecimal

/*
  Data class representing next generation actuarial predictors that have been developed
  to provide greater prediction accuracy, based on modern offending data
*/
data class OGRS4Risks(
  val allReoffendingScoreType: String? = null,
  val allReoffendingScore: BigDecimal? = null,
  val allReoffendingBand: String? = null,

  val violentReoffendingScoreType: String? = null,
  val violentReoffendingScore: BigDecimal? = null,
  val violentReoffendingBand: String? = null,

  val seriousViolentReoffendingScoreType: String? = null,
  val seriousViolentReoffendingScore: BigDecimal? = null,
  val seriousViolentReoffendingBand: String? = null,

  val directContactSexualReoffendingScore: BigDecimal? = null,
  val directContactSexualReoffendingBand: String? = null,

  val indirectImageContactSexualReoffendingScore: BigDecimal? = null,
  val indirectImageContactSexualReoffendingBand: String? = null,

  val combinedSeriousReoffendingScoreType: String? = null,
  val combinedSeriousReoffendingScore: BigDecimal? = null,
  val combinedSeriousReoffendingBand: String? = null,

)
