package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import java.time.LocalDateTime

data class RiskScoresDto(
  val completedDate: LocalDateTime? = null,
  val assessmentStatus: String? = null,
  val groupReconvictionScore: OgrScoreDto? = null,
  val violencePredictorScore: OvpScoreDto? = null,
  val generalPredictorScore: OgpScoreDto? = null,
  val riskOfSeriousRecidivismScore: RsrScoreDto? = null,
  val sexualPredictorScore: OspScoreDto? = null,
)
