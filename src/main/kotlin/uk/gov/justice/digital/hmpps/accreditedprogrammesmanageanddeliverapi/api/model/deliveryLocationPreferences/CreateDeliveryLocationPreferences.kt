package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription

data class CreateDeliveryLocationPreferences(
  @Valid
  @get:JsonProperty("preferredDeliveryLocations", required = true)
  val preferredDeliveryLocations: MutableSet<PreferredDeliveryLocations>,
  @Pattern(
    regexp = "^\\s*\\S[\\s\\S]*$",
    message = "cannotAttendText must not be blank if provided",
  )
  @get:JsonProperty("cannotAttendText", required = false)
  @Schema(
    example = "Alex River cannot attend locations in Postcode NE1",
    description = "Rich text explaining locations the person cannot attend",
  )
  val cannotAttendText: String? = null,
)

data class PreferredDeliveryLocations(
  @NotBlank(message = "pduCode must not be blank")
  @get:JsonProperty("pduCode", required = true)
  @Schema(
    example = "PDU001",
    description = "The nDelius code for the Probation Delivery Unit",
  )
  val pduCode: String,
  @NotBlank(message = "pduDescription must not be blank")
  @get:JsonProperty("pduDescription", required = true)
  @Schema(
    example = "London PDU",
    description = "The nDelius description for the Probation Delivery Unit",
  )
  val pduDescription: String,
  val deliveryLocations: List<CodeDescription>,
)
