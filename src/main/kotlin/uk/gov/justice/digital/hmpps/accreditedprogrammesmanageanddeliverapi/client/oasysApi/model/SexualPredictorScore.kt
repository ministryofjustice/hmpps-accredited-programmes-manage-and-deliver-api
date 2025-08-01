package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

data class SexualPredictorScore(
  val ospIndecentPercentageScore: Int,
  val ospContactPercentageScore: Int,
  val ospIndirectImagePercentageScore: Int,
  val ospDirectContactPercentageScore: Int,
)
