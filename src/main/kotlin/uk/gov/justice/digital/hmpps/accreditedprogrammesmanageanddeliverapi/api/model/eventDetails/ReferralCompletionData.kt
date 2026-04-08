package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ReferralCompletionData(
  val requirementId: String,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val requirementCompletedAt: LocalDateTime,
)
