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
  val probationDeliveryUnits: List<NDeliusApiProbationDeliveryUnitWithOfficeLocations> = emptyList(),
)

/**
 * The Manager of a Licence Condition or Requirement represents a Probation Practitioner
 * associated with the relevant initial source.  This person may not be the Person on
 * Probation's Probation Manager or Probation Practitioner.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RequirementOrLicenceConditionManager(
  val staff: RequirementStaff,
  val team: CodeDescription,
  val probationDeliveryUnit: NDeliusApiProbationDeliveryUnit,
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
