package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAssessmentTimeline(
  val crn: String?,
  val nomsId: String?,
  val timeline: List<Timeline>,
) {
  init {
    require((crn != null) xor (nomsId != null)) {
      "Exactly one of crn or nomsId must be provided"
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Timeline(
  val id: Long,
  val status: String,
  val type: String,
  val completedAt: LocalDateTime?,
)

// Gets the most recent completed assessment
fun OasysAssessmentTimeline.getLatestCompletedLayerThreeAssessment(): Timeline? = timeline
  .filter { it.status == "COMPLETE" && it.type == "LAYER3" }
  .sortedByDescending { it.completedAt }
  .firstOrNull()
