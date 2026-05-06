package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestReferralStatusDescription(
  val id: UUID?,
  val description: String,
  val updatedAt: LocalDateTime?,
)

fun ReferralStatusDescriptionEntity.toApi() = SubjectAccessRequestReferralStatusDescription(
  id = id,
  description = description,
  updatedAt = updatedAt,
)
