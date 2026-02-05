package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class UpdateSessionAttendeesRequest(
  @field:NotNull(message = "referralIdList must not be null")
  @field:NotEmpty(message = "referralIdList must not be empty")
  val referralIdList: List<UUID>,
)
