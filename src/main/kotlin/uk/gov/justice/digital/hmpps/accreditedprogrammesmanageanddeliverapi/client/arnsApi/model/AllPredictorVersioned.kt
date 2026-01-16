package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.arnsApi.model.type.AssessmentStatus
import java.time.LocalDateTime

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "outputVersion",
  visible = true,
  defaultImpl = AllPredictorVersionedLegacyDto::class,
)
@JsonSubTypes(
  JsonSubTypes.Type(value = AllPredictorVersionedLegacyDto::class, name = "1"),
  JsonSubTypes.Type(value = AllPredictorVersionedDto::class, name = "2"),
)
interface AllPredictorVersioned<out T> {
  val completedDate: LocalDateTime?
  val status: AssessmentStatus?
  val outputVersion: String
  val output: T?
}
