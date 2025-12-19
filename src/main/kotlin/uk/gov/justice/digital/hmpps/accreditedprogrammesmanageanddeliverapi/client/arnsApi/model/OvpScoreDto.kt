package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Score
import java.math.BigDecimal

data class OvpScoreDto(
  val ovpStaticWeightedScore: BigDecimal? = null,
  val ovpDynamicWeightedScore: BigDecimal? = null,
  val ovpTotalWeightedScore: BigDecimal? = null,
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val ovpRisk: ScoreLevel? = null,
)

fun OvpScoreDto.toModel(): Score = Score(
  oneYear = oneYear,
  twoYears = twoYears,
  scoreLevel = ovpRisk?.name,
)
