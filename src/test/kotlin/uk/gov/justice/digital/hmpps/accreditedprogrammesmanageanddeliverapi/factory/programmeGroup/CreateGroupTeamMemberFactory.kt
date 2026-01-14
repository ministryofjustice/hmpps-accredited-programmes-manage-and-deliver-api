package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord

class CreateGroupTeamMemberFactory {
  private var facilitator: String = "Default facilitator name"
  private var facilitatorCode: String = "Default facilitator code"
  private var teamName: String = "Default team name"
  private var teamCode: String = "Default team code"
  private var teamMemberType: CreateGroupTeamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR

  fun withFacilitator(facilitator: String) = apply { this.facilitator = facilitator }
  fun withFacilitatorCode(facilitatorCode: String) = apply { this.facilitatorCode = facilitatorCode }
  fun withTeamName(teamName: String) = apply { this.teamName = teamName }
  fun withTeamCode(teamCode: String) = apply { this.teamCode = teamCode }
  fun withTeamMemberType(teamMemberType: CreateGroupTeamMemberType) = apply { this.teamMemberType = teamMemberType }

  fun produce(): CreateGroupTeamMember = CreateGroupTeamMember(
    facilitator = this.facilitator,
    facilitatorCode = this.facilitatorCode,
    teamName = this.teamName,
    teamCode = this.teamCode,
    teamMemberType = this.teamMemberType,
  )

  fun produceWithRandomValues(
    personName: String? = null,
    personCode: String? = null,
    ndeliusTeamName: String? = null,
    ndeliusTeamCode: String? = null,
    teamMemberType: CreateGroupTeamMemberType? = null,
  ): CreateGroupTeamMember = CreateGroupTeamMember(
    facilitator = personName ?: randomFullName().getNameAsString(),
    facilitatorCode = personCode ?: randomUppercaseString(),
    teamName = ndeliusTeamName ?: randomWord(1..2).toString(),
    teamCode = ndeliusTeamCode ?: randomUppercaseString(),
    teamMemberType = teamMemberType ?: CreateGroupTeamMemberType.TREATMENT_MANAGER,
  )
}
