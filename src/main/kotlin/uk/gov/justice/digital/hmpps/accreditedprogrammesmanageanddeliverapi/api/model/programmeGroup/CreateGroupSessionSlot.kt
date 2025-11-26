package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.DayOfWeek

@Schema(description = "Session slot details for a programme group")
data class CreateGroupSessionSlot(
  @NotNull(message = "dayOfWeek must not be null")
  @get:JsonProperty("dayOfWeek", required = true)
  @Schema(
    description = "The day of the week for the session",
    example = "MONDAY",
    implementation = DayOfWeek::class,
  )
  var dayOfWeek: DayOfWeek,

  @NotNull(message = "hour must not be null")
  @Min(value = 1, message = "hour must be between 1 and 12")
  @Max(value = 12, message = "hour must be between 1 and 12")
  @get:JsonProperty("hour", required = true)
  @Schema(
    description = "The hour of the session in 12-hour format",
    example = "9",
    minimum = "1",
    maximum = "12",
  )
  var hour: Int,

  @NotNull(message = "minutes must not be null")
  @Min(value = 0, message = "minutes must be between 0 and 59")
  @Max(value = 59, message = "minutes must be between 0 and 59")
  @get:JsonProperty("minutes", required = true)
  @Schema(
    description = "The minutes of the session",
    example = "0",
    minimum = "0",
    maximum = "59",
  )
  var minutes: Int,

  @NotNull(message = "amOrPm must not be null")
  @get:JsonProperty("amOrPm", required = true)
  @Schema(
    description = "AM or PM indicator",
    example = "AM",
    enumAsRef = true,
    implementation = AmOrPm::class,
  )
  var amOrPm: AmOrPm,
)

@Schema(description = "AM/PM time indicator")
enum class AmOrPm(
  @Schema(description = "Display label", example = "AM")
  val label: String,
) {
  @Schema(description = "Morning time")
  AM("am"),

  @Schema(description = "Afternoon/evening time")
  PM("pm"),
}
