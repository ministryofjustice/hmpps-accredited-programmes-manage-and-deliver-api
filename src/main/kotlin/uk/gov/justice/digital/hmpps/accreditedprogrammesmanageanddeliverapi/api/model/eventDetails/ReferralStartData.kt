package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDateTime

@Schema(description = "Start data for a referral")
data class ReferralStartData(
  @Schema(description = "The ID of the requirement or licence-condition associated with this referral", required = true)
  val licReqId: String,
  @Schema(
    description = "The date and time of the first attended pre-group one-to-one session. This should be used as the 'Actual Start Date' for the requirement/licence condition",
    required = true,
    example = "2025-04-01T09:30:00.000Z",
  )
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val licReqStartedAt: LocalDateTime,
  @Schema(description = "The type of entity from which this status change was sourced", required = true)
  val sourcedFromEntityType: ReferralEntitySourcedFrom,
)
