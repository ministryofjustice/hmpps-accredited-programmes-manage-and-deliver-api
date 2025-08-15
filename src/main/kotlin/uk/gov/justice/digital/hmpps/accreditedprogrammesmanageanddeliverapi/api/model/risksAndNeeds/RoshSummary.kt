package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysRoshSummary
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.getHighestPriorityScore

data class RoshSummary(
  @Schema(
    example = "Medium",
    description = "Risk towards prisoners in Custody",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskPrisonersCustody") val riskPrisonersCustody: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards staff in Custody",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskStaffCustody") val riskStaffCustody: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards known adult in Custody",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskKnownAdultCustody") val riskKnownAdultCustody: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards public in Custody",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskPublicCustody") val riskPublicCustody: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards children in Custody",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskChildrenCustody") val riskChildrenCustody: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards staff in Community",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskStaffCommunity") val riskStaffCommunity: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards known adults in Community",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskKnownAdultCommunity") val riskKnownAdultCommunity: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards the public in Community",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskPublicCommunity") val riskPublicCommunity: String? = null,
  @Schema(
    example = "Medium",
    description = "Risk towards children in Community",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("riskChildrenCommunity") val riskChildrenCommunity: String? = null,
  @Schema(
    example = "Low",
    description = "Risk of Serious Harm level",
    required = false,
    allowableValues = ["Low", "Medium", "High"],
  )
  @get:JsonProperty("overallRoshLevel") val overallRoshLevel: String? = null,
)

fun OasysRoshSummary.toModel(): RoshSummary = RoshSummary(
  riskChildrenCustody = riskChildrenCustody?.type,
  riskChildrenCommunity = riskChildrenCustody?.type,
  riskPrisonersCustody = riskPrisonersCustody?.type,
  riskPublicCustody = riskPublicCustody?.type,
  riskPublicCommunity = riskPublicCommunity?.type,
  riskKnownAdultCustody = riskKnownAdultCustody?.type,
  riskKnownAdultCommunity = riskKnownAdultCommunity?.type,
  riskStaffCustody = riskStaffCustody?.type,
  riskStaffCommunity = riskStaffCommunity?.type,
  overallRoshLevel = getHighestPriorityScore()?.type,
)
