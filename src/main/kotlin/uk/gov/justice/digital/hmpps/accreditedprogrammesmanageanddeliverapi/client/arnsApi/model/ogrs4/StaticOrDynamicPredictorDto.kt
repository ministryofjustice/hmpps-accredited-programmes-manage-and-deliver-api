package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreType
import java.math.BigDecimal

open class StaticOrDynamicPredictorDto(
  val staticOrDynamic: ScoreType? = null,
  score: BigDecimal? = null,
  band: ScoreLevel? = null,
) : BasePredictorDto(score, band)
