package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord

class CreateGroupTeamMemberFactory {
  private var facilitator: String = "Default facilitator name"
  private var facilitatorForename: String = "Default facilitator forename"
  private var facilitatorSurname: String = "Default facilitator surname"
  private var facilitatorMiddleNames: String? = null
  private var facilitatorCode: String = "Default facilitator code"
  private var teamName: String = "Default team name"
  private var teamCode: String = "Default team code"
  private var teamMemberType: CreateGroupTeamMemberType = CreateGroupTeamMemberType.REGULAR_FACILITATOR

  fun withFacilitator(facilitator: String) = apply { this.facilitator = facilitator }
  fun withFacilitatorForename(facilitatorForename: String) = apply { this.facilitatorForename = facilitatorForename }
  fun withFacilitatorSurname(facilitatorSurname: String) = apply { this.facilitatorSurname = facilitatorSurname }
  fun withFacilitatorMiddleNames(facilitatorMiddleNames: String?) = apply { this.facilitatorMiddleNames = facilitatorMiddleNames }

  fun withFacilitatorCode(facilitatorCode: String) = apply { this.facilitatorCode = facilitatorCode }
  fun withTeamName(teamName: String) = apply { this.teamName = teamName }
  fun withTeamCode(teamCode: String) = apply { this.teamCode = teamCode }
  fun withTeamMemberType(teamMemberType: CreateGroupTeamMemberType) = apply { this.teamMemberType = teamMemberType }

  fun produce(): CreateGroupTeamMember = CreateGroupTeamMember(
    _facilitator = this.facilitator,
    facilitatorForename = this.facilitatorForename,
    facilitatorSurname = this.facilitatorSurname,
    facilitatorMiddleNames = this.facilitatorMiddleNames,
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
    _facilitator = personName ?: randomFullName().getNameAsString(),
    facilitatorForename = personName?.split(" ").orEmpty().firstOrNull() ?: randomWord(1..2).toString(),
    facilitatorSurname = personName?.split(" ").orEmpty().lastOrNull() ?: randomWord(1..2).toString(),
    facilitatorCode = personCode ?: randomUppercaseString(),
    teamName = ndeliusTeamName ?: randomWord(1..2).toString(),
    teamCode = ndeliusTeamCode ?: randomUppercaseString(),
    teamMemberType = teamMemberType ?: CreateGroupTeamMemberType.TREATMENT_MANAGER,
  )
}
