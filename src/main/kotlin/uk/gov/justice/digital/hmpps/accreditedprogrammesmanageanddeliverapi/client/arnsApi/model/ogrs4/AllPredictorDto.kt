package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.ogrs4

data class AllPredictorDto(
  val allReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val violentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
)
