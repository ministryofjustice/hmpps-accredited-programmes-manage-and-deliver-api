package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class PopulatePersonalDetailsRequest(
  @get:JsonProperty("referralIds", required = true)
  @Schema(
    example = "[\"981421e1-0242-4cde-92a2-44c737077f86\", \"af2e88f7-8a89-4a01-b52a-5d7e6805f605\"]",
    description = """List of referral IDs to populate personal details for.  The presence of 
      wildcard character "*" will trigger re-fetching for all referrals in the database (use responsibly!)""",
    required = true,
  )
  val referralIds: List<String>,
)
