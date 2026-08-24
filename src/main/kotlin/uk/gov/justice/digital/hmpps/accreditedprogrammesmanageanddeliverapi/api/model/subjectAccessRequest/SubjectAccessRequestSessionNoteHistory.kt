package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionNoteHistory(
  val id: UUID?,
  val note: String?,
  val recordedBy: String?,
  val createdAt: LocalDateTime,
)

fun SessionNotesHistoryEntity.toApi() = SubjectAccessRequestSessionNoteHistory(
  id = id,
  note = notes,
  recordedBy = createdByFullName,
  createdAt = createdAt,
)
