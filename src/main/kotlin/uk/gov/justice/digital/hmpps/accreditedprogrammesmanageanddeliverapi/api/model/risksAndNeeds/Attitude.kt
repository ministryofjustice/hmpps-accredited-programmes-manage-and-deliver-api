package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param proCriminalAttitudes
 * @param motivationToAddressBehaviour
 * @param hostileOrientation
 */
data class Attitude(

  @Schema(example = "0-No problems")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("motivationToAddressBehaviour") val motivationToAddressBehaviour: String? = null,

  @Schema(example = "0-No problems")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: String? = null,
)
