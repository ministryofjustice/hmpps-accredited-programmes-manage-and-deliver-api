package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import java.time.LocalDateTime

data class ProgrammeCompletionDetails(
  val requirementId: String,
  val requirementCompletedAt: LocalDateTime,
)
