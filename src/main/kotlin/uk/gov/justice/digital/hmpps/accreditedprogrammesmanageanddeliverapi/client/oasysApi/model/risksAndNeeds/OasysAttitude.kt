package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Attitude
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAttitude(
  val proCriminalAttitudes: String? = null,
  val motivationToAddressBehaviour: String? = null,
  val hostileOrientation: String? = null,
)

fun OasysAttitude.toModel(assessmentCompletedDate: LocalDate?) = Attitude(
  assessmentCompleted = assessmentCompletedDate,
  proCriminalAttitudes = proCriminalAttitudes,
  motivationToAddressBehaviour = motivationToAddressBehaviour,
  hostileOrientation = hostileOrientation,
)
