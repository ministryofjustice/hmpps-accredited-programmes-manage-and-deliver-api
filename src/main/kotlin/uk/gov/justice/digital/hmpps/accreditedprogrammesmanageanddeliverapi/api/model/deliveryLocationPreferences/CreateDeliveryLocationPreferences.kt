package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.deliveryLocationPreferences

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.EmptyStringToNullDeserializer

data class CreateDeliveryLocationPreferences(
  @Valid
  @get:JsonProperty("preferredDeliveryLocations", required = true)
  val preferredDeliveryLocations: MutableSet<PreferredDeliveryLocation>,
  @JsonDeserialize(using = EmptyStringToNullDeserializer::class)
  @get:JsonProperty("cannotAttendText", required = false)
  @Schema(
    example = "Alex River cannot attend locations in Postcode NE1",
    description = "Rich text explaining locations the person cannot attend",
  )
  val cannotAttendText: String? = null,
)

data class PreferredDeliveryLocation(
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
