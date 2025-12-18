package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

data class OspScoreDto(
  val ospIndecentPercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospIndecentScoreLevel: ScoreLevel? = null,
  val ospContactScoreLevel: ScoreLevel? = null,
  val ospIndirectImagePercentageScore: BigDecimal? = null,
  val ospDirectContactPercentageScore: BigDecimal? = null,
  val ospIndirectImageScoreLevel: ScoreLevel? = null,
  val ospDirectContactScoreLevel: ScoreLevel? = null,
)
