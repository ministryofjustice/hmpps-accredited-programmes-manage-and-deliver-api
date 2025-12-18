package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreType
import java.math.BigDecimal

class VersionedStaticOrDynamicPredictorDto(
  val algorithmVersion: String? = null,
  staticOrDynamic: ScoreType? = null,
  score: BigDecimal? = null,
  band: ScoreLevel? = null,
) : StaticOrDynamicPredictorDto(staticOrDynamic, score, band)
