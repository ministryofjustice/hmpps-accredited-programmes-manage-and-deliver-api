package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

data class LimitedAccessOffenderCheck(
  val crn: String,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String? = null,
  val restrictionMessage: String? = null,
)

data class LimitedAccessOffenderCheckResponse(
  val access: List<LimitedAccessOffenderCheck>,
)
