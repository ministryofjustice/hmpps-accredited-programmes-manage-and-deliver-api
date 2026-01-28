package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.time.LocalTime

data class SessionTime(
  @NotNull(message = "hour must not be null")
  @Min(value = 1, message = "hour must be between 1 and 12")
  @Max(value = 12, message = "hour must be between 1 and 12")
  @get:JsonProperty("hour", required = true)
  @Schema(description = "Hour in 12-hour format (1-12)", example = "10")
  val hour: Int,

  @NotNull(message = "minutes must not be null")
  @Min(value = 0, message = "minutes must be between 0 and 59")
  @Max(value = 59, message = "minutes must be between 0 and 59")
  @get:JsonProperty("minutes", required = true)
  @Schema(description = "Minutes (0-59)", example = "30")
  val minutes: Int,

  @NotNull(message = "amOrPm must not be null")
  @get:JsonProperty("amOrPm", required = true)
  @Schema(description = "AM or PM designation", implementation = AmOrPm::class)
  val amOrPm: AmOrPm,
)

fun SessionTime.toLocalTime(): LocalTime {
  val hour24 = when (amOrPm) {
    AmOrPm.AM -> if (hour == 12) 0 else hour
    AmOrPm.PM -> if (hour == 12) 12 else hour + 12
  }
  return LocalTime.of(hour24, minutes)
}

fun fromDateTime(dateTime: LocalDateTime): SessionTime {
  fun Int.toHour12(): Int = when {
    this == 0 -> 12
    this > 12 -> this - 12
    else -> this
  }

  val hour24h = dateTime.hour
  val amOrPm = if (hour24h < 12) AmOrPm.AM else AmOrPm.PM

  return SessionTime(
    hour = hour24h.toHour12(),
    minutes = dateTime.minute,
    amOrPm = amOrPm,
  )
}
