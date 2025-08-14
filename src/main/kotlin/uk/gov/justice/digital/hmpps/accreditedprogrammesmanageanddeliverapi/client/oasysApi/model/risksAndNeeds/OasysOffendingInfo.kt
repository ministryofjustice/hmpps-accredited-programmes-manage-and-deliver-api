package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffendingInfo(
  val ospCRisk: String?,
  val ospIRisk: String?,
  val ospIICRisk: String?,
  val ospDCRisk: String?,
  val crn: String?,
  val latestCompleteDate: LocalDateTime,
)
