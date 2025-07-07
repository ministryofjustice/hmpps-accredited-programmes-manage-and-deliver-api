package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class ReferralDto(
  val id: UUID? = null,
  val personName: String,
  val crn: String,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  val statusHistories: List<ReferralStatusHistoryEntity>,

)

fun ReferralEntity.toDto() = ReferralDto(
  id = id,
  personName = personName,
  crn = crn,
  createdAt = createdAt,
  statusHistories = statusHistories,
)
