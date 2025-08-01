package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import java.math.BigDecimal
import kotlin.let

/**
 *
 * @param ogrs3
 * @param ovp
 * @param ospDc
 * @param ospIic
 * @param rsr
 * @param sara
 */
data class IndividualRiskScores(

  @Schema(example = "Medium", description = "The OGRS risk level")
  @get:JsonProperty("ogrs3Risk") val ogrs3Risk: String? = null,

  @Schema(example = "High", description = "The OVP Risk level")
  @get:JsonProperty("ovpRisk") val ovpRisk: String? = null,

  @Schema(example = "0", description = "")
  @get:JsonProperty("ospDc") val ospDc: String? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("ospIic") val ospIic: String? = null,

  @Schema(example = "5", description = "")
  @get:JsonProperty("rsr") val rsr: BigDecimal? = null,

  @Schema(description = "SARA related risk score")
  @get:JsonProperty("sara") val sara: Sara? = null,
) {
  companion object {
    fun from(pniResponse: PniResponse) = IndividualRiskScores(
      ogrs3Risk = pniResponse.assessment?.ogrs3Risk?.type,
      ovpRisk = pniResponse.assessment?.ovpRisk?.type,
      ospDc = pniResponse.assessment?.osp?.cdc?.type,
      ospIic = pniResponse.assessment?.osp?.iiic?.type,
      rsr = pniResponse.assessment?.rsrPercentage?.let { BigDecimal.valueOf(it) },
      sara = Sara.from(pniResponse),
    )
  }
}
