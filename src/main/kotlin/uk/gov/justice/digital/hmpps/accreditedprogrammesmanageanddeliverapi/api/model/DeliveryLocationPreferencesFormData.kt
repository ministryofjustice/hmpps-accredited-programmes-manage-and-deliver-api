package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Form data for the multi-page DeliveryLocationPreferences form in the M&D UI")
data class DeliveryLocationPreferencesFormData(
  @field:Schema(description = "Person on Probation details (sourced freshly from nDelius)")
  val personOnProbation: PersonOnProbationSummary,

  @field:Schema(description = "Existing Delivery Location Preferences, if any")
  val existingDeliveryLocationPreferences: ExistingDeliveryLocationPreferences?,

  @field:Schema(description = "Primary PDU of the Manager of the Requirement or Licence Condition associated with a Referral")
  val primaryPdu: ProbationDeliveryUnit,

  @field:Schema(description = "Other PDUs in the same Region as the Manager")
  val otherPdusInSameRegion: List<ProbationDeliveryUnit>,
)

@Schema(description = "Summary information about the Person on Probation")
data class PersonOnProbationSummary(
  @field:Schema(description = "Full name", example = "Alex River")
  val name: String,

  @field:Schema(description = "Case Reference Number", example = "ABC123")
  val crn: String,

  @field:Schema(description = "Risk tier", example = "C2")
  val tier: String?,

  @field:Schema(description = "Date of birth", example = "2000-01-01")
  val dateOfBirth: LocalDate,
)

@Schema(description = "Existing Delivery Location Preferences")
data class ExistingDeliveryLocationPreferences(
  @field:Schema(description = "Locations (presently Offices) the person can attend")
  val canAttendLocationsValues: List<DeliveryLocationOption>,

  @field:Schema(description = "Rich text explaining locations the person cannot attend", example = "Locations in BN1")
  val cannotAttendLocations: String?,
)

@Schema(description = "A delivery location (i.e. Office) with value and label, formatted for the UI")
data class DeliveryLocationOption(
  @field:Schema(description = "Office code", example = "OFFICE-CODE-123")
  val value: String,

  @field:Schema(description = "Human-readable office name", example = "Brighton and Hove: Probation Office")
  val label: String,
)

@Schema(description = "Probation Delivery Unit with available delivery locations")
data class ProbationDeliveryUnit(
  @field:Schema(description = "PDU Code (sourced from nDelius)", example = "N54DUR")
  val code: String,

  @field:Schema(description = "PDU name", example = "County Durham and Darlington")
  val name: String,

  @field:Schema(description = "Available delivery locations within this PDU")
  val deliveryLocations: List<DeliveryLocationOption>,
)
