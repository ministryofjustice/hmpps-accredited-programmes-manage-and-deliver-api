package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.probationAccessControlApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class AllCaseAccess(
  val crn: String,
  val excludedFrom: List<AllCaseAccessUsernameRange> = emptyList(),
  val restrictedTo: List<AllCaseAccessUsernameRange> = emptyList(),
  val exclusionMessage: String? = null,
  val restrictionMessage: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AllCaseAccessUsernameRange(
  val username: String,
  val since: OffsetDateTime,
  val until: OffsetDateTime?,
)
