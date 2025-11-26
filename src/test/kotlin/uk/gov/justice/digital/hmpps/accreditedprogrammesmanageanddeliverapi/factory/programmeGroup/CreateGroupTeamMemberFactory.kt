package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomWord

class CreateGroupTeamMemberFactory {
  fun produce(
    personName: String? = null,
    personCode: String? = null,
    ndeliusTeamName: String? = null,
    ndeliusTeamCode: String? = null,
    teamMemberType: CreateGroupTeamMemberType? = null,
  ): CreateGroupTeamMember = CreateGroupTeamMember(
    facilitator = personName ?: randomFullName().getNameAsString(),
    facilitatorCode = personCode ?: randomUppercaseString(),
    teamName = ndeliusTeamName ?: randomWord(1..2).toString(),
    ndeliusTeamCode = ndeliusTeamCode ?: randomUppercaseString(),
    teamMemberType = teamMemberType ?: CreateGroupTeamMemberType.TREATMENT_MANAGER,
  )
}
