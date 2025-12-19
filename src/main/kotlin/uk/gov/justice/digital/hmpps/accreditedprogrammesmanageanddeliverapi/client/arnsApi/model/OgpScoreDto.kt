package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

data class OgpScoreDto(
  val ogpStaticWeightedScore: BigDecimal? = null,
  val ogpDynamicWeightedScore: BigDecimal? = null,
  val ogpTotalWeightedScore: BigDecimal? = null,
  val ogp1Year: BigDecimal? = null,
  val ogp2Year: BigDecimal? = null,
  val ogpRisk: ScoreLevel? = null,
)
