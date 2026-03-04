package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity

data class SubjectAccessRequestSessionNoteHistory(
  val note: String?,
)

fun SessionNotesHistoryEntity.toApi() = SubjectAccessRequestSessionNoteHistory(
  note = notes,
)
