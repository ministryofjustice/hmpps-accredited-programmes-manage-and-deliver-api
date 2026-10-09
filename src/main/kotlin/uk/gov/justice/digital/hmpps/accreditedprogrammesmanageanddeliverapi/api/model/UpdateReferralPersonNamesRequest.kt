package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateReferralPersonNamesRequest(
  @get:JsonProperty("referralIds", required = true)
  @field:Schema(
    example = "[981421e1-0242-4cde-92a2-44c737077f86, af2e88f7-8a89-4a01-b52a-5d7e6805f605]",
    description = """List of referral IDs to update structured person names for. 
      |If empty, will trigger re-updating for all referrals in the database that have blank structured person names.""",
    required = true,
  )
  val referralIds: List<UUID>,
)
