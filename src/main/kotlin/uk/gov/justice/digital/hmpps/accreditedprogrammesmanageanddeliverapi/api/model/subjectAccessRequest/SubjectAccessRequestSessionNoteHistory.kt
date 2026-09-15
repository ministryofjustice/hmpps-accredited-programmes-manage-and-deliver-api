package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.toSurname
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionNoteHistory(
  val id: UUID?,
  val note: String?,
  /**
   * Surname of the user who recorded the session note (particles preserved,
   * e.g. `van der Berg`). See APG-2580 / round-2 QAT review.
   */
  val recordedBy: String?,
  val createdAt: LocalDateTime,
)

fun SessionNotesHistoryEntity.toApi() = SubjectAccessRequestSessionNoteHistory(
  id = id,
  note = notes,
  // APG-2580: SAR data-dictionary rule is "surname only" for Recorded by fields.
  recordedBy = toSurname(createdByFullName),
  createdAt = createdAt,
)
