package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

open class BasePredictorDto(
  val score: BigDecimal? = null,
  val band: ScoreLevel? = null,
)
