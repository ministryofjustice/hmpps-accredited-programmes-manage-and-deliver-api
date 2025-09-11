package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import java.time.LocalDateTime

data class DeliveryLocationPreferences(
  @get:JsonProperty("preferredDeliveryLocations")
  @Schema(description = "List of preferred delivery locations where the person can attend the programme")
  val canAttendLocations: List<String>? = emptyList(),

  @get:JsonProperty("cannotAttendLocations")
  @Schema(
    description = "Text describing locations or circumstances where the person cannot attend",
    example = "Cannot attend evening sessions due to caring responsibilities",
  )
  val cannotAttendLocations: String?,

  @get:JsonProperty("lastUpdatedBy")
  @Schema(description = "The user that last created the delivery location preferences")
  val createdBy: String? = null,
  @get:JsonProperty("lastUpdatedAt")
  @Schema(description = "The time and date of the last update to the delivery location preferences")
  val lastUpdatedAt: LocalDateTime? = null,
)

fun DeliveryLocationPreferenceEntity.toModel(): DeliveryLocationPreferences = DeliveryLocationPreferences(
  createdBy = createdBy,
  lastUpdatedAt = lastUpdatedAt,
  cannotAttendLocations = locationsCannotAttendText,
  canAttendLocations = preferredDeliveryLocations.map { it.deliusDescription },
)
