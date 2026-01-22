package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import java.util.UUID

data class EditSessionDetails(
  val sessionId: UUID,
  val groupCode: String,
  val sessionName: String,
  val sessionDate: String,
  val sessionStartTime: SessionTime,
  val sessionEndTime: SessionTime,
)
