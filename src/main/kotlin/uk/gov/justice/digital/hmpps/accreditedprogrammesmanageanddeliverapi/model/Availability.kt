package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID

data class Availability(

  @get:JsonProperty("id")
  @Schema(example = "null", description = "Unique ID of the availability", required = false)
  var id: UUID? = null,

  @get:JsonProperty("referralId", required = true)
  @Schema(example = "d3f55f38-7c7b-4b6e-9aa1-e7d7f9e3e785", description = "The ID of the referral", required = true)
  val referralId: UUID? = null,

  @get:JsonProperty("startDate")
  @Schema(example = "2025-07-10", description = "Start date of the availability")
  val startDate: LocalDateTime? = null,

  @get:JsonProperty("endDate")
  @Schema(example = "2025-07-20", description = "End date of the availability", nullable = true)
  val endDate: LocalDateTime? = null,

  @get:JsonProperty("otherDetails")
  @Schema(example = "Available for remote sessions", description = "Additional details", nullable = true)
  val otherDetails: String? = null,

  @get:JsonProperty("lastModifiedBy")
  @Schema(example = "admin_user", description = "User who last modified this record")
  var lastModifiedBy: String? = null,

  @get:JsonProperty("lastModifiedAt")
  @Schema(example = "2025-07-10T12:00:00", description = "Timestamp when last modified")
  var lastModifiedAt: LocalDateTime? = null,

  @JsonProperty("availabilities")
  val availabilities: List<DailyAvailabilityModel>,
)

data class Slot(
  @JsonProperty("label")
  val label: String,

  @JsonProperty("value")
  var value: Boolean,
)

data class DailyAvailabilityModel(
  @JsonProperty("label")
  val label: AvailabilityOption,

  @JsonProperty("slots")
  val slots: List<Slot>,
)

enum class AvailabilityOption(val displayName: String) {

  MONDAY("Mondays"),
  TUESDAY("Tuesdays"),
  WEDNESDAY("Wednesdays"),
  THURSDAY("Thursdays"),
  FRIDAY("Fridays"),
  SATURDAY("Saturday"),
  SUNDAY("Sunday"),
}

fun AvailabilityOption.toDayOfWeek(): DayOfWeek = when (this) {
  AvailabilityOption.MONDAY -> DayOfWeek.MONDAY
  AvailabilityOption.TUESDAY -> DayOfWeek.TUESDAY
  AvailabilityOption.WEDNESDAY -> DayOfWeek.WEDNESDAY
  AvailabilityOption.THURSDAY -> DayOfWeek.THURSDAY
  AvailabilityOption.FRIDAY -> DayOfWeek.FRIDAY
  AvailabilityOption.SATURDAY -> DayOfWeek.SATURDAY
  AvailabilityOption.SUNDAY -> DayOfWeek.SUNDAY
}
