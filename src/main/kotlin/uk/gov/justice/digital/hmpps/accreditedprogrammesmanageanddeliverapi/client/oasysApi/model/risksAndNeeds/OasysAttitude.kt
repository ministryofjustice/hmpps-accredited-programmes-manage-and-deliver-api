package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Attitude

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAttitude(
  val proCriminalAttitudes: String?,
  val motivationToAddressBehaviour: String?,
  val hostileOrientation: String?,
)

fun OasysAttitude.toModel() = Attitude(
  proCriminalAttitudes = proCriminalAttitudes,
  motivationToAddressBehaviour = motivationToAddressBehaviour,
  hostileOrientation = hostileOrientation,
)
