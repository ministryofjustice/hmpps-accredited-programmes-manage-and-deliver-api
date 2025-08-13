package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds.Health
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.YesValue.YES

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysHealth(
  val generalHealth: String?,
  val generalHeathSpecify: String?,
)

fun OasysHealth.toModel() = Health(
  anyHealthConditions = generalHealth == YES,
  description = generalHeathSpecify,
)
