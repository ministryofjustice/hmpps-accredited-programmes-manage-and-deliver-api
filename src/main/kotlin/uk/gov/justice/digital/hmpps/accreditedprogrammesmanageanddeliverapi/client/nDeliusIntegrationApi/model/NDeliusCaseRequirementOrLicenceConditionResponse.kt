package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * This data structure comes back from both the /case/$crn/requirement/$id _and_ /case/$crn/licence-conditions/$id endpoint
 * It represents the "manager", associated with the inciting Requirement or Licence Condition which triggered the creation of
 * a Referral in our system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusCaseRequirementOrLicenceConditionResponse(
  val manager: RequirementOrLicenceConditionManager,
)

data class RequirementOrLicenceConditionPdu(
  /** @example N03ANPS */
  val code: String,
  /** @example All Location */
  val description: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequirementOrLicenceConditionManager(
  val staff: RequirementStaff,
  val team: CodeDescription,
  val probationDeliveryUnit: RequirementOrLicenceConditionPdu,
  val officeLocations: List<CodeDescription>,
)

/**
 * This likely represents a human being, although from Dev Testing, it appears that
 * this can have the value of "Unallocated Unallocated".  We may need to trigger
 * pathways from our code in the future if this appears to be a problem in production.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RequirementStaff(
  val code: String,
  val name: FullName,
)
