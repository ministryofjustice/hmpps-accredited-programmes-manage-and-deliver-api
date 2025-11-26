package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord
import java.time.LocalDate

class CreateGroupRequestFactory(
  private val teamMemberFactory: CreateGroupTeamMemberFactory = CreateGroupTeamMemberFactory(),
  private val sessionSlotFactory: CreateGroupSessionSlotFactory = CreateGroupSessionSlotFactory(),
) {

  fun produce(
    groupCode: String? = null,
    cohort: ProgrammeGroupCohort? = null,
    sex: ProgrammeGroupSexEnum? = null,
    startedAtDate: LocalDate? = null,
    createGroupSessionSlot: Set<CreateGroupSessionSlot>? = null,
    pduName: String? = null,
    pduCode: String? = null,
    deliveryLocationName: String? = null,
    deliveryLocationCode: String? = null,
    teamMembers: List<CreateGroupTeamMember>? = null,
  ): CreateGroupRequest = CreateGroupRequest(
    groupCode = groupCode ?: randomUppercaseString(),
    cohort = cohort ?: ProgrammeGroupCohort.GENERAL,
    sex = sex ?: ProgrammeGroupSexEnum.MALE,
    startedAtDate = startedAtDate ?: LocalDate.now(),
    createGroupSessionSlot = createGroupSessionSlot ?: setOf(sessionSlotFactory.produce()),
    pduName = pduName ?: randomWord(1..2).toString(),
    pduCode = pduCode ?: randomUppercaseString(),
    deliveryLocationName = deliveryLocationName ?: randomWord(1..2).toString(),
    deliveryLocationCode = deliveryLocationCode ?: randomUppercaseString(),
    teamMembers = teamMembers ?: listOf(teamMemberFactory.produce()),
  )
}
