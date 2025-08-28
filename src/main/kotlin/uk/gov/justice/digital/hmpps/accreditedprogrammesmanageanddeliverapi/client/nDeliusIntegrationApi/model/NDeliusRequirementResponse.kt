package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * // TODO: There are open questions about this data here, which I need to actively investigate
 * APG-1222 --TJWC 2025-08-28
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusRequirementResponse(
  val manager: RequirementManager,
)

data class RequirementProbationDeliveryUnit(
  // e.g. N54NUTY
  val code: String,
  // e.g. Newcastle Upon Tyne
  val description: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequirementManager(
  val staff: RequirementStaff,
  val team: CodeDescription,
  val probationDeliveryUnit: RequirementProbationDeliveryUnit,
  // TODO: Determine if this can be null, or empty.
  // TODO: Probably need to flag that this list of offices is incompatible with ours
  val officeLocations: List<CodeDescription>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequirementStaff(
  val code: String,
  val name: FullName,
)
