package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import java.time.LocalDate
import java.util.UUID

class ScheduleSessionRequestFactory {
  private var sessionTemplateId: UUID? = null
  private var referralIds: List<UUID> = listOf()
  private var facilitators: List<CreateGroupTeamMember> = listOf()
  private var startDate: LocalDate = LocalDate.now()
  private var startTime: SessionTime = SessionTime(10, 0, AmOrPm.AM)
  private var endTime: SessionTime = SessionTime(11, 0, AmOrPm.AM)

  fun withSessionTemplateId(sessionTemplateId: UUID?) = apply { this.sessionTemplateId = sessionTemplateId }
  fun withReferralIds(referralIds: List<UUID>) = apply { this.referralIds = referralIds }
  fun withFacilitators(facilitators: List<CreateGroupTeamMember>) = apply { this.facilitators = facilitators }
  fun withStartDate(startDate: LocalDate) = apply { this.startDate = startDate }
  fun withStartTime(startTime: SessionTime) = apply { this.startTime = startTime }
  fun withEndTime(endTime: SessionTime) = apply { this.endTime = endTime }
  fun produce() = ScheduleSessionRequest(
    sessionTemplateId = this.sessionTemplateId!!,
    referralIds = this.referralIds,
    facilitators = this.facilitators,
    startDate = this.startDate,
    startTime = this.startTime,
    endTime = this.endTime,
  )
}
