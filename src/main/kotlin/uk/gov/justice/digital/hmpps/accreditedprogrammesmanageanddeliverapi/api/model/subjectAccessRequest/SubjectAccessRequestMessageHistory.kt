package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.MessageHistoryEntity

data class SubjectAccessRequestMessageHistory(
  val description: String?,
  val message: String,
)

fun MessageHistoryEntity.toApi() = SubjectAccessRequestMessageHistory(
  description = description,
  message = message,
)
