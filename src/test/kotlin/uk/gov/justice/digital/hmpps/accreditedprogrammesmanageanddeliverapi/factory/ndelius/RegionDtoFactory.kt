package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RegionDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString

class RegionDtoFactory {
  fun produce(
    teams: List<RegionDto.TeamDto>? = null,
  ): RegionDto = RegionDto(
    teams = teams ?: listOf(TeamDtoFactory().produce()),
  )
}

class TeamDtoFactory {
  fun produce(
    code: String? = null,
    description: String? = null,
    members: List<RegionDto.MemberDto>? = null,
  ): RegionDto.TeamDto = RegionDto.TeamDto(
    code = code ?: randomUppercaseString(2),
    description = description ?: randomSentence(1..2),
    members = members ?: listOf(MemberDtoFactory().produce()),
  )
}

class MemberDtoFactory {
  fun produce(
    code: String? = null,
    name: FullName? = null,
  ): RegionDto.MemberDto = RegionDto.MemberDto(
    code = code ?: randomUppercaseString(2),
    name = name ?: randomFullName(),
  )
}
